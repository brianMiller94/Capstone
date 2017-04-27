package com.inventory_tracker.company_name.eventinventorytracker.Create_Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.PickItemsListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.custom_widgets.improvedDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Event extends AppCompatActivity {

    static final int PICK_INVENTORY_ITEMS_REQUEST = 1;
    static final String INVENTORY_ITEMS = "items";
    static final String INVENTORY_ACCESSORIES = "accessories";
    static final String INVENTORY_BUNDLES = "bundles";

    //Event
    Event_Content.Event mEvent;
    //Widgets
    EditText mName;
    EditText mDescription;
    improvedDatePicker mStartDate;
    improvedDatePicker mEndDate;
    Button mSaveButton;
    Button mPickItems;
    Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);
        mEvent = new Event_Content.Event();
        initializeWidgets();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PICK_INVENTORY_ITEMS_REQUEST){
            mEvent.dropAll();
            //get items
            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS) != null){
                ArrayList<Item_Content.Item> items =
                        Item_Content.get(this.getApplicationContext()).
                                mapInventoryItemsByUUID(data.getExtras().
                                        getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS));
                this.mEvent.setItems(items);
            }
            //get Accessories
            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES) != null){
                ArrayList<Accessory_Content.Accessory> accessories =
                        Accessory_Content.get(this.getApplicationContext()).
                                mapAccessoriesByUUID(data.getExtras().
                                        getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES));
                ArrayList<Integer> amounts = data.getExtras().getIntegerArrayList(PickItemsListActivity.AMOUNT_OF_ACCESSORIES);
                for (int i = 0; i < accessories.size(); i++) {
                    if(amounts.get(i) != 0){
                        Accessory_Content.Accessory pickedAccessory = accessories.get(i);
                        mEvent.addAccessory(pickedAccessory,amounts.get(i));
                    }else{
                        mEvent.removeAccessory(accessories.get(0));
                    }

                }
            }
            //Get Bundles
            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_BUNDLES) != null){
                ArrayList<Bundle_Content.ItemBundle> bundles =
                        Bundle_Content.get(this.getApplicationContext()).mapBundlesByUUID(
                                data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_BUNDLES)
                        );
                this.mEvent.setItemBundles(bundles);
            }
        }
    }

    private void initializeWidgets(){
        mName = (EditText) findViewById(R.id.createEvent_name_editText);
        mDescription = (EditText) findViewById(R.id.createEvent_description_editText);
        mStartDate = (improvedDatePicker) findViewById(R.id.createEvent_dateStart_datePicker);
        mEndDate = (improvedDatePicker) findViewById(R.id.createEvent_dateEnd_datePicker);
        mSaveButton = (Button) findViewById(R.id.createEvent_save_button);
        mPickItems = (Button) findViewById(R.id.createEvent_addItems_button);
        mCancelButton = (Button) findViewById(R.id.createEvent_cancel_button);
        wireWidgets();
    }
    private void wireWidgets(){
        wireSaveButton();
        wirePickItemsButton();
        wireCancelButton();
    }
    private void wireSaveButton(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateEvent();
                    Event_Content.Event.validateEvent(mEvent);
                    Event_Content.get(getApplicationContext()).add(mEvent);
                    Toast.makeText(Event.this, "Event Created", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Toast.makeText(Event.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void wirePickItemsButton(){
        mPickItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddItem();
            }
        });
    }

    private void wireCancelButton(){
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    finish();
                } catch (Exception e) {
                    Toast.makeText(Event.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void updateEvent() {
        mEvent.setName(mName.getText().toString());
        mEvent.setDescription(mDescription.getText().toString());
        mEvent.setDateStart(mStartDate.getDate());
        mEvent.setDateEnd(mEndDate.getDate());
    }


    private void launchAddItem(){
        Intent pickInventoryIntent = new Intent(getApplicationContext(),PickItemsListActivity.class);
        pickInventoryIntent.putExtra(PickItemsListActivity.SHOW_ALL, 1);
        pickInventoryIntent.putExtra(PickItemsListActivity.SPECIFIC_EVENT, mEvent.getId().toString());
        startActivityForResult(pickInventoryIntent, PICK_INVENTORY_ITEMS_REQUEST);
    }


}
