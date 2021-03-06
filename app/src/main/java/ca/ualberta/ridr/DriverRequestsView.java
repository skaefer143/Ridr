package ca.ualberta.ridr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * This view displays requests when logged in as a Driver. Implements ACallback, which uses the update()
 * method to update the rider's request list.
 */
public class DriverRequestsView extends Activity implements ACallback{

    private String userName;
    private ArrayList<Request> requests = new ArrayList<>();
    private ListView requestList;
    private Context context = this;
    private RequestController requestController = new RequestController(context);
    private RiderController riderController = new RiderController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requests_from_riders);

        requestList = (ListView)findViewById(R.id.requests_from_riders_list);

        Intent intent= getIntent();
        Bundle extras = intent.getExtras();
        if(extras!=null)
        {
            userName = extras.getString("Name");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(requests != null){
            requests.clear();
        }

        //Executes any pending functions from offline functionality once online
        requestController.executeAllPending(userName);
        riderController.pushPendingNotifications();

        //We need to get the list of requests that has this drivers UUID in their possibleDrivers list
        requestController = new RequestController(this, context);
        requestController.findAllRequestsWithDataMember("request", "possibleDrivers", userName);
        //search for our name in any possibleDriver list
        //update gets called from Acallback


        RequestAdapter customAdapter = new RequestAdapter(DriverRequestsView.this, requests, userName);
        requestList.setAdapter(customAdapter);

        //this is to recognize listview item presses within the view
        requestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Request request = requests.get(position);
                String clickedRequestIDStr = request.getID().toString();
                Intent intent = new Intent(DriverRequestsView.this, AcceptRiderView.class);
                intent.putExtra("RequestUUID", clickedRequestIDStr);
                intent.putExtra("username", userName);
                startActivity(intent);
            }
        });

        if(requests.size() <= 0){
            Toast.makeText(DriverRequestsView.this, "You haven't accepted any requests yet!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    //for Acallback
    public void update() {
        requests.clear();
        ArrayList<Request> requestControllerList = requestController.getList();
        for (int i = 0; i < requestControllerList.size(); ++i){
            if(requestControllerList.get(i).isValid() && !requestControllerList.get(i).isAccepted()){
                //add to our request list, if the request is a valid request (not cancelled),
                // and not accepted by anybody else
                requests.add(requestControllerList.get(i));
            }
        }
    }
}
