package ca.ualberta.ridr;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by jferris on 22/10/16.
 */
public class RideController {
    ArrayList<Ride> rides;
    ACallback cb;
    AsyncController asyncController;
    RideController(){}
    RideController(ACallback callback){
        asyncController = new AsyncController();
        this.cb = callback;
        this.rides = new ArrayList<Ride>();
    }

    public void createRide(String driverId, Request request, String riderId) {
        //will need to replace the date I guess with actual date that ride is supposed to occur
        Ride ride = new Ride(driverId, riderId,  request.getPickup(), request.getDropoff(), new Date() , request.getPickupCoords(), request.getDropOffCoords());

        //rider.confirmDriver(ride);
        //another cant do while the ride array list of rider is null

        String rideString = ride.toJsonString();
        AsyncController con = new AsyncController();
        JsonObject s = con.create("ride",ride.getId().toString(), rideString);

    }

    @Nullable
    public Ride getRide(String id){
        for(int i=0; i<rides.size(); ++i){
            Ride ride = rides.get(0);
            Log.i("Ride", ride.getId().toString());
            Log.i("Ride", id);
            if(ride.getId().equals(UUID.fromString(id))){
                Log.i("Ridish", ride.toJsonString());
                return ride;
            }
        }
        return null;
    }

    public void findRide(String rideID) {
        JsonObject ride = asyncController.get("ride", "id", rideID);
        Log.i("Printing ride", ride.toString());
        try {
            Ride aRide = new Ride(ride);
            Log.i("Printing ride", ride.toString());
            rides.add(new Ride(ride));
            cb.update();
        } catch (Exception e){
            Log.i("Failed to make ride", String.valueOf(e));
        }
    }

}
