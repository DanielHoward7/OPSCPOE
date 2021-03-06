package com.dan.admin.opscpoe;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, OnPolylineClickListener {

    private static final String TAG = "DEBUGZ";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    //    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 1235;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final LatLngBounds latlngbounds = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));


    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private UserLocation userLocation;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fsDb;
    private AutoCompleteTextView searchET;
    private GoogleMap map;
    private ImageView gps;
    private ImageView info;
    private ImageView walk;
    private ImageView car;
    private ImageView pub;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceDetails mPlace;
    private Marker marker;
    private GeoApiContext geoApiContext = null;
    private ArrayList<UserLocation> userLocationArrayList = new ArrayList<>();
    private List<com.google.maps.model.LatLng> decodedPath;
    private List<LatLng> newDecodedPath;
    private ArrayList<Polylines> polyLines = new ArrayList<>();
    private Marker selectedMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        fsDb = FirebaseFirestore.getInstance();

        searchET = (AutoCompleteTextView) findViewById(R.id.input_search);
        gps = (ImageView) findViewById(R.id.ic_loc);
        info = (ImageView) findViewById(R.id.ic_info);
        walk = (ImageView) findViewById(R.id.ic_walk);
        car = (ImageView) findViewById(R.id.ic_car);
        pub = (ImageView) findViewById(R.id.ic_pub);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, 0, this)
                .build();

        getLocationPermission();
        checkMapServices();
        displayMap();
        getProfileDetails();
        setUserLocation();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        map = googleMap;
        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setOnInfoWindowClickListener(this);
            map.setOnPolylineClickListener(this);
            init();


        }
    }

    private void displayMap() {
        Log.d(TAG, "displayMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void init() {
        Log.d(TAG, "init: initializing");

        searchET.setOnItemClickListener(autocompleteAdapter);

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                latlngbounds, null);

        searchET.setAdapter(placeAutocompleteAdapter);

        searchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    locate();
                }

                return false;
            }
        });

        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                    } else {
                        marker.showInfoWindow();
                    }
                } catch (NullPointerException e) {
                    e.getMessage();
                }
            }
        });

        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Walk selected");
                Toast.makeText(MapActivity.this, "Walking", Toast.LENGTH_SHORT).show();

                userLocation.getProfile().setMode("Walk");
            }
        });

        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Car selected");
                Toast.makeText(MapActivity.this, "Driving", Toast.LENGTH_SHORT).show();

                userLocation.getProfile().setMode("Car");


            }
        });

        pub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Public transport selected");
                Toast.makeText(MapActivity.this, "Using Public Transport", Toast.LENGTH_SHORT).show();

                userLocation.getProfile().setMode("Public Transport");

            }
        });

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.api_key)).build();
        }

    }

    private void setUserLocation() {
        for (UserLocation userPosition : userLocationArrayList) {
            if (userPosition.getProfile().getUser_id().equals(FirebaseAuth.getInstance().getUid())) {
                userLocation = userPosition;
            }
        }
    }

    private void locate() {

        String search = searchET.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(search, 1);
        } catch (IOException e) {
            Log.e(TAG, "geolocate error: " + e.getMessage());
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "locate: found a location: " + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), 13, address.getAddressLine(0));

        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceDetails details) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        map.clear();

        if (details != null) {
            try {
                String snippet = "Address: " + details.getAddress() + "\n" +
                        "Phone Number: " + details.getPhoneNumber() + "\n" +
                        "Website: " + details.getWebsiteUri() + "\n" +
                        "Price Rating: " + details.getRating() + "\n" +
                        "Do you want directions to this place?";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(details.getName())
                        .snippet(snippet);
                marker = map.addMarker(options);

            } catch (NullPointerException e) {
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage());
            }
        } else {
            map.addMarker(new MarkerOptions().position(latLng));
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        map.addMarker(options);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                GeoPoint geoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

                                userLocation.setGeoPoint(geoPoint);

                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13, "My location");
                            }else{
                                Log.d(TAG, "onComplete: current location is null");
                                Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }

    }

    private void calculateDirections(Marker marker) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(

                        userLocation.getGeoPoint().getLatitude(),
                        userLocation.getGeoPoint().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());

        Log.d(TAG, "calculateDirections: DEPART: " + userLocation.getGeoPoint());


        directions.destination(destination).setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "onResult: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                mapRoute(result);

                GeoPoint location = userLocation.getGeoPoint();
                Log.d(TAG, "location: " + location);
                Profile profile = userLocation.getProfile();
                Log.d(TAG, "profile: " + profile);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                DocumentReference ref = db.collection("User Locations").document();

                UserLocation user = new UserLocation();
                user.setGeoPoint(location);
                user.setProfile(profile);

                ref.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "user posted");
                        }else{
                            Log.d(TAG, "user did not post");
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage());

            }
        });

    }

    private void getProfileDetails() {

        if (userLocation == null) {
            userLocation = new UserLocation();

            DocumentReference reference = fsDb.collection(getString(R.string.user_collection))
                    .document(FirebaseAuth.getInstance().getUid());

            reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the user profile.");
                        Profile profile = task.getResult().toObject(Profile.class);
                        userLocation.setProfile(profile);
                        ((UserProfile) getApplicationContext()).setProfile(profile);

                        Log.d(TAG, "getProfileDetails: worked");
//                        saveUserLocation();
                    }
                }
            });
        } else {
//            saveUserLocation();
            Log.d(TAG, "getProfileDetails: already a user");
        }
    }

    private void mapRoute(final DirectionsResult directionsResult) {
        Log.d(TAG, "mapRoute: got here");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + directionsResult.routes.length);

                double time = 9999999;

                for (int i = 0; i < directionsResult.routes.length; i++) {
                    DirectionsRoute route = directionsResult.routes[i];
                    for (int j = 0; j < route.legs.length; j++) {
                        DirectionsLeg leg = route.legs[j];
                    }
                    decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    newDecodedPath = new ArrayList<>();

                    for (com.google.maps.model.LatLng latLng : decodedPath) {
                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                    }

                    Polyline polyline = map.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                    polyline.setClickable(true);
                    polyLines.add(new Polylines(polyline, route.legs[0]));

                    double duration = route.legs[0].duration.inSeconds;
                    if (duration < time){
                        time = duration;
                        onPolylineClick(polyline);

                    }
                    selectedMarker.setVisible(false);
                }
            }
        });
    }


    private void saveUserLocation() {
        Log.d(TAG, "saveUserLocation");

        if (userLocation != null) {
            DocumentReference reference = fsDb.collection(getString(R.string.user_locations))
                    .document(mAuth.getUid());

            reference.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + userLocation.getGeoPoint().getLatitude() +
                                "\n longitude: " + userLocation.getGeoPoint().getLongitude());
                    }
                }
            });
        }
    }

//    private void getLastLocation() {
//        Log.d(TAG, "getLastKnownLocation: called.");
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
//            @Override
//            public void onComplete(@NonNull Task<android.location.Location> task) {
//                Log.d(TAG, "OnComplete: task successful");
//
//                Location location = task.getResult();
//                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
//                userLocation.setGeoPoint(geoPoint);
//                Log.d(TAG, "OnComplete: lat:" + geoPoint.getLatitude());
//                Log.d(TAG, "OnComplete: long:" + geoPoint.getLongitude());
//
//                userLocation.setGeoPoint(geoPoint);
////                saveUserLocation();
//            }
//        });
//
//    }

    private boolean checkMapServices(){
        if(isServicesUpToDate()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        Bundle mapVBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
//        if (mapVBundle == null){
//            mapVBundle = new Bundle();
//            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapVBundle);
//        }
//        mapView.onSaveInstanceState(mapVBundle);
//    }

      public boolean isServicesUpToDate(){
        Log.d(TAG, "isServicesUpToDate: checking google services version" );

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if (available == ConnectionResult.SUCCESS){
            //user is able to use maps
            Log.d(TAG, "isServicesUpToDate: Google play services is up to date");
            return true;
        }else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //needs to update
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this,"You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoConnection();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoConnection() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS required.. please enable gps to use this service")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
//        isMapsEnabled();
    }

    private void getLocationPermission(){

        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                displayMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    displayMap();

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionsGranted){
                    getProfileDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuMap:

                startActivity(new Intent(this, MapActivity.class));

            case R.id.menuLogout:

                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
        }
        return true;
    }

    //Google places api autocomplete stuff

    private AdapterView.OnItemClickListener autocompleteAdapter = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceDetails();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), 13, mPlace);

            places.release();
        }
    };

    @Override
    public void onInfoWindowClick(final Marker marker) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(marker.getSnippet())
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            Log.e(TAG,"calculateDirections");
                            selectedMarker = marker;
                            calculateDirections(marker);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for (Polylines polylineData : polyLines){
            index++;
            Log.e(TAG,"onPolyLineCLick : " + polyline.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
                polylineData.getPolyline().setZIndex(1);


                LatLng destination = new LatLng(polylineData.getLeg().endLocation.lat, polylineData.getLeg().endLocation.lng);

                Marker marker = map.addMarker(new MarkerOptions().position(destination)
                        .title("Trip: #" +index)
                        .snippet("Duration: " + polylineData.getLeg().duration + " and "  + "Distance: "+ polylineData.getLeg().distance));

                marker.showInfoWindow();

            }else{
                polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                polyline.setZIndex(0);
            }
        }
    }
}
