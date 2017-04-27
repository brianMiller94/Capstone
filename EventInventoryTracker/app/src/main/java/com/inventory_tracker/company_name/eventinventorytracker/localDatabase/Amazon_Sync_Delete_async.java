package com.inventory_tracker.company_name.eventinventorytracker.localDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_View;

/**
 * Created by brian on 4/19/2017.
 */

public class Amazon_Sync_Delete_async extends AsyncTask<Combo_Context_Object,Integer,String> {

    private static String TAG ="Amazon_Async_Sync";

    private Context applicationContext;
    private Object object;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper mapper;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Combo_Context_Object... params) {
        try{
            applicationContext = params[0].getContext();
            object = params[0].getObject();
            //Prepare amazon classes for use
            prepareCredentialProvider();
            prepareAmazonDatabaseClient();
            prepareAmazonDatabaseMapper();

            mapper.delete(object);

        }catch (Exception e){
            cancel(true);
        }
        return null;
    }

    private void prepareCredentialProvider(){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                applicationContext.getApplicationContext(),
                "[AMAZON_COGNITO_IDENTITY]",
                Regions.US_WEST_2
        );

        Log.v(TAG,"My Cognito ID is " + credentialsProvider.getIdentityId());
    }

    private void prepareAmazonDatabaseClient(){
        dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_WEST_2));
    }

    private void prepareAmazonDatabaseMapper(){
        mapper = new DynamoDBMapper(dynamoDBClient);
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Toast.makeText(applicationContext, "Something Went Wrong with Syncing", Toast.LENGTH_SHORT).show();
    }
}
