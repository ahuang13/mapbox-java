package com.mapbox.services.directions.v5;

import com.mapbox.services.Constants;
import com.mapbox.services.commons.MapboxBuilder;
import com.mapbox.services.commons.MapboxService;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;
import com.mapbox.services.directions.v5.models.DirectionsResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The Directions API allows the calculation of routes between coordinates. The fastest route
 * is returned with geometries, and turn-by-turn instructions. The Mapbox Directions API supports
 * routing for driving cars, riding bicycles and walking.
 *
 * @since 1.0.0
 */
public class MapboxDirections extends MapboxService<DirectionsResponse> {

  private Builder builder = null;
  private DirectionsService service = null;
  private Call<DirectionsResponse> call = null;

  // Allows testing
  private String baseUrl = Constants.BASE_API_URL;

  private MapboxDirections(Builder builder) {
    this.builder = builder;
  }

  /**
   * Used internally.
   *
   * @param baseUrl the baseURL
   * @since 1.0.0
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  private DirectionsService getService() {
    // No need to recreate it
    if (service != null) {
      return service;
    }

    // Retrofit instance
    Retrofit retrofit = new Retrofit.Builder()
      .client(getOkHttpClient())
      .baseUrl(baseUrl)
      .addConverterFactory(GsonConverterFactory.create())
      .build();

    // Directions service
    service = retrofit.create(DirectionsService.class);
    return service;
  }

  private Call<DirectionsResponse> getCall() {
    // No need to recreate it
    if (call != null) {
      return call;
    }

    call = getService().getCall(
      getHeaderUserAgent(builder.getClientAppName()),
      builder.getUser(),
      builder.getProfile(),
      builder.getCoordinates(),
      builder.getAccessToken(),
      builder.isAlternatives(),
      builder.getGeometries(),
      builder.getOverview(),
      builder.getRadiuses(),
      builder.isSteps(),
      builder.isContinueStraight());

    // Done
    return call;
  }

  /**
   * Execute the call
   *
   * @return The Directions v5 response
   * @throws IOException Signals that an I/O exception of some sort has occurred.
   * @since 1.0.0
   */
  @Override
  public Response<DirectionsResponse> executeCall() throws IOException {
    return getCall().execute();
  }

  /**
   * Execute the call
   *
   * @param callback A Retrofit callback.
   * @since 1.0.0
   */
  @Override
  public void enqueueCall(Callback<DirectionsResponse> callback) {
    getCall().enqueue(callback);
  }

  /**
   * Cancel the call
   *
   * @since 1.0.0
   */
  @Override
  public void cancelCall() {
    getCall().cancel();
  }

  /**
   * clone the call
   *
   * @return cloned call
   * @since 1.0.0
   */
  @Override
  public Call<DirectionsResponse> cloneCall() {
    return getCall().clone();
  }

  /**
   * Directions v5 builder
   *
   * @since 1.0.0
   */
  public static class Builder extends MapboxBuilder {

    // We use `Boolean` instead of `boolean` to allow unset (null) values.
    private String user = null;
    private String profile = null;
    private ArrayList<Position> coordinates = null;
    private String accessToken = null;
    private Boolean alternatives = null;
    private String geometries = null;
    private String overview = null;
    private double[] radiuses = null;
    private Boolean steps = null;
    private Boolean continueStraight = null;

    /**
     * Constructor
     *
     * @since 1.0.0
     */
    public Builder() {
      // Set defaults
      this.user = DirectionsCriteria.PROFILE_DEFAULT_USER;

      // We only support polyline encoded geometries to reduce the size of the response.
      // If we need the corresponding LineString object, this SDK can do the decoding with
      // LineString.fromPolyline(String polyline, int precision).
      this.geometries = DirectionsCriteria.GEOMETRY_POLYLINE;
    }

    /*
     * Setters
     */

    /**
     * @param user User string
     * @return Builder
     * @since 1.0.0
     */
    public Builder setUser(String user) {
      this.user = user;
      return this;
    }

    /**
     * @param profile Profile string
     * @return Builder
     * @since 1.0.0
     */
    public Builder setProfile(String profile) {
      this.profile = profile;
      return this;
    }

    /**
     * Set the list of coordinates for the directions service. If you've previously set an
     * origin with setOrigin() or a destination with setDestination(), those will be
     * overridden.
     *
     * @param coordinates List of {@link Position} giving origin and destination(s) coordinates.
     * @return Builder
     * @since 1.0.0
     */
    public Builder setCoordinates(ArrayList<Position> coordinates) {
      this.coordinates = coordinates;
      return this;
    }

    /**
     * Inserts the specified position at the beginning of the coordinates list. If you've
     * set other coordinates previously with setCoordinates() those elements are kept
     * and their index will be moved up by one (the coordinates are moved to the right).
     *
     * @param origin {@link Position} of route origin.
     * @return Builder
     * @since 1.0.0
     */
    public Builder setOrigin(Position origin) {
      if (coordinates == null) {
        coordinates = new ArrayList<>();
      }

      // The default behavior of ArrayList is to inserts the specified element at the
      // specified position in this list (beginning) and to shift the element currently at
      // that position (if any) and any subsequent elements to the right (adds one to
      // their indices)
      coordinates.add(0, origin);

      return this;
    }

    /**
     * Appends the specified destination to the end of the coordinates list. If you've
     * set other coordinates previously with setCoordinates() those elements are kept
     * and the destination is added at the end of the list.
     *
     * @param destination {@link Position} of route destination.
     * @return Builder
     * @since 1.0.0
     */
    public Builder setDestination(Position destination) {
      if (coordinates == null) {
        coordinates = new ArrayList<>();
      }

      // The default behavior for ArrayList is to appends the specified element
      // to the end of this list.
      coordinates.add(destination);

      return this;
    }

    /**
     * Required to call when building {@link Builder}.
     *
     * @param accessToken Mapbox access token, You must have a Mapbox account inorder to use
     *                    this library.
     * @return Builder
     * @since 1.0.0
     */
    @Override
    public Builder setAccessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    /**
     * Optionally, call if you'd like to receive alternative routes besides just one.
     *
     * @param alternatives true if you'd like alternative routes, else false.
     * @return Builder
     * @since 1.0.0
     */
    public Builder setAlternatives(Boolean alternatives) {
      this.alternatives = alternatives;
      return this;
    }

    /**
     * Optionally, set whether you want the route geometry to be full, simplified, etc.
     *
     * @param overview String defining type of overview you'd like the API to give. Use one of
     *                 the constants.
     * @return Builder
     * @since 1.0.0
     */
    public Builder setOverview(String overview) {
      this.overview = overview;
      return this;
    }

    /**
     * Optionally, set a radius values for the coordinates to allow for a more flexible origin
     * and destinations point locations.
     *
     * @param radiuses double array containing the radiuses
     * @return Builder
     */
    public Builder setRadiuses(double[] radiuses) {
      this.radiuses = radiuses;
      return this;
    }

    /**
     * Optionally, call if you'd like to include step information within route.
     *
     * @param steps true if you'd like step information.
     * @return Builder
     * @since 1.0.0
     */
    public Builder setSteps(Boolean steps) {
      this.steps = steps;
      return this;
    }

    /**
     * Toggle whether you want the route to always continue straight toward the next destination
     * or allow for backtracking.
     *
     * @param continueStraight boolean true if you want to always continue straight, else false.
     * @return Builder
     * @since 1.0.0
     */
    public Builder setContinueStraight(Boolean continueStraight) {
      this.continueStraight = continueStraight;
      return this;
    }

    /*
     * Getters, they return the value in a format ready for the API to consume
     */

    /**
     * @return the user as String
     * @since 1.0.0
     */
    public String getUser() {
      return user;
    }

    /**
     * @return {@link com.mapbox.services.directions.v4.DirectionsCriteria#PROFILE_DRIVING},
     * {@link com.mapbox.services.directions.v4.DirectionsCriteria#PROFILE_CYCLING},
     * or {@link com.mapbox.services.directions.v4.DirectionsCriteria#PROFILE_WALKING}
     * @since 1.0.0
     */
    public String getProfile() {
      return profile;
    }

    /**
     * The coordinates parameter denotes between which points routing happens. The coordinates
     * must be in the format:
     * <p>
     * {longitude},{latitude};{longitude},{latitude}[;{longitude},{latitude} ...]
     * <p>
     * - Each coordinate is a pair of a longitude float and latitude float, which are separated by a ,
     * - Coordinates are separated by a ; from each other
     * - A query must at minimum have 2 coordinates and may at maximum have 25 coordinates
     *
     * @return String containing coordinates formatted.
     * @since 1.0.0
     */
    public String getCoordinates() {
      List<String> coordinatesFormatted = new ArrayList<>();
      for (Position coordinate : coordinates) {
        coordinatesFormatted.add(String.format(Locale.US, "%f,%f",
          coordinate.getLongitude(),
          coordinate.getLatitude()));
      }

      return TextUtils.join(";", coordinatesFormatted.toArray());
    }

    /**
     * @return your Mapbox access token.
     * @since 1.0.0
     */
    @Override
    public String getAccessToken() {
      return accessToken;
    }

    /**
     * @return true if you {@link #setAlternatives(Boolean)} to true.
     * @since 1.0.0
     */
    public Boolean isAlternatives() {
      return alternatives;
    }

    /**
     * @return {@link com.mapbox.services.directions.v4.DirectionsCriteria#GEOMETRY_GEOJSON},
     * {@link com.mapbox.services.directions.v4.DirectionsCriteria#GEOMETRY_POLYLINE},
     * or {@link com.mapbox.services.directions.v4.DirectionsCriteria#GEOMETRY_FALSE}
     * @since 1.0.0
     */
    public String getGeometries() {
      return geometries;
    }

    /**
     * @return The overview, full, simplied, etc.
     * @since 1.0.0
     */
    public String getOverview() {
      return overview;
    }

    /**
     * Radiuses indicate how far from a coordinate a routeable way is searched. They
     * are indicated like this:
     * <p>
     * ?radiuses={radius};{radius}}[;{radius} ...].
     * <p>
     * If no routeble way can be found within the serach radius, a NoRoute error will be returned.
     * - Radiuses are separated by a ,
     * - Each radius must be of a value {@code float >= 0} in meters or unlimited (default)
     * - There must be as many radiuses as there are coordinates
     * - It is possible to not specify radiuses via ;;, which result in the same behaviour as setting unlimited
     *
     * @return String containing formatted radiuses.
     * @since 1.0.0
     */
    public String getRadiuses() {
      if (radiuses == null || radiuses.length == 0) {
        return null;
      }

      String[] radiusesFormatted = new String[radiuses.length];
      for (int i = 0; i < radiuses.length; i++) {
        radiusesFormatted[i] = String.format(Locale.US, "%f", radiuses[i]);
      }

      return TextUtils.join(";", radiusesFormatted);
    }

    /**
     * @return true if you requested step information in {@link #setSteps(Boolean)}.
     * @since 1.0.0
     */
    public Boolean isSteps() {
      return steps;
    }

    /**
     * Determine whether the route's been set to continue straight or not.
     *
     * @return true if continuing straight, otherwise false.
     * @since 1.0.0
     */
    public Boolean isContinueStraight() {
      return continueStraight;
    }

    public Builder setClientAppName(String appName) {
      super.clientAppName = appName;
      return this;
    }

    /**
     * Build method
     *
     * @return MapboxDirections
     * @throws ServicesException Generic Exception for all things directions.
     * @since 1.0.0
     */
    @Override
    public MapboxDirections build() throws ServicesException {
      validateAccessToken(accessToken);

      if (coordinates == null || coordinates.size() < 2) {
        throw new ServicesException(
          "You should provide at least two coordinates (from/to).");
      }

      if (radiuses != null && radiuses.length != coordinates.size()) {
        throw new ServicesException(
          "There must be as many radiuses as there are coordinates.");
      }

      return new MapboxDirections(this);
    }

  }

}
