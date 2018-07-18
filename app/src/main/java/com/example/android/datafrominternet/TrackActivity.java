package com.example.android.datafrominternet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.datafrominternet.utilities.NetworkUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static android.view.View.VISIBLE;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button scanbtn;
    Button nsbtn;
    TextView result;
    TextView instruction;
    public String mChipCode;
    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;
    TextView mSearchResultsTextView;
    TextView errorMessageTextView;
    ProgressBar mLoadingIndicator;
    GoogleMap m_map;
    boolean mapReady=false;
    MarkerOptions mManufacturer;
    MarkerOptions mPackager;
    MarkerOptions mSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        scanbtn = (Button) findViewById(R.id.scanbtn);
        nsbtn = (Button) findViewById(R.id.nsbtn);
        result = (TextView) findViewById(R.id.result);
        instruction = (TextView) findViewById(R.id.instruction);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);
        errorMessageTextView = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
        }
        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrackActivity.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(TrackActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap map){
        mapReady=true;
        m_map = map;
        //LatLng IBMZRL = new LatLng(47.309565, 8.545062);
        //CameraPosition target = CameraPosition.builder().target(IBMZRL).zoom(10).build();
        //m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                final Barcode barcode = data.getParcelableExtra("barcode");
                result.post(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(barcode.displayValue);
                        String githubQuery = barcode.displayValue;
                        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
                        new TrackActivity.GithubQueryTask().execute(githubSearchUrl);
                    }
                });
            }
        }
    }


    private void showJsonDataView() {
        errorMessageTextView.setVisibility(View.INVISIBLE);
        mSearchResultsTextView.setVisibility(View.VISIBLE);
        instruction.setText("Chip information retrieved.");
    }

    private void showErrorMessage() {
        errorMessageTextView.setVisibility(View.VISIBLE);
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        instruction.setText("Chip information not found in database.");
    }

    //Look into the JSON gibberish for chip code

    public static String getChipInfo(String SearchResults, String Keyword){
        try {
            JSONObject JsonComplete = new JSONObject(SearchResults);
            return JsonComplete.getString(Keyword) ;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static LatLng getChipLatLng(String SearchResults, String Keyword){
        try {
            JSONObject JsonComplete = new JSONObject(SearchResults);
            String[] latlong =  JsonComplete.getString(Keyword).split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            LatLng location = new LatLng(latitude, longitude);
            return location;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getChipCode(String SearchResults){
        try {
            JSONObject JsonComplete = new JSONObject(SearchResults);
            return JsonComplete.getString("Chip Code") ;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public class GithubQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String githubSearchResults = null;
            try {
                githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return githubSearchResults;
        }

        @Override
        protected void onPostExecute(String SearchResults) {

            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (SearchResults != null && !SearchResults.equals("")) {
                showJsonDataView();
                mChipCode = getChipCode(SearchResults);
                mManufacturer = new MarkerOptions()
                                    .position(getChipLatLng(SearchResults, "Manufacturer Location"))
                                    .title("Manufacturer: " + getChipInfo(SearchResults, "Manufacturer Name"));
                m_map.addMarker(mManufacturer);
                mPackager = new MarkerOptions()
                        .position(getChipLatLng(SearchResults, "Packager Location"))
                        .title("Packager: " + getChipInfo(SearchResults, "Packager Name"));
                m_map.addMarker(mPackager);
                mSeller = new MarkerOptions()
                        .position(getChipLatLng(SearchResults, "Seller Location"))
                        .title("Seller: " + getChipInfo(SearchResults, "Seller Name"));
                m_map.addMarker(mSeller);
                mSearchResultsTextView.setText("Chip Type: " + getChipInfo(SearchResults, "Chip Type")+
                        "\nChip Code: " + mChipCode +
                        "\nManufacture Date: " + getChipInfo(SearchResults, "Manufacture Date")+
                        "\nManufacturer Name: " + getChipInfo(SearchResults, "Manufacturer Name")+
                        "\nManufacturer Location: " + getChipInfo(SearchResults, "Manufacturer Location")+
                        "\nPackage Date: " + getChipInfo(SearchResults, "Package Date")+
                        "\nPackager Name: " + getChipInfo(SearchResults, "Packager Name")+
                        "\nPackager Location: " + getChipInfo(SearchResults, "Packager Location")+
                        "\nSale Date: " + getChipInfo(SearchResults, "Sale Date")+
                        "\nSeller Name: " + getChipInfo(SearchResults, "Seller Name")+
                        "\nSeller Location: " + getChipInfo(SearchResults, "Seller Location"));
            } else{
                showErrorMessage();
            }
        }
    }
}
