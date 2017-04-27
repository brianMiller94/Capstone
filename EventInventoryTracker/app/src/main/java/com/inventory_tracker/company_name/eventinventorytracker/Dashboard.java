package com.inventory_tracker.company_name.eventinventorytracker;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory;
import com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Event;
import com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item_Bundle;
import com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_View;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_async;

import java.util.Date;

public class Dashboard extends Activity implements View.OnClickListener {

    public static final String TAG = Dashboard.class.getSimpleName();
    private Button mGenerateDummyButton;
    private Button viewDatabase;
    private Button syncAmazon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.james_dashboard);

        Button newEvent = (Button) findViewById(R.id.createEvent);
        Button newItem = (Button) findViewById(R.id.dashboard_newItem);
        Button newAccessory = (Button) findViewById(R.id.dashboard_newAccessory);
        Button newBundle = (Button) findViewById(R.id.dashboard_newBundle);
        Button viewInventory = (Button) findViewById(R.id.dashboard_viewItems_Button);
        mGenerateDummyButton = (Button) findViewById(R.id.createDummyData);
        viewDatabase = (Button) findViewById(R.id.dashboard_viewDatabase_button);
        syncAmazon = (Button) findViewById(R.id.syncWithAmazon);
        wireAmazonSync();
        wireDummyDataButton();
        wireViewDatabaseButton();

        setListeners(newEvent, newItem, newAccessory, newBundle, viewInventory);
    }

    private void wireAmazonSync(){
        syncAmazon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Amazon_Sync_async().execute(new Combo_Context_View(getApplicationContext(),findViewById(R.id.syncProgressBar)));
            }
        });
    }

    private void setListeners(Button newEvent, Button newItem, Button newAccessory, Button newBundle, Button viewInventory) {
        newEvent.setOnClickListener(this);
        newItem.setOnClickListener(this);
        newAccessory.setOnClickListener(this);
        newBundle.setOnClickListener(this);
        viewInventory.setOnClickListener(this);
    }

    private void wireDummyDataButton(){
        mGenerateDummyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateDummyData();
            }
        });
    }
    private void wireViewDatabaseButton(){
        viewDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dbmanager = new Intent(getApplicationContext(),AndroidDatabaseManager.class);
                startActivity(dbmanager);
            }
        });
    }

    private void wireDummyEventButton(){

    }

    @Override
    public void onClick(View v) {
        int btnId = v.getId();
        Intent i = null;
        switch (btnId) {
            case R.id.createEvent:
                Log.i(TAG, "Create Event");
                i = new Intent(v.getContext(), Event.class);
                break;
            case R.id.dashboard_newItem:
                Log.i(TAG, "Create Item");
                i = new Intent(v.getContext(), Item.class);
                break;
            case R.id.dashboard_newAccessory:
                Log.i(TAG, "Create Accessory");
                i = new Intent(v.getContext(), Accessory.class);
                break;
            case R.id.dashboard_newBundle:
                Log.i(TAG, "Create Bundle");
                i = new Intent(v.getContext(), Item_Bundle.class);
                break;
            case R.id.dashboard_viewItems_Button:
                i = new Intent(v.getContext(), ItemListActivity.class);
                break;
            default:
                Log.i(TAG, "Oops....Missed Something!!!");
                break;
        }
        if (i != null) {
            try{
                startActivity(i);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /**
     * This is a routine to generate dummy data
     */
    private void generateDummyData() {
        //Items
        try {
            int itemCount = 0;
            try {
                itemCount = Item_Content.get(getApplicationContext()).getInventoryItems().size();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Item_Content.Item dummyItem = new Item_Content.Item();
            dummyItem.setBarcode("item");
            dummyItem.setSerialNumber("sample");
            dummyItem.setDescription("Dummy description");
            dummyItem.setName("Dummy Item " + itemCount);
            Item_Content.get(this).add(dummyItem);


            //Accessories
            Accessory_Content.Accessory dummyAccessory = new Accessory_Content.Accessory();
            dummyAccessory.setName("Dummy Accessory " + itemCount);
            dummyAccessory.setBarcode("accessory");
            dummyAccessory.setOnHand(10);
            dummyAccessory.setTotalQuantity(10);
            dummyItem.setSerialNumber("sample");
            Accessory_Content.get(this).add(dummyAccessory);

            //Item Bundle
            Item_Content.Item itemForDummy = new Item_Content.Item();
            itemForDummy.setBarcode("BundleItem");
            itemForDummy.setSerialNumber("sample");
            itemForDummy.setDescription("Item for Test Bundle");
            itemForDummy.setName("Dummy for bundle " + itemCount);
            itemForDummy.setSerialNumber("sample");
            Item_Content.get(this).add(itemForDummy);

            Bundle_Content.ItemBundle itemBundle = new Bundle_Content.ItemBundle();
            itemBundle.addAccessory(dummyAccessory, 5);
            itemBundle.addItem(itemForDummy);
            itemBundle.setBarcode("bundle");
            itemBundle.setDescription("This is a test Item Bundle");
            itemBundle.setName("Test Bundle " + itemCount);
            Bundle_Content.get(this).add(itemBundle);

            Toast.makeText(this, "Dummy Data Created", Toast.LENGTH_SHORT).show();

            generateDummyEvent();

        } catch (SQLiteConstraintException sqlce) {
            Toast.makeText(this, "Dummy Items Already Created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            e.printStackTrace();


        }
    }
        /** This is for generating dummy events */
    private void generateDummyEvent(){
        //Event
        try{
            Event_Content.Event dummyEvent = new Event_Content.Event();
            dummyEvent.setName("Dummy Event");
            dummyEvent.setDescription("Description for dummy event");
            Date dummyDate = new Date();
            dummyEvent.setDateStart(dummyDate);
            dummyEvent.setDateEnd(dummyDate);
            Event_Content.get(getApplicationContext()).add(dummyEvent);
            Toast.makeText(this, "Dummy Event Created", Toast.LENGTH_SHORT).show();
            //Testing used in debug
            Event_Content.Event testing = Event_Content.get(getApplicationContext()).
                    getEventById(dummyEvent.getId().toString());

        }catch (SQLiteConstraintException sqlce){
            Toast.makeText(this, "Dummy Events Already Created", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            e.printStackTrace();


        }

    }
}
