package ca.ualberta.ridr;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import android.location.Location;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * This view allows for a rider to create a new request. Allows Rider to also see his requests and
 * rides from the menu option. Implements google maps methods, as well as ACallback.
 */
public class RiderMainView extends FragmentActivity implements ACallback, OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener{


    private EditText fareInput;


    private TextView dateTextView;
    private TextView timeTextView;

    private Button addRequest;
    private Button dateButton;
    private Button timeButton;
    private Button menuButton;

    private PlaceAutocompleteFragment pickupAutocompleteFragment;
    private PlaceAutocompleteFragment dropoffAutocompleteFragment;

    private String riderName; // string of the current UUID
    private Rider currentRider;

    private String defaultStartText = "Enter Pick Up Location";
    private String defaultDestinationText = "Enter Destination";
    private String defaultFareText = "Enter a Fare";

    private GoogleMap gMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng lastKnownPlace;
    private boolean firstLoad;
    private ArrayList<Marker> markers;
    private Marker startMarker;
    private Marker endMarker;
    private Geocoder geocoder;
    private final int MY_PERMISSION_ACCESS_FINE_LOCATION = 3;

    private Context context = this;
    private LatLng pickupCoord;
    private LatLng dropoffCoord;
    private String pickupStr;
    private String dropoffStr;

    private float distance;
    private float fare;


    private RequestController reqController;
    private RiderController riderController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rider_main);
        reqController = new RequestController(this, context);
        riderController = new RiderController(context);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.newRequestMap);
        mapFragment.getMapAsync(this);
        firstLoad = false;
        geocoder = new Geocoder(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        //retrieve the current rider's name
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            riderName = extras.getString("username");
        }

        setViews();

        // autocomplete for the pick up location
        pickupAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                pickupStr = place.getAddress().toString();
                pickupCoord = place.getLatLng();
                addMarkers(pickupCoord, true);
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupCoord, 11));
                if(dropoffCoord != null){
                    estimateFare(pickupCoord, dropoffCoord);
                }
            }
            @Override
            public void onError(Status status) {
                Log.i("Places", "An error occurred: " + status);
            }
        });

        //autocomplete for the drop off location
        dropoffAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                dropoffStr = place.getAddress().toString();
                dropoffCoord = place.getLatLng();
                addMarkers(dropoffCoord, false);
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dropoffCoord, 11));
                if(pickupCoord != null){
                    estimateFare(pickupCoord, dropoffCoord);
                }

            }

            @Override
            public void onError(Status status) {
                Log.i("Places", "An error occurred: " + status);
            }
        });

        pickupAutocompleteFragment.getView().findViewById(R.id.pickup_autocomplete_fragment).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primary_colour, null));
        dropoffAutocompleteFragment.getView().findViewById(R.id.dropoff_autocomplete_fragment).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primary_colour, null));
        //open date picker
        dateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //makes a date fragment when clicked
                DialogFragment frag = new DateSelector();
                frag.show(getFragmentManager(), "DatePicker");
            }
        });

        //open time picker
        timeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // make a date fragment when clicked
                DialogFragment frag = new TimeSelector();
                frag.show(getFragmentManager(), "TimePicker");
            }
        });

        // create request button
        addRequest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Rider rider = null; // for now just so that we wont get compile errors
                System.out.println(new Gson().toJson(currentRider));
                addRequestEvent(currentRider);
                //Executes any pending functions from offline functionality once online
                reqController.executeAllPending(riderName);
            }
        });

        //menu button
        menuButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                showMenu(v);
            }
        });

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

        //from the name, get the rider object
        //we want this in onStart, because we want to pull notification every time we go back to the activity
        try {
            currentRider = new Gson().fromJson(new AsyncController(context).get("user", "name", riderName), Rider.class);
        } catch(Exception e){
            Log.i("Error parsing Rider", e.toString());
        }

        //check for notifications, display
        try {
            if (currentRider.getPendingNotification() != null) {
                Toast.makeText(this, currentRider.getPendingNotification(), Toast.LENGTH_LONG).show();
                currentRider.setPendingNotification(null);
                //update the user object in the database
                try {
                    AsyncController asyncController = new AsyncController(context);
                    asyncController.create("user", currentRider.getID().toString(), new Gson().toJson(currentRider));
                    //successful account updating
                } catch (Exception e) {
                    Log.i("Communication Error", "Could not communicate with the elastic search server");
                }
            }
        } catch(Exception e) {
            Log.i("error check notify", e.toString());
        }

        //Executes any pending functions from offline functionality once online
        reqController.executeAllPending(riderName);
        riderController.pushPendingNotifications();
    }
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.reconnect();
    }

    protected void onPause(){
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    // Need this for ConnectionsCallback, doesn't need to do anything AFAIK
    // If a map view does live tracking it might be more useful
    public void onConnectionSuspended(int i){

    }

    @Override
    //On connected listener, required to be able to zoom to users location at login
    public void onConnected(Bundle connectionHint){
        lastKnownPlace = getCurrentLocation();
        //Toast.makeText(RiderMainView.this, "connected!", Toast.LENGTH_SHORT).show();

        if(lastKnownPlace != null && !firstLoad) {
            firstLoad = true;
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownPlace, 12));
        }

    }

    // This should eventually be updated to quit the app or go back to a view that doesn't require geolocation
    // Currently this shows an alert notifying the user that the connection failed
    public void onConnectionFailed(ConnectionResult result) {
        //Toast.makeText(RiderMainView.this, "connection failed", Toast.LENGTH_SHORT).show();
        new AlertDialog.Builder(this)
                .setTitle("Connection Failure")
                .setMessage(result.getErrorMessage())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        gMap = googleMap;
        // Allow the user to go home at any time

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_PERMISSION_ACCESS_FINE_LOCATION );
        } else {
            //we have permission
            gMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    gMap.setMyLocationEnabled(true);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    public void update(){}

    /**
     * finds views by their ID's and assigns them to their respective variable
     */
    private void setViews(){
        fareInput = (EditText) findViewById(R.id.editFare);

        dateTextView = (TextView) findViewById(R.id.dateText);
        timeTextView = (TextView) findViewById(R.id.timeText);

        addRequest = (Button) findViewById(R.id.createRequestButton);
        dateButton = (Button) findViewById(R.id.dateButton);
        timeButton = (Button) findViewById(R.id.timeButton);
        menuButton = (Button) findViewById(R.id.riderMainMenuButton);

        pickupAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.pickup_autocomplete_fragment);
        ((EditText)pickupAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHint(defaultStartText);

        dropoffAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.dropoff_autocomplete_fragment);
        ((EditText)dropoffAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHint(defaultDestinationText);
    }

    /**
     * reset text inputs in the view
     */
    private void resetText(){
        ((EditText)pickupAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");
        ((EditText)dropoffAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");
        dateTextView.setText("");
        timeTextView.setText("");
        fareInput.setText("");
    }

    /**
     * handles the event called when Create Request is clicked. Verifies that the fields have been filled and creates a Request
     * @param rider
     */
    private void addRequestEvent(Rider rider){

        if(pickupCoord == null){
            Toast.makeText(RiderMainView.this, "Please enter the address from where you would like to be picked up", Toast.LENGTH_SHORT).show();
            return;
        }
        if(dropoffCoord == null){
            Toast.makeText(RiderMainView.this, "Please enter the address of your destination", Toast.LENGTH_SHORT).show();
            return;
        }
        if(dateTextView.getText().toString().matches("")){
            Toast.makeText(RiderMainView.this, "Please enter the date on which you would like to be picked up", Toast.LENGTH_SHORT).show();
            return;
        }
        if(timeTextView.getText().toString().matches("")){
            Toast.makeText(RiderMainView.this, "Please enter the time at which you would like to be picked up", Toast.LENGTH_SHORT).show();
            return;
        }
        if(fareInput.getText().toString().matches("") || fareInput.getText().toString().matches(defaultFareText)){
            Toast.makeText(RiderMainView.this, "Please enter a fare", Toast.LENGTH_SHORT).show();
            return;
        }
        Date pickupDate = stringToDate(dateTextView.getText().toString(), timeTextView.getText().toString());

        reqController.executeAllPending(riderName);


        fare = Float.parseFloat(fareInput.getText().toString());
        float costDist = fare/distance;
        costDist = roundFloatToTwoDec(costDist);
        fare = roundFloatToTwoDec(fare);
        reqController.createRequest(rider, pickupStr, dropoffStr, pickupCoord, dropoffCoord, pickupDate,fare, costDist);
        Toast.makeText(RiderMainView.this, "You request has been made", Toast.LENGTH_SHORT).show();

        // reset text fields
        resetText();
        //clear map of markers
        gMap.clear();

        //reset coordinates
        pickupCoord = null;
        dropoffCoord = null;

    }

    /**
     * popup menu that allows the user to edit their profile and view all of their requests
     * @param v
     */
    public void showMenu(View v){
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.rider_main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            public boolean onMenuItemClick(MenuItem item){
                switch(item.getItemId()){
                    case R.id.mainRiderMenuEditUserInfo:
                        resetText();
                        Intent editInfoIntent = new Intent(RiderMainView.this, EditProfileView.class);
                        editInfoIntent.putExtra("Name", riderName);
                        startActivity(editInfoIntent);
                        return true;
                    case R.id.mainRiderMenuViewRequests:
                        resetText();
                        Intent viewRequestsIntent = new Intent(RiderMainView.this, RiderRequestView.class);
                        viewRequestsIntent.putExtra("Name", riderName);
                        startActivity(viewRequestsIntent);
                        return true;
                    case R.id.mainRiderMenuViewRides:
                        resetText();
                        Intent viewRidesIntent = new Intent(RiderMainView.this, RiderRidesView.class);
                        viewRidesIntent.putExtra("Name", riderName);
                        startActivity(viewRidesIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }



    /**
     * converts data and time strings into a Date object
     * @param dateString string with format dd/MM/yyyy
     * @param timeString string with format hit h:mm a, where hh is the time from 1-12 and a is an am/pm indicator
     * @return a date object with the format dd/MM/yyyy hh:mm a
     */
    private Date stringToDate(String dateString, String timeString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        try{
            return dateFormat.parse(dateString +" "+ timeString);
        } catch(ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    /**
     * When called this function checks uses LocationServices to grab the lastLocation and returns
     * that LatLng to the caller
     * @nullable
     * @return currentLocation
     */
    // Simple function to grab current location in LatLong
    private LatLng getCurrentLocation(){

        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(currentLocation != null) {
            return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        //Toast.makeText(RiderMainView.this, "failed to get current location", Toast.LENGTH_SHORT).show();
        return null;
    }

    /**
     * Add markers to the map.
     *
     * @param coords coordinates of the marker
     * @param isStart true if it is the start location marker, false if it is the destination
     */
    public void addMarkers(LatLng coords, Boolean isStart){
        if(isStart){
            if(startMarker != null){
                startMarker.remove();
            }
            startMarker = gMap.addMarker((new MarkerOptions().position(coords).title("Pick up")));
        } else{
            if(endMarker != null){
                endMarker.remove();
            }
            endMarker = gMap.addMarker((new MarkerOptions().position(coords).title("Destination")));
        }
    }

    /**
     * estimate a fare based on the distance between two locations
     * @param pickup coordinates of the pick up address
     * @param dropoff coordinates of the drop off address
     */
    private void estimateFare(LatLng pickup, LatLng dropoff){
        float[] results = new float[1];
        Location.distanceBetween(pickup.latitude, pickup.longitude,
                dropoff.latitude, dropoff.longitude, results);
        //Toast.makeText(RiderMainView.this, "distance is " + Float.toString(results[0]), Toast.LENGTH_SHORT).show();
        distance = results[0] / 1000; // in KM
        float gasCostFactor = 2; // calculate something later
        fare =  distance *gasCostFactor;
        //fareInput.setText((String.format("%.2f", fare)));
        fareInput.setText(String.valueOf(roundFloatToTwoDec(fare)));
    }

    private float roundFloatToTwoDec(float number){
        BigDecimal dec = new BigDecimal(Float.toString(number));
        dec = dec.setScale(2, BigDecimal.ROUND_HALF_UP);
        return dec.floatValue();
    }


}
