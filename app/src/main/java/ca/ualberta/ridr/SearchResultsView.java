package ca.ualberta.ridr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import io.searchbox.core.SearchResult;

/**
 * The view that shows keyword search results for a driver
 * Also allows new keyword searches to be performed
 */
public class SearchResultsView extends Activity {

    private ArrayList<Request> requestList = new ArrayList<>();
    final RequestController requestController = new RequestController();
    private String keyword;
    private ListView searchResults;
    private RequestAdapter requestAdapter;
    private EditText bodyText;
    private Button mainMenu;
    private UUID userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results_driver);

        Intent intent= getIntent();
        Bundle extras = intent.getExtras();
        if(extras!=null)
        {
            userID = UUID.fromString(extras.getString("UUID"));
        }

        //main menu button
        mainMenu = (Button)findViewById(R.id.driverMainMenuButton);
        mainMenu.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                showMenu(v);
            }
        });

        searchResults = (ListView) findViewById(R.id.search_results);
        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //implement when putting together for on click items
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                /*Intent intent = new Intent(SearchResultsView.this, .class);
                intent.putExtra("Habit", position);
                startActivity(intent);*/
            }
        });

        bodyText = (EditText) findViewById(R.id.searchRequestsText);
        Button searchButton = (Button) findViewById(R.id.searchRequestsButton);

        searchButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String text = bodyText.getText().toString();
                searchResultsByKeyword(text);
                requestAdapter.notifyDataSetChanged();
            }
        });

        Button switchGeoButton = (Button) findViewById(R.id.searchByGeoButton);

        switchGeoButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String text = userID.toString();
                Intent intent = new Intent(SearchResultsView.this, GeoView.class);
                intent.putExtra("user", text);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestAdapter = new RequestAdapter(this, requestList);
        searchResults.setAdapter(requestAdapter);
    }

    /**
     * Calls on the request controller method searchRequestsKeyword
     * Updates the requestList without making it a new object (Clear and re add)
     * @param keyword
     */
    protected void searchResultsByKeyword(String keyword) {
        if(keyword != null) {
            ArrayList<Request> tempRequestList = requestController.searchRequestsKeyword(keyword);
            requestList.clear();
            requestList.addAll(tempRequestList);
        }
    }

    public void showMenu(View v){
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.rider_main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            public boolean onMenuItemClick(MenuItem item){
                switch(item.getItemId()){
                    case R.id.mainRiderMenuEditUserInfo:
                        Toast.makeText(SearchResultsView.this, "Edit User Info", Toast.LENGTH_SHORT).show();
                        Intent editInfoIntent = new Intent(SearchResultsView.this, EditProfileView.class);
                        editInfoIntent.putExtra("UUID", userID.toString());
                        startActivity(editInfoIntent);
                        return true;
                    case R.id.mainRiderMenuViewRequests:
                        Toast.makeText(SearchResultsView.this, "View Requests", Toast.LENGTH_SHORT).show();
                        Intent viewRequestsIntent = new Intent(SearchResultsView.this, RiderRequestView.class);
                        viewRequestsIntent.putExtra("UUID", userID.toString());
                        startActivity(viewRequestsIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }
}

