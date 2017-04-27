package com.inventory_tracker.company_name.eventinventorytracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class DatabaseSyncActivity extends AppCompatActivity {

    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonDynamoDBClient dynamoDBClient;
    DynamoDBMapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.utility_database_sync);
    }

    private void createCredentialsProvider(){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), //context
                "IDENTITY_POOL_ID",
                Regions.US_EAST_1
        );

    }

    private void createddbClient(){
        dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

    }

    private void createDBMapper(){
        mapper = new DynamoDBMapper(dynamoDBClient);
    }

}
