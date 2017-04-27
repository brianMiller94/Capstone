package com.inventory_tracker.company_name.eventinventorytracker.detail_fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.PickItemsListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.InventoryItemDetailActivity;
import com.inventory_tracker.company_name.eventinventorytracker.ItemListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.barcode.BarcodeCaptureActivity;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link InventoryItemDetailActivity}
 * on handsets.
 */
public class Item extends Fragment {
    //Item
    private Item_Content.Item mItem;
    private Item_Content.Item mItemCopy;

    //Widgets
    EditText mName;
    EditText mDescription;
    EditText mBarcode;
    EditText mSerialNumber;
    Button mSaveButton;
    Button mPhotoButton;
    Button mCancelButton;
    Button mDeleteButton;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "Detail Item";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Item() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(InventoryItemDetailActivity.ARG_ITEM_ID)) {
            // Load Item
            String uuidStringId = getArguments().getString(InventoryItemDetailActivity.ARG_ITEM_ID);
            mItem = Item_Content.get(this.getContext()).getInventoryItemById(uuidStringId);
            mItemCopy = Item_Content.get(this.getContext()).getInventoryItemById(uuidStringId);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_item, container, false);

        //Define Widgets
        mName = (EditText) rootView.findViewById(R.id.detailItem_name_editText);
        mDescription = (EditText) rootView.findViewById(R.id.detailItem_Description_editText);
        mBarcode = (EditText) rootView.findViewById(R.id.detailItem_barcode_editText);
        mSerialNumber = (EditText) rootView.findViewById(R.id.detailItem_serialNumber_editText);
        mSaveButton = (Button) rootView.findViewById(R.id.detailItem_save_button);
        mPhotoButton = (Button) rootView.findViewById(R.id.detailItem_takePhoto_button);
        mCancelButton = (Button) rootView.findViewById(R.id.detailItem_cancel_button);
        mDeleteButton = (Button) rootView.findViewById(R.id.detailItem_delete_button);

        if (mItem == null){
            mSaveButton.setEnabled(false);
        }
        //Wire Button
        wireSaveButton();
        wireTakePhotoButton();
        wireCancelButton();
        wireDeleteButton();

        //set widgets
        mName.setText(mItem.getName());
        mDescription.setText(mItem.getDescription());
        mBarcode.setText(mItem.getBarcode());
        mSerialNumber.setText(mItem.getSerialNumber());
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

    private void wireSaveButton(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    updateItem();
                    Item_Content.Item.validateItem(mItem);
                    Item_Content.get(getContext()).update(mItem);
                    Toast.makeText(getActivity(), "Item Updated", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                try{
                    ((ItemListActivity)getActivity()).onResume();
                }catch (Exception e){
                    e.printStackTrace();
                }
                try{
                    ((PickItemsListActivity)getActivity()).onResume();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void wireTakePhotoButton(){
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
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
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void wireDeleteButton(){
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    Item_Content.get(getContext()).delete(mItem);
                                    try{
                                        ((ItemListActivity)getActivity()).onResume();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    try{
                                        ((PickItemsListActivity)getActivity()).onResume();
                                    }catch (Exception e){
                                        e.printStackTrace();
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

    private void updateItem(){
        mItem.setName(mName.getText().toString());
        mItem.setDescription(mDescription.getText().toString());
        mItem.setBarcode(mBarcode.getText().toString());
        mItem.setSerialNumber(mSerialNumber.getText().toString());
    }

    private void clearForm() {
        mItemCopy = mItem;
        mName.setText(mItemCopy.getName());
        mDescription.setText(mItemCopy.getDescription());
        mBarcode.setText(mItemCopy.getBarcode());
        mSerialNumber.setText(mItemCopy.getSerialNumber());
    }
}
