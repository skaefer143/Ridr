package ca.ualberta.ridr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchRequestsView extends AppCompatActivity {

    private ArrayList<Request> requestList = new ArrayList<Request>();
    private ArrayAdapter<Request> adapter;
    final RequestController requestController = new RequestController();
    private TextView bodyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_requests);

        bodyText = (EditText) findViewById(R.id.searchRequestsText);
        Button searchButton = (Button) findViewById(R.id.searchRequestsButton);

        searchButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String text = bodyText.getText().toString();
                RequestController.SearchRequestsKeyword searchRequestsKeyword = new RequestController.SearchRequestsKeyword();
                searchRequestsKeyword.execute(text);
                try {
                    requestList = searchRequestsKeyword.get();
                }
                catch (Exception e) {
                    Log.i("Error", "Failed to get the requests out of the async object.");
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
