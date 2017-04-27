package com.inventory_tracker.company_name.eventinventorytracker.detail_fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.EventDetailActivity;
import com.inventory_tracker.company_name.eventinventorytracker.EventListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.CheckIn;
import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.PickItemsListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.InventoryItemDetailActivity;
import com.inventory_tracker.company_name.eventinventorytracker.ItemListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.custom_widgets.improvedDatePicker;

import java.util.ArrayList;
import java.util.Date;

public class Event extends Fragment {

    //Item
    private Event_Content.Event mEvent;
    private Event_Content.Event mEventCopy;

    //ARGS
    public static String EVENT_ID;

    public static int PICK_INVENTORY_ITEMS_REQUEST = 512;

    //Widgets
    EditText mName;
    EditText mDescription;
    improvedDatePicker mStartDate;
    improvedDatePicker mEndDate;
    Button mSaveButton;
    Button mPickItems;
    Button mCheckIn;
    Button mCancelButton;
    Button mDeleteButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(EVENT_ID)) {
            // Load Item
            String uuidStringId = getArguments().getString(EVENT_ID);
            mEvent = Event_Content.get(this.getContext()).getEventById(uuidStringId);
            mEventCopy = Event_Content.get(this.getContext()).getEventById(uuidStringId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_event, container, false);

        //Define Widgets
        mName = (EditText) rootView.findViewById(R.id.detailEvent_name_editText);
        mDescription = (EditText) rootView.findViewById(R.id.detailEvent_description_editText);
        mStartDate = (improvedDatePicker) rootView.findViewById(R.id.detailEvent_dateStart_datePicker);
        mEndDate = (improvedDatePicker) rootView.findViewById(R.id.detailEvent_dateEnd_datePicker);
        mSaveButton = (Button) rootView.findViewById(R.id.detailEvent_save_button);
        mPickItems = (Button) rootView.findViewById(R.id.detailEvent_addItems_button);
        mCheckIn = (Button) rootView.findViewById(R.id.detailEvent_checkInItems_button);
        mCancelButton = (Button) rootView.findViewById(R.id.detailEvent_cancel_button);
        mDeleteButton = (Button) rootView.findViewById(R.id.detailEvent_delete_button);

        //set widgets
        mName.setText(mEvent.getName());
        mDescription.setText(mEvent.getDescription());
        mStartDate.updateDate(mEvent.getDateStart());
        mEndDate.updateDate(mEvent.getDateEnd());

        //wire widgets
        wireSaveButton();
        wirePickItemsButton();
        wireCheckInButton();
        wireCancelButton();
        wireDeleteButton();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PICK_INVENTORY_ITEMS_REQUEST){
            mEvent.dropAll();
            //get items
            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS) != null){
                ArrayList<Item_Content.Item> items =
                        Item_Content.get(this.getContext()).
                                mapInventoryItemsByUUID(data.getExtras().
                                        getStringArrayList(PickItemsListActivity.INVENTORY_ITEMS));
                this.mEvent.setItems(items);
            }

            //get Accessories
            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES) != null){
                ArrayList<Accessory_Content.Accessory> accessories =
                        Accessory_Content.get(this.getContext()).
                                mapAccessoriesByUUID(data.getExtras().
                                        getStringArrayList(PickItemsListActivity.INVENTORY_ACCESSORIES));
                ArrayList<Integer> amounts = data.getExtras().getIntegerArrayList(PickItemsListActivity.AMOUNT_OF_ACCESSORIES);
                for (int i = 0; i < accessories.size(); i++) {
                    if(amounts.get(i) != 0){
                        Accessory_Content.Accessory pickedAccessory = accessories.get(i);
                        mEvent.addAccessory(pickedAccessory,amounts.get(i));
                    }else{
                        mEvent.removeAccessory(accessories.get(i));
                    }

                }
            }
            //Get Bundles
            if(data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_BUNDLES) != null){
                ArrayList<Bundle_Content.ItemBundle> bundles =
                        Bundle_Content.get(this.getContext()).mapBundlesByUUID(
                                data.getExtras().getStringArrayList(PickItemsListActivity.INVENTORY_BUNDLES)
                        );
                this.mEvent.setItemBundles(bundles);
            }
        }
    }

    private void wireSaveButton(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                updateEvent();
                Event_Content.Event.validateEvent(mEvent);
                Event_Content.get(getContext()).update(mEvent);
                Toast.makeText(getActivity(), "Event Updated", Toast.LENGTH_SHORT).show();
                ((EventListActivity)getActivity()).onResume();
                }catch (Exception e){
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void wireCheckInButton(){
        mCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getContext(), CheckIn.class);
                    intent.putExtra(CheckIn.SPECIFIC_EVENT, mEvent.getId().toString());
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    private void wireDeleteButton(){
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    Event_Content.get(getContext()).delete(mEvent);
                                    getActivity().getSupportFragmentManager().popBackStack();
                                    ((EventListActivity)getActivity()).onResume();
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

    public void updateEvent(){
        mEvent.setName(mName.getText().toString());
        mEvent.setDescription(mDescription.getText().toString());
        mEvent.setDateStart(mStartDate.getDate());
        mEvent.setDateEnd(mEndDate.getDate());
    }

    private void clearForm() {
        mEventCopy = mEvent;
        mName.setText(mEventCopy.getName());
        mDescription.setText(mEventCopy.getDescription());
        mStartDate.updateDate(mEventCopy.getDateStart());
        mEndDate.updateDate(mEventCopy.getDateEnd());
    }

    private void launchAddItem(){
        Intent pickInventoryIntent = new Intent(getContext(),PickItemsListActivity.class);
        pickInventoryIntent.putExtra(PickItemsListActivity.SHOW_ALL, 1);
        pickInventoryIntent.putExtra(PickItemsListActivity.SPECIFIC_EVENT, mEvent.getId().toString());
        startActivityForResult(pickInventoryIntent, PICK_INVENTORY_ITEMS_REQUEST);
    }
}
