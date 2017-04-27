package com.inventory_tracker.company_name.eventinventorytracker.Create_Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;

public class Accessory extends AppCompatActivity {

    Accessory_Content.Accessory mAccessory;

    TextView mName;
    TextView mOnHand;
    TextView mTotalQuantity;
    TextView mBarcode;
    Button mSave;
    Button mTakePhoto;
    Button mCancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_accessory);
        //Create new accessory
        mAccessory = new Accessory_Content.Accessory();
        initializeWidgets();
    }
    private void initializeWidgets(){
        mName = (TextView) findViewById(R.id.createAccessory_name_editText);
        mOnHand = (TextView) findViewById(R.id.createAccessory_onHandQuantity_editText);
        mOnHand.setEnabled(false);
        mTotalQuantity = (TextView) findViewById(R.id.createAccessory_totalQuantity_editText);
        mBarcode = (TextView) findViewById(R.id.createAccessory_barcode_editText);
        mSave = (Button) findViewById(R.id.createAccessory_save_button);
        mTakePhoto = (Button) findViewById(R.id.createAccessory_takePhoto_button);
        mCancelButton = (Button) findViewById(R.id.createAccessory_cancel_button);

        wireWidgets();
    }

    private void wireWidgets(){
        wireTakePhotoButton();
        wireSaveButton();
        wireCancelButton();
        wireTotalQuantity();
    }

    private void wireTotalQuantity(){
        mTotalQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mTotalQuantity.getText().toString().equals("")){

                }else{
                    mOnHand.setText(mTotalQuantity.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void wireSaveButton(){
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    updateAccessory();
                    Accessory_Content.get(getApplicationContext()).add(mAccessory);
                    Toast.makeText(Accessory.this, "Accessory Added", Toast.LENGTH_SHORT).show();
                    finish();
                }catch (Exception e){
                    Toast.makeText(Accessory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
                    Toast.makeText(Accessory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void wireTakePhotoButton(){
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Accessory.this, "Sorry feature not implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateAccessory(){
        mAccessory.setName(mName.getText().toString());
        mAccessory.setOnHand(Integer.parseInt(mOnHand.getText().toString()));
        mAccessory.setTotalQuantity(Integer.parseInt(mTotalQuantity.getText().toString()));
        mAccessory.setBarcode(mBarcode.getText().toString());
    }
}
