package com.inventory_tracker.company_name.eventinventorytracker.Create_Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.PickItemsListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.ScanActivity;
import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;

import java.util.ArrayList;

public class Item_Bundle extends AppCompatActivity {

    //Widgets
    Button takePhoto;
    Button createBundle;
    Button addItems;
    Button cancel;

    EditText mName;
    EditText mDescription;
    EditText mBarcode;

    //Constants
    final static int TAKE_PHOTO_REQUEST = 1;
    final static int ADD_INVENTORY_REQUEST = 2;

    final static String ITEMS = "items";
    final static String ACCESSORIES = "accessories";
    final static String BARCODE = "barcode";

    //Globals

    Bundle_Content.ItemBundle mItemBundle;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_bundle);
        mItemBundle = new Bundle_Content.ItemBundle();
        bindWidgets();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO_REQUEST:
                photoOfBarcodeTaken(data);
                break;
            case ADD_INVENTORY_REQUEST:
                pullInventoryItemsIntoBundle(data);
                break;
        }
    }

    private void bindWidgets() {
        takePhoto = (Button) findViewById(R.id.createBundle_takePhotoOfBarcode_Button);
        createBundle = (Button) findViewById(R.id.createBundle_createBundle_Button);
        addItems = (Button) findViewById(R.id.createBundle_addItemsToBundle_Button);
        cancel = (Button) findViewById(R.id.createBundle_cancel_button);

        mName = (EditText) findViewById(R.id.createBundle_name_EditText);
        mDescription = (EditText) findViewById(R.id.createBundle_description_TextView);
        mBarcode = (EditText) findViewById(R.id.createBundle_addBarcode_EditText);

        wireTakePhotoButton();
        wireCreateBundleButton();
        wireAddItemsButton();
        wireCancelButton();
    }

    private void wireTakePhotoButton() {
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePhotoIntent = new Intent(Item_Bundle.this, ScanActivity.class);
                startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
            }
        });

    }

    private void wireCreateBundleButton() {
        createBundle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String descriptionString = mDescription.getText().toString();
                    String barcodeString = mBarcode.getText().toString();
                    String nameString = mName.getText().toString();

                    mItemBundle.setName(nameString);
                    mItemBundle.setBarcode(barcodeString);
                    mItemBundle.setDescription(descriptionString);

                    Bundle_Content.ItemBundle.validateBundle(mItemBundle);

                    Bundle_Content.get(getApplicationContext()).add(mItemBundle);
                    Toast.makeText(Item_Bundle.this, "New Item Bundle created", Toast.LENGTH_SHORT).show();
                    finish();

                } catch (Exception e) {
                    Toast.makeText(Item_Bundle.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void wireAddItemsButton() {
        addItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickInventoryIntent = new Intent(Item_Bundle.this, PickItemsListActivity.class);
                pickInventoryIntent.putExtra(PickItemsListActivity.SHOW_ITEMS_ACCESSORIES, 1);
                startActivityForResult(pickInventoryIntent, ADD_INVENTORY_REQUEST);
            }
        });
    }

    private void wireCancelButton() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void photoOfBarcodeTaken(Intent data) {
        String photoOfBarcode = data.getExtras().getString(BARCODE);
        mBarcode.setText(photoOfBarcode);
    }

    private void pullInventoryItemsIntoBundle(Intent data) {
        mItemBundle.dropAll();
        //get items
        if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS) != null){
            ArrayList<Item_Content.Item> items =
                    Item_Content.get(getApplicationContext()).
                            mapInventoryItemsByUUID(data.getExtras().
                                    getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS));
            this.mItemBundle.setItems(items);
        }

        if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES) != null){
            ArrayList<Accessory_Content.Accessory> accessories =
                    Accessory_Content.get(getApplicationContext()).
                            mapAccessoriesByUUID(data.getExtras().
                                    getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES));
            ArrayList<Integer> amounts = data.getExtras().getIntegerArrayList(PickItemsListActivity.AMOUNT_OF_ACCESSORIES);
            for (int i = 0; i < accessories.size(); i++) {
                if(amounts.get(i) != 0){
                    Accessory_Content.Accessory pickedAccessory = accessories.get(i);
                    mItemBundle.addAccessory(pickedAccessory,amounts.get(i));
                }else{
                    mItemBundle.removeAccessory(accessories.get(0));
                }

            }
        }
    }
}
