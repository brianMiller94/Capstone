package com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.barcode.BarcodeCaptureActivity;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;

import java.util.ArrayList;

public class CheckIn extends AppCompatActivity {

    //Constants
    public static final String INVENTORY_ITEMS = "items";
    public static final String INVENTORY_ACCESSORIES = "items";
    public static final String AMOUNT_OF_ACCESSORIES = "accessoriesAmount";
    public static final String INVENTORY_BUNDLES = "bundles";
    public static final String SPECIFIC_EVENT = "specific_event";
    private static final int RC_BARCODE_CAPTURE = 9001;

    //Returned Arrays
    private ArrayList<String> uuidsOfPickedAccessories;
    private ArrayList<String> uuidsOfPickedBundles;
    private ArrayList<String> uuidsOfPickedItems;
    private ArrayList<Integer> amountOfAccessories;

    //Global Variables
    Event_Content.Event mCurrentEvent;


    //Widgets
    EditText mSearchBarcode;
    Button mCheckInButton;
    Button mDone;
    Button mScan;
    ListView mCheckedOutItems;
    ListView mCheckedOutAccessories;
    ListView mCheckedOutBundles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_check_in);

        //prevent keyboard from showing initially
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //setup array lists
        uuidsOfPickedAccessories = new ArrayList<>();
        uuidsOfPickedBundles = new ArrayList<>();
        uuidsOfPickedItems = new ArrayList<>();

        getEvent();
        defineWidgets();
        wireWidgets();
        setWidgets();
    }

    @Override
    public void onPause(){
        super.onPause();
        overridePendingTransition(0,0);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    void getEvent(){
        String uuidPassed = getIntent().getExtras().getString(SPECIFIC_EVENT);
        mCurrentEvent = Event_Content.get(getApplicationContext()).getEventById(uuidPassed);
    }

    void defineWidgets(){
        mSearchBarcode = (EditText) findViewById(R.id.masterCheckIn_barcodeText_EditText);
        mCheckInButton = (Button) findViewById(R.id.masterCheckIn_checkIn_Button);
        mDone = (Button) findViewById(R.id.masterCheckIn_Finish_Button);
        mScan = (Button) findViewById(R.id.masterCheckIn_Scan_Button);
        mCheckedOutItems = (ListView) findViewById(R.id.masterCheckIn_checkedOutItems_ListView);
        mCheckedOutAccessories = (ListView) findViewById(R.id.masterCheckIn_checkedOutAccessories_ListView);
        mCheckedOutBundles = (ListView) findViewById(R.id.masterCheckIn_checkedOutBundles_ListView);
    }

    void setWidgets(){
        setCheckedOutItemsListView();
        setCheckedOutAccessoriesListView();
        setCheckOutBundlesListView();
    }

    void setCheckedOutItemsListView(){
        ItemAdapter itemAdapter = new ItemAdapter();
        itemAdapter.addItems(mCurrentEvent.getItems());
        mCheckedOutItems.setAdapter(itemAdapter);
    }

    void setCheckedOutAccessoriesListView(){
        AccessoryAdapter accessoryAdapter = new AccessoryAdapter();
        accessoryAdapter.addAccessories(mCurrentEvent.getAccessories());
        mCheckedOutAccessories.setAdapter(accessoryAdapter);
    }

    void setCheckOutBundlesListView(){
        BundleAdapter bundleAdapter = new BundleAdapter();
        bundleAdapter.addBundles(mCurrentEvent.getItemBundles());
        mCheckedOutBundles.setAdapter(bundleAdapter);
    }

    void wireWidgets(){
        wireEditTextTextChanged();
        wireCheckInButton();
        wireDoneButton();
        wireScanButton();
    }
    void wireDoneButton(){
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void wireScanButton(){
        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckIn.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });
    }

    void wireEditTextTextChanged(){
        mSearchBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void wireCheckInButton(){
        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barcode = mSearchBarcode.getText().toString();

                int accessoryLocation = barcodeIsAccessory(barcode);
                int itemLocation = barcodeIsItem(barcode);
                int bundleLocation = barcodeIsBundle(barcode);

                boolean found = false;

                if(accessoryLocation > -1){
                    NumberPicker mNumberPicker;
                    TextView mName;
                    View view = getViewByPosition(accessoryLocation,mCheckedOutAccessories);
                    mNumberPicker = (NumberPicker) view.findViewById(R.id.contentCheckInAccessories_amount_picker);
                    mName = (TextView) view.findViewById(R.id.contentCheckInAccessories_accessoryName_textView);
                    int currentValue = mNumberPicker.getValue();
                    int max = mNumberPicker.getMaxValue();
                    if(currentValue != max){
                        mNumberPicker.setValue(currentValue + 1);
                    }else{
                        Toast.makeText(CheckIn.this,
                                "All " + mName.getText().toString() + " are checked in" ,
                                Toast.LENGTH_SHORT).show();
                    }
                    mSearchBarcode.setText("");
                    found = true;
                }

                if(itemLocation > -1){
                    CheckBox mCheckIn;
                    View view = getViewByPosition(itemLocation,mCheckedOutItems);
                    mCheckIn = (CheckBox) view.findViewById(R.id.contentCheckInItems_picked_checkBox);
                    mCheckIn.setChecked(true);
                    mSearchBarcode.setText("");
                    found = true;
                }

                if(bundleLocation > -1){
                    CheckBox mCheckIn;
                    View view = getViewByPosition(bundleLocation,mCheckedOutBundles);
                    mCheckIn = (CheckBox) view.findViewById(R.id.contentCheckInBundles_picked_checkBox);
                    mCheckIn.setChecked(true);
                    mSearchBarcode.setText("");
                    found = true;
                }
                if(!found){
                    Toast.makeText(CheckIn.this, "Sorry barcode not valid", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    int barcodeIsAccessory(String barcode){
        int found = -1;
        for (int i = 0; i < mCurrentEvent.getAccessories().size(); i++) {
            if(mCurrentEvent.getAccessories().get(i).getBarcode().toLowerCase().equals(barcode.toLowerCase())){
                found = i;
            }
        }
        return found;
    }

    int barcodeIsItem(String barcode){
        int found = -1;
        for (int i = 0; i < mCurrentEvent.getItems().size(); i++) {
            if(mCurrentEvent.getItems().get(i).getBarcode().toLowerCase().equals(barcode.toLowerCase())){
                found = i;
            }
        }
        return found;
    }

    int barcodeIsBundle(String barcode){
        int found = -1;
        for (int i = 0; i < mCurrentEvent.getItemBundles().size(); i++) {
            if(mCurrentEvent.getItemBundles().get(i).getBarcode().toLowerCase().equals(barcode.toLowerCase())){
                found = i;
            }
        }
        return found;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private class AccessoryAdapter extends BaseAdapter {

        private ArrayList<Accessory_Content.Accessory> accessories = new ArrayList<>();
        private LayoutInflater mInflater;

        public AccessoryAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addAccessories(final ArrayList<Accessory_Content.Accessory> accessories) {
            this.accessories = accessories;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return accessories.size();
        }

        @Override
        public Object getItem(int position) {
            return accessories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AccessoryViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.content_check_in_accessories, null);
                holder = new AccessoryViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (AccessoryViewHolder) convertView.getTag();
            }
            holder.bindAccessory(accessories.get(position));
            return convertView;
        }


        private class AccessoryViewHolder {
            View v;
            Accessory_Content.Accessory mAccessory;

            //Widgets
            NumberPicker mNumberPicker;
            TextView mName;

            AccessoryViewHolder(View v) {
                this.v = v;
            }

            void bindAccessory(Accessory_Content.Accessory accessory) {
                this.mAccessory = accessory;
                bindWidgets();
                setWidgets();
                wireWidgets();
            }

            void bindWidgets() {
                mNumberPicker = (NumberPicker) v.findViewById(R.id.contentCheckInAccessories_amount_picker);
                mName = (TextView) v.findViewById(R.id.contentCheckInAccessories_accessoryName_textView);
            }

            void setWidgets() {
                mName.setText(mAccessory.getName());
                mNumberPicker.setMinValue(0);
                mNumberPicker.setMaxValue(mAccessory.getOnHand());
                mNumberPicker.setValue(0);
            }

            void wireWidgets() {
                wireNumberPickerChanged();
            }
            void wireNumberPickerChanged(){
                mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    }
                });
            }
        }

    }
    private class ItemAdapter extends BaseAdapter {

        private ArrayList<Item_Content.Item> items = new ArrayList<>();
        private LayoutInflater mInflater;

        public ItemAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItems(final ArrayList<Item_Content.Item> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.content_check_in_items, null);
                holder = new ItemViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
            }
            holder.bindItems(items.get(position));
            return convertView;
        }


        private class ItemViewHolder {
            View v;
            Item_Content.Item mItems;

            //Widgets
            CheckBox mScanned;
            TextView mName;

            ItemViewHolder(View v) {
                this.v = v;
            }

            void bindItems(Item_Content.Item item) {
                this.mItems = item;
                bindWidgets();
                setWidgets();
            }

            void bindWidgets() {
                mScanned = (CheckBox) v.findViewById(R.id.contentCheckInItems_picked_checkBox);
                mName = (TextView) v.findViewById(R.id.contentCheckInItems_itemName_textView);
            }

            void setWidgets() {
                mName.setText(mItems.getName());
                mScanned.setChecked(false);
            }

            void wireWidgets() {

            }
        }
    }
    private class BundleAdapter extends BaseAdapter {

        private ArrayList<Bundle_Content.ItemBundle> bundles = new ArrayList<>();
        private LayoutInflater mInflater;

        public BundleAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addBundles(final ArrayList<Bundle_Content.ItemBundle> bundles) {
            this.bundles = bundles;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return bundles.size();
        }

        @Override
        public Object getItem(int position) {
            return bundles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BundleViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.content_check_in_bundles, null);
                holder = new BundleViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (BundleViewHolder) convertView.getTag();
            }
            holder.bindBundles(bundles.get(position));
            return convertView;
        }


        private class BundleViewHolder {
            View v;
            Bundle_Content.ItemBundle mBundles;

            //Widgets
            CheckBox mScanned;
            TextView mName;

            BundleViewHolder(View v) {
                this.v = v;
            }

            void bindBundles(Bundle_Content.ItemBundle bundle) {
                this.mBundles = bundle;
                bindWidgets();
                setWidgets();
            }

            void bindWidgets() {
                mScanned = (CheckBox) v.findViewById(R.id.contentCheckInBundles_picked_checkBox);
                mName = (TextView) v.findViewById(R.id.contentCheckInBundles_bundleName_textView);
            }

            void setWidgets() {
                mName.setText(mBundles.getName());
                mScanned.setChecked(false);
            }

            void wireWidgets() {

            }
        }
    }
}

