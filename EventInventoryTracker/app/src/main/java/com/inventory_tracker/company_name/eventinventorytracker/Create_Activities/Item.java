package com.inventory_tracker.company_name.eventinventorytracker.Create_Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;

import java.util.Calendar;
import java.util.Date;

public class Item extends AppCompatActivity {

    //Item
    private Item_Content.Item mItem;

    //Widgets
    EditText mName;
    EditText mDescription;
    EditText mBarcode;
    CheckBox mCheckIn;
    EditText mSerialNumber;
    Button mSaveButton;
    Button mTakePhotoButton;
    Button mCancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_item);
        mItem = new Item_Content.Item();
        initializeWidgets();
    }

    private void initializeWidgets(){
        mName = (EditText) findViewById(R.id.createItem_name_editText);
        mDescription = (EditText) findViewById(R.id.createItem_Description_editText);
        mBarcode = (EditText) findViewById(R.id.createItem_barcode_editText);
        mCheckIn = (CheckBox) findViewById(R.id.createItem_checkIn_checkBox);
        mSerialNumber = (EditText) findViewById(R.id.createItem_serialNumber_EditText);
        mSaveButton = (Button) findViewById(R.id.createItem_save_button);
        mTakePhotoButton = (Button) findViewById(R.id.createItem_takePhoto_button);
        mCancelButton = (Button) findViewById(R.id.createItem_cancel_button);
        wireWidgets();
    }
    private void wireWidgets(){
        wireSaveButton();
        wirePhotoButton();
        wireCancelButton();
    }
    private void wireSaveButton(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateItem();
                    Item_Content.Item.validateItem(mItem);
                    Item_Content.get(getApplicationContext()).add(mItem);
                    Toast.makeText(Item.this, "Item Created", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Toast.makeText(Item.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void wirePhotoButton(){
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Item.this, "Sorry, feature not implemented", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Item.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void updateItem(){
        mItem.setName(mName.getText().toString());
        mItem.setDescription(mDescription.getText().toString());
        mItem.setBarcode(mBarcode.getText().toString());
        mItem.setSerialNumber(mSerialNumber.getText().toString());
    }

    private Date getDateFromPicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);

        return calendar.getTime();
    }
}
