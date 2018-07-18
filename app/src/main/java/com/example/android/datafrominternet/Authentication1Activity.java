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
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static android.view.View.VISIBLE;

public class Authentication1Activity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication1);
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
                Intent intent = new Intent(Authentication1Activity.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        nsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Intent = new Intent (Authentication1Activity.this, Authentication2Activity.class);
                Intent.putExtra("ChipCode", mChipCode);
                startActivity(Intent);
            }
        });

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
                        new Authentication1Activity.GithubQueryTask().execute(githubSearchUrl);
                    }
                });
            }
        }
    }


    private void showJsonDataView() {
        errorMessageTextView.setVisibility(View.INVISIBLE);
        mSearchResultsTextView.setVisibility(View.VISIBLE);
        instruction.setText("Chip information retrieved. Please verify, and continue by pressing NEXT STEP.");
        nsbtn.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        errorMessageTextView.setVisibility(View.VISIBLE);
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        instruction.setText("Chip information not found in database. Authentication can not continue.");
        nsbtn.setVisibility(View.INVISIBLE);
    }

    //Look into the JSON gibberish for chip code
    public static String getChipCode(String SearchResults){
        try {
            JSONObject JsonComplete = new JSONObject(SearchResults);
            return JsonComplete.getString("Meal") ;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getChipType(String SearchResults){
        try {
            JSONObject JsonComplete = new JSONObject(SearchResults);
            return JsonComplete.getString("Chip Type") ;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getChipDate(String SearchResults){
        try {
            JSONObject JsonComplete = new JSONObject(SearchResults);
            return JsonComplete.getString("Manufacture Date") ;
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
        protected void onPostExecute(String githubSearchResults) {

            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (githubSearchResults != null && !githubSearchResults.equals("")) {
                showJsonDataView();
                mChipCode = getChipCode(githubSearchResults);
                mSearchResultsTextView.setText("Chip Type: " + getChipType(githubSearchResults)+
                        "\nManufacture Date: " + getChipDate(githubSearchResults)+
                        "\nChip Code: " + mChipCode);
            } else{
                showErrorMessage();
            }
        }
    }

}
