package com.inventory_tracker.company_name.eventinventorytracker.detail_fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.*;
import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.PickItemsListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.InventoryItemDetailActivity;
import com.inventory_tracker.company_name.eventinventorytracker.ItemListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.barcode.BarcodeCaptureActivity;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;

import java.util.ArrayList;

public class Item_Bundle extends Fragment {

    Bundle_Content.ItemBundle mItem;
    Bundle_Content.ItemBundle mItemCopy;

    //Widgets

    TextView mName;
    TextView mDescription;
    TextView mBarcode;
    Button mSave;
    Button mTakePhoto;
    Button mAddItems;
    Button mCancelButton;
    Button mDeleteButton;

    static final int PICK_INVENTORY_ITEMS_REQUEST = 8006;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "Detail Item";

    public Item_Bundle() {
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(InventoryItemDetailActivity.ARG_ITEM_ID)) {
            // Load Item
            String uuidString = getArguments().getString(InventoryItemDetailActivity.ARG_ITEM_ID);
            mItem = Bundle_Content.get(getContext()).getBundleById(uuidString);
            mItemCopy = Bundle_Content.get(getContext()).getBundleById(uuidString);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             android.os.Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_bundle, container, false);

        initializeWidgets(rootView);
        setWidgetsToBundleValues();
        wireWidgets();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PICK_INVENTORY_ITEMS_REQUEST){
            //Drop all items and accessories
            mItem.dropAll();
            //get items
            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS) != null){
                ArrayList<Item_Content.Item> items =
                        Item_Content.get(this.getContext()).
                                mapInventoryItemsByUUID(data.getExtras().
                                        getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS));
                this.mItem.setItems(items);
            }

            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES) != null){
                ArrayList<Accessory_Content.Accessory> accessories =
                        Accessory_Content.get(this.getContext()).
                                mapAccessoriesByUUID(data.getExtras().
                                        getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES));
                ArrayList<Integer> amounts = data.getExtras().getIntegerArrayList(PickItemsListActivity.AMOUNT_OF_ACCESSORIES);
                for (int i = 0; i < accessories.size(); i++) {
                    if(amounts.get(i) != 0){
                        Accessory_Content.Accessory pickedAccessory = accessories.get(i);
                        mItem.addAccessory(pickedAccessory,amounts.get(i));
                    }else{
                        mItem.removeAccessory(accessories.get(i));
                    }

                }
            }
        }else if (requestCode == RC_BARCODE_CAPTURE) {
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
        mBarcode = (TextView) v.findViewById(R.id.detailBundle_addBarcode_EditText);
        mDescription = (TextView) v.findViewById(R.id.detailBundle_description_TextView);
        mName = (TextView) v.findViewById(R.id.detailBundle_name_EditText);
        mSave = (Button) v.findViewById(R.id.detailBundle_saveBundle_Button);
        mTakePhoto = (Button) v.findViewById(R.id.detailBundle_takePhotoOfBarcode_Button);
        mAddItems = (Button) v.findViewById(R.id.detailBundle_addItemsToBundle_Button);
        mCancelButton = (Button) v.findViewById(R.id.detailBundle_cancel_button);
        mDeleteButton = (Button) v.findViewById(R.id.detailBundle_delete_button);
    }

    private void setWidgetsToBundleValues(){
        mBarcode.setText(mItem.getBarcode());
        mDescription.setText(mItem.getDescription());
        mName.setText(mItem.getName());
    }

    private void wireWidgets(){
        wireTakePhotoButton();
        wireSaveButton();
        wireAddItemButton();
        wireCancelButton();
        wireDeleteButton();
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

    private void wireSaveButton(){
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    updateBundle();
                    Bundle_Content.ItemBundle.validateBundle(mItem);
                    Bundle_Content.get(getContext()).update(mItem);
                    Toast.makeText(getActivity(), "Bundle Updated", Toast.LENGTH_SHORT).show();
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

    private void wireDeleteButton(){
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Bundle")
                        .setMessage("Are you sure you want to delete this bundle?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    Bundle_Content.get(getContext()).delete(mItem);
                                    try{
                                        ((ItemListActivity)getActivity()).onResume();
                                    }catch (Exception e){

                                    }
                                    try{
                                        ((PickItemsListActivity)getActivity()).onResume();
                                    }catch (Exception e){

                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
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

    private void wireAddItemButton(){
        mAddItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getActivity(), "Sorry, feature not yet implemented", Toast.LENGTH_SHORT).show();
                launchAddItem();
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

    private void launchAddItem(){
        Intent pickInventoryIntent = new Intent(getContext(),PickItemsListActivity.class);
        pickInventoryIntent.putExtra(PickItemsListActivity.SHOW_ITEMS_ACCESSORIES, 1);
        pickInventoryIntent.putExtra(PickItemsListActivity.SPECIFIC_BUNDLE, mItem.getId().toString());
        startActivityForResult(pickInventoryIntent, PICK_INVENTORY_ITEMS_REQUEST);
    }

    private void updateBundle(){
        mItem.setName(mName.getText().toString());
        mItem.setBarcode(mBarcode.getText().toString());
        mItem.setDescription(mDescription.getText().toString());
    }

    private void clearForm() {
        mItemCopy = mItem;
        mName.setText(mItemCopy.getName());
        mBarcode.setText(mItemCopy.getBarcode());
        mDescription.setText(mItemCopy.getDescription());
    }
}