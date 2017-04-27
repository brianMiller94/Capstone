package com.inventory_tracker.company_name.eventinventorytracker.localDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_View;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundles_Have_Accessories_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Accessories_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Bundles_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Items_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Accessories;

import java.util.List;

/**
 * Created by brian on 4/18/2017.
 */

public class Amazon_Sync_async extends AsyncTask<Combo_Context_View,Integer,String> {

    private static String TAG ="Amazon_Async_Sync";

    private Context applicationContext;
    private View applicationView;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper mapper;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Combo_Context_View... params) {
        //Get Context
        applicationContext = params[0].getContext();
        applicationView = params[0].getProgressBar();

        //Prepare amazon classes for use
        prepareCredentialProvider();
        prepareAmazonDatabaseClient();
        prepareAmazonDatabaseMapper();

        //publish progress of 4%
        publishProgress(4);

        //Sync Items table
        try {
            syncItemTable();
            publishProgress(16);
        } catch (Exception e) {
            cancel(true);
        }

        try {
            syncBundlesHaveAccessoriesTable();
            publishProgress(28);
        } catch (Exception e) {
            cancel(true);
        }

        try {
            syncAccessoryTable();
            publishProgress(40);
        } catch (Exception e) {
            cancel(true);
        }

        try {
            syncEventTable();
            publishProgress(52);
        } catch (Exception e) {
            cancel(true);
        }

        try {
            syncEventsHaveAccessoriesTable();
            publishProgress(64);
        } catch (Exception e) {
            cancel(true);
        }

        try {
            syncEventsHaveBundlesTable();
            publishProgress(76);
        } catch (Exception e) {
            cancel(true);
        }

        try {
            syncEventsHaveItemsTable();
            publishProgress(88);
        } catch (Exception e) {
            cancel(true);
        }

        try {
            syncBundleTable();
            publishProgress(100);
        } catch (Exception e) {
            cancel(true);
        }

        return null;
    }

    private void prepareCredentialProvider(){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                applicationContext.getApplicationContext(),
                "[AMAZON_COGNITO_CREDENTIALS",
                Regions.US_WEST_2
        );

        Log.v(TAG,"My Cognito ID is " + credentialsProvider.getIdentityId());
    }

    private void prepareAmazonDatabaseClient(){
        dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_WEST_2));
    }

    private void syncAccessoryTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Accessory_Content.Accessory> result = mapper.scan(Accessory_Content.Accessory.class,expression);
            for (int i = 0; i < result.size(); i++) {
                Accessory_Content.Accessory accessory = result.get(i);
                Accessory_Content.Accessory testAccessory = Accessory_Content.get(applicationContext).getAccessoryById(accessory.getId().toString());
                if(testAccessory == null){
                    Accessory_Content.get(applicationContext).add(accessory);
                }
                mapper.delete(accessory);
            }

            List<Accessory_Content.Accessory> accessories = Accessory_Content.get(applicationContext).getAccessories();
            for (Accessory_Content.Accessory accessory:
                 accessories) {
                mapper.save(accessory);
            }
            Log.d(TAG, "Accessories Synced");
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void syncBundleTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Bundle_Content.ItemBundle> result = mapper.scan(Bundle_Content.ItemBundle.class,expression);
            for (int i = 0; i < result.size(); i++) {
                Bundle_Content.ItemBundle bundle = result.get(i);
                Bundle_Content.get(applicationContext).addItemsToBundle(bundle);
                Bundle_Content.get(applicationContext).addAccessoriesToBundle(bundle);
                Bundle_Content.ItemBundle testBundle = Bundle_Content.get(applicationContext).getBundleById(bundle.getId().toString());
                if(testBundle == null){
                    Bundle_Content.get(applicationContext).add(bundle);
                }
                mapper.delete(bundle);
            }
            List<Bundle_Content.ItemBundle> bundles = Bundle_Content.get(applicationContext).getBundles();
            for (Bundle_Content.ItemBundle bundle:
                 bundles) {
                mapper.save(bundle);
            }
            Log.d(TAG,"Bundle Synced");
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void syncBundlesHaveAccessoriesTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Bundles_Have_Accessories_Content.Bundle_Has_Accessory> result =
                    mapper.scan(Bundles_Have_Accessories_Content.Bundle_Has_Accessory.class, expression);
            for (int i = 0; i < result.size(); i++) {
                Bundles_Have_Accessories_Content.Bundle_Has_Accessory bundle_has_accessory = result.get(i);
                Bundles_Have_Accessories_Content.Bundle_Has_Accessory testItem =
                        Bundles_Have_Accessories_Content.get(applicationContext).
                                getSpecificEntry(bundle_has_accessory.getAccessoryId(),
                                        bundle_has_accessory.getBundleId());
                if(testItem == null){
                    Bundles_Have_Accessories_Content.get(applicationContext).add(bundle_has_accessory);
                }
                mapper.delete(bundle_has_accessory);
            }
            List<Bundles_Have_Accessories_Content.Bundle_Has_Accessory> bundle_has_accessories = Bundles_Have_Accessories_Content.get(applicationContext).getAllEntries();
            for (int i = 0; i < bundle_has_accessories.size(); i++) {
                mapper.save(bundle_has_accessories.get(i));
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void syncEventTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Event_Content.Event> result = mapper.scan(Event_Content.Event.class,expression);
            for (int i = 0; i < result.size(); i++) {
                Event_Content.Event event = result.get(i);
                Event_Content.Event testEvent = Event_Content.get(applicationContext).getEventById(event.getId().toString());
                if(testEvent == null){
                    Event_Content.get(applicationContext).add(event);
                }
                mapper.delete(event);
            }
            List<Event_Content.Event> events = Event_Content.get(applicationContext).getEvents();
            for (int i = 0; i < events.size(); i++) {
                mapper.save(events.get(i));
            }
            Log.i(TAG, "Synced Events");
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void syncEventsHaveAccessoriesTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Events_Have_Accessories_Content.Event_Has_Accessory> result =
                    mapper.scan(Events_Have_Accessories_Content.Event_Has_Accessory.class,expression);
            for (int i = 0; i < result.size(); i++) {
                Events_Have_Accessories_Content.Event_Has_Accessory event_has_accessory = result.get(i);
                Events_Have_Accessories_Content.Event_Has_Accessory test =
                        Events_Have_Accessories_Content.get(applicationContext).getSpecificEntry(
                                event_has_accessory.getIdOfEvent(),event_has_accessory.getIdOfAccessory()
                        );
                if(test == null){
                    Events_Have_Accessories_Content.get(applicationContext).add(event_has_accessory);
                }
                mapper.delete(event_has_accessory);
            }
            List<Events_Have_Accessories_Content.Event_Has_Accessory> list =
                    Events_Have_Accessories_Content.get(applicationContext).getAllEntries();
            for (int i = 0; i < list.size(); i++) {
                mapper.save(list.get(i));
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void syncEventsHaveBundlesTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Events_Have_Bundles_Content.Event_Has_Bundle> resutlt =
                    mapper.scan(Events_Have_Bundles_Content.Event_Has_Bundle.class,expression);
            for (int i = 0; i < resutlt.size(); i++) {
                Events_Have_Bundles_Content.Event_Has_Bundle event_has_bundle = resutlt.get(i);
                Events_Have_Bundles_Content.Event_Has_Bundle test =
                        Events_Have_Bundles_Content.get(applicationContext).getSpecificEntry(
                                event_has_bundle.getIdOfEvent(),event_has_bundle.getIdOfBundle()
                        );
                if(test == null){
                    Events_Have_Bundles_Content.get(applicationContext).add(event_has_bundle);
                }
                mapper.delete(event_has_bundle);
            }
            List<Events_Have_Bundles_Content.Event_Has_Bundle> list =
                    Events_Have_Bundles_Content.get(applicationContext).getAllEntries();
            for (int i = 0; i < list.size(); i++) {
                mapper.save(list.get(i));
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void syncEventsHaveItemsTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Events_Have_Items_Content.Event_Has_Item> result =
                    mapper.scan(Events_Have_Items_Content.Event_Has_Item.class, expression);
            for (int i = 0; i < result.size(); i++) {
                Events_Have_Items_Content.Event_Has_Item event_has_item = result.get(i);
                Events_Have_Items_Content.Event_Has_Item test =
                        Events_Have_Items_Content.get(applicationContext).getSpecificEntry(
                                event_has_item.getEventUUID(),event_has_item.getItemUUID()
                        );
                if(test == null){
                    Events_Have_Items_Content.get(applicationContext).add(
                            event_has_item
                    );
                }
                mapper.delete(event_has_item);
            }
            List<Events_Have_Items_Content.Event_Has_Item> list = Events_Have_Items_Content.get(applicationContext).getAllEntries();
            for (int i = 0; i < list.size(); i++) {
                mapper.save(list.get(i));
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void syncItemTable() throws Exception{
        DynamoDBScanExpression expression = new DynamoDBScanExpression();
        try{
            PaginatedScanList<Item_Content.Item> result = mapper.scan(Item_Content.Item.class,expression);
            for (int i = 0; i < result.size(); i++) {
                Item_Content.Item item = result.get(i);
                Item_Content.Item testItem = Item_Content.get(applicationContext).getInventoryItemById(item.getId().toString());
                if(testItem == null){
                    Item_Content.get(applicationContext).add(item);
                }
                mapper.delete(item);
            }
            List<Item_Content.Item> items = Item_Content.get(applicationContext).getInventoryItemsForSync();
            for (Item_Content.Item item:
                    items) {
                mapper.save(item);
            }
            Log.d(TAG, "Items Synced");
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
            throw e;
        }
    }



    private void prepareAmazonDatabaseMapper(){
        mapper = new DynamoDBMapper(dynamoDBClient);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        ProgressBar progressBar = (ProgressBar) applicationView;
        try {
            //progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            progressBar.setProgress(values[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        ProgressBar progressBar = (ProgressBar) applicationView;
        //progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(applicationContext, "Sync Complete", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        ProgressBar progressBar = (ProgressBar) applicationView;
        progressBar.setProgress(0);
        Toast.makeText(applicationContext, "SYNCING HAS BEEN DISABLED", Toast.LENGTH_SHORT).show();
    }
}
