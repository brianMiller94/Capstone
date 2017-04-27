package com.inventory_tracker.company_name.eventinventorytracker.detail_fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.PickItemsListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.InventoryItemDetailActivity;
import com.inventory_tracker.company_name.eventinventorytracker.ItemListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.barcode.BarcodeCaptureActivity;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;


public class Accessory extends Fragment {

    Accessory_Content.Accessory mAccessory;
    Accessory_Content.Accessory mAccessoryCopy;

    TextView mName;
    TextView mOnHand;
    TextView mTotalQuantity;
    TextView mBarcode;
    Button mSave;
    Button mTakePhoto;
    Button mCancelButton;
    Button mDeleteButton;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "Detail Activity";

    public Accessory() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(InventoryItemDetailActivity.ARG_ITEM_ID)) {
            //load Item
            String uuidString = getArguments().getString(InventoryItemDetailActivity.ARG_ITEM_ID);
            mAccessory = Accessory_Content.get(this.getContext()).getAccessoryById(uuidString);
            mAccessoryCopy = Accessory_Content.get(this.getContext()).getAccessoryById(uuidString);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_accessory, container, false);

        initializeWidgets(rootView);
        setWidgetsToAccessoryValues(mAccessory);
        wireButtons();

        return rootView;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    mBarcode.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {

                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Toast.makeText(getContext(), String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initializeWidgets(View v){
        mName = (TextView) v.findViewById(R.id.detailAccessory_name_editText);
        mOnHand = (TextView) v.findViewById(R.id.detailAccessory_onHand_editText);
        mOnHand.setEnabled(false);
        mTotalQuantity = (TextView) v.findViewById(R.id.detailAccessory_totalQuantity_editText);
        mBarcode = (TextView) v.findViewById(R.id.detailAccessory_barcode_editText);
        mSave = (Button) v.findViewById(R.id.detailAccessory_save_button);
        mTakePhoto = (Button) v.findViewById(R.id.detailAccessory_takePhoto_button);
        mCancelButton = (Button) v.findViewById(R.id.detailAccessory_cancel_button);
        mDeleteButton = (Button) v.findViewById(R.id.detailAccessory_delete_button);

    }
    private void setWidgetsToAccessoryValues(Accessory_Content.Accessory accessory){
        mName.setText(accessory.getName());
        mOnHand.setText(String.valueOf(accessory.getOnHand()));
        mTotalQuantity.setText(String.valueOf(accessory.getTotalQuantity()));
        mBarcode.setText(accessory.getBarcode());
    }
    private void wireButtons(){
        wireSaveAccessoryButton();
        wireTakePhotoButton();
        wireTotalQuantityChanged();
        wireCancelButton();
        wireDeleteButton();
    }

    private void wireDeleteButton(){
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Accessory")
                        .setMessage("Are you sure you want to delete this accessory?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    Accessory_Content.get(getContext());
                                    Accessory_Content.get(getContext()).delete(mAccessory);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                try{
                                    ((ItemListActivity)getActivity()).onResume();
                                }catch (Exception e){

                                }
                                try{
                                    ((PickItemsListActivity)getActivity()).onResume();
                                }catch (Exception e){

                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_delete)
                        .show();
            }
        });
    }
    private void wireSaveAccessoryButton(){
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    updateAccessory();
                    Accessory_Content.Accessory.validateAccessory(mAccessory);
                    Accessory_Content.get(getActivity().getApplicationContext()).update(mAccessory);
                    Toast.makeText(getActivity(), "Accessory Saved", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                try{
                    ((ItemListActivity)getActivity()).onResume();
                }catch (Exception e){

                }
                try{
                    ((PickItemsListActivity)getActivity()).onResume();
                }catch (Exception e){

                }

            }
        });
    }
    private void wireTakePhotoButton(){
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });
    }

    private void wireCancelButton(){
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clearForm();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void wireTotalQuantityChanged(){
        mTotalQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    int oldTotal = mAccessory.getTotalQuantity();
                    int oldOnHand = mAccessory.getOnHand();

                    int enteredTotal = Integer.parseInt(mTotalQuantity.getText().toString());

                    int difference = enteredTotal - oldTotal;
                    int newTotal = oldTotal + difference;
                    int newOnHand = oldOnHand + difference;

                    mAccessory.setOnHand(newOnHand);
                    mAccessory.setTotalQuantity(newTotal);

                    mOnHand.setText(String.valueOf(newOnHand));

                }
            }
        });

        mTotalQuantity.addTextChangedListener(new TextWatcher() {
            int oldTotal;
            int oldOnHand;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldOnHand = mAccessory.getOnHand();
                oldTotal = mAccessory.getTotalQuantity();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    Integer enteredTotal = Integer.parseInt(s.toString());
                    int difference = enteredTotal - oldTotal;
                    int newTotal = oldTotal + difference;
                    int newOnHand = oldOnHand + difference;

                    mAccessory.setOnHand(newOnHand);
                    mAccessory.setTotalQuantity(newTotal);

                    mOnHand.setText(String.valueOf(newOnHand));

                }catch (Exception e){
                    if(s.equals("")){
                        mTotalQuantity.setText(0);
                        onTextChanged("0",start,before,count);
                    }else{

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void updateAccessory(){
            int onHand = Integer.parseInt(mOnHand.getText().toString());
            int totalQuantity = Integer.parseInt(mTotalQuantity.getText().toString());
            mAccessory.setBarcode(mBarcode.getText().toString());
            mAccessory.setName(mName.getText().toString());
            mAccessory.setTotalQuantity(Integer.parseInt(mTotalQuantity.getText().toString()));
            mAccessory.setOnHand(Integer.parseInt(mOnHand.getText().toString()));
    }

    private void clearForm() {
        mAccessoryCopy = mAccessory;
        mName.setText(mAccessoryCopy.getName());
        mTotalQuantity.setText(String.valueOf(mAccessoryCopy.getTotalQuantity()));
        mOnHand.setText(String.valueOf(mAccessoryCopy.getOnHand()));
        mBarcode.setText(mAccessoryCopy.getBarcode());
    }
}