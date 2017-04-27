package com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;


import com.inventory_tracker.company_name.eventinventorytracker.EventListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.InventoryItemDetailActivity;
import com.inventory_tracker.company_name.eventinventorytracker.ItemListActivity;
import com.inventory_tracker.company_name.eventinventorytracker.R;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_View;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.InventoryItems;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Accessory;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Item;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Item_Bundle;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_async;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

public class PickItemsListActivity extends AppCompatActivity {

    private boolean mTwoPane;

    private pickItemAdapter mPickInventoryAdapter;
    private ArrayList<String> uuidsOfPickedAccessories;
    private ArrayList<String> uuidsOfPickedBundles;
    private ArrayList<String> uuidsOfPickedItems;
    private ArrayList<Integer> amountOfAccessories;
    private Bundle_Content.ItemBundle mCurrentItemBundle;
    private Event_Content.Event mCurrentEvent;

    static final int PICK_INVENTORY_ITEMS_REQUEST = 1;
    public static final String INVENTORY_ITEMS = "items";
    public static final String INVENTORY_ACCESSORIES = "accessories";
    public static final String INVENTORY_BUNDLES = "bundles";
    public static final String SHOW_ALL = "show_all";
    public static final String SHOW_ITEMS_ACCESSORIES = "no_bundles";
    public static final String SPECIFIC_BUNDLE = "specific_bundle";
    public static final String SPECIFIC_EVENT = "specific_event";
    public static final String AMOUNT_OF_ACCESSORIES = "accessoriesAmount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_pick_inventory_list);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Pick Items");

        //initialize arrays
        uuidsOfPickedAccessories = new ArrayList<>();
        uuidsOfPickedBundles = new ArrayList<>();
        uuidsOfPickedItems = new ArrayList<>();


        View recyclerView = findViewById(R.id.pick_item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.pick_item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_inventory);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                Intent intent = null;
                switch (tabId) {
                    case R.id.tab_add:
                        CharSequence categories[] = new CharSequence[] {"Item", "Accessory", "Bundle", "Event"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(PickItemsListActivity.this);
                        builder.setTitle("Pick a Category");
                        builder.setItems(categories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = null;
                                switch (which){
                                    case 0:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item.class);
                                        break;
                                    case 1:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory.class);
                                        break;
                                    case 2:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item_Bundle.class);
                                        break;
                                    case 3:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Event.class);
                                        break;
                                    default:
                                        Toast.makeText(PickItemsListActivity.this, "no item", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(i);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.tab_inventory:
                        break;
                    case R.id.tab_events:
                        intent = new Intent(getApplicationContext(), EventListActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                Intent intent = null;
                switch (tabId) {
                    case R.id.tab_add:
                        CharSequence categories[] = new CharSequence[] {"Item", "Accessory", "Bundle", "Event"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(PickItemsListActivity.this);
                        builder.setTitle("Pick a Category");
                        builder.setItems(categories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = null;
                                switch (which){
                                    case 0:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item.class);
                                        break;
                                    case 1:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory.class);
                                        break;
                                    case 2:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item_Bundle.class);
                                        break;
                                    case 3:
                                        i = new Intent(PickItemsListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Event.class);
                                        break;
                                    default:
                                        Toast.makeText(PickItemsListActivity.this, "no item", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(i);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.tab_inventory:
                        break;
                    case R.id.tab_events:
                        intent = new Intent(PickItemsListActivity.this, EventListActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                new Amazon_Sync_async().execute(new Combo_Context_View(getApplicationContext(),findViewById(R.id.syncProgressBar)));
                break;
            case R.id.help:
                new AlertDialog.Builder(PickItemsListActivity.this)
                        .setTitle("Contact Us")
                        .setMessage(
                                "Brian Miller - Backend Developer (Database)" + "\n" +
                                        "brianmmiller94@gmail.com" + "\n" +
                                        "270-557-6635" + "\n" +
                                        "\n" +
                                        "Tyler Durfey - Frontend Developer (Layout)" + "\n" +
                                        "tylerdurfey94@gmail.com" + "\n" +
                                        "574-249-2351"
                        )
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .show();

        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView((RecyclerView) findViewById(R.id.item_list));
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_inventory);
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed(){
        Intent result = createResult();
        setResult(RESULT_OK, result);
        super.onBackPressed();
    }

    private Intent createResult(){
        Intent results = new Intent();
        results.putStringArrayListExtra(INVENTORY_ITEMS, uuidsOfPickedItems);
        results.putStringArrayListExtra(INVENTORY_ACCESSORIES, uuidsOfPickedAccessories);
        results.putStringArrayListExtra(INVENTORY_BUNDLES, uuidsOfPickedBundles);
        results.putIntegerArrayListExtra(AMOUNT_OF_ACCESSORIES, amountOfAccessories);
        return results;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        List<InventoryItems> inventoryItems = new ArrayList<>();
        uuidsOfPickedAccessories = new ArrayList<>();
        uuidsOfPickedBundles = new ArrayList<>();
        uuidsOfPickedItems = new ArrayList<>();

        mCurrentItemBundle = null;
        mCurrentEvent = null;

        int noBundles = getIntent().getExtras().getInt(SHOW_ITEMS_ACCESSORIES, 0);

        //Set current event if parameter is passed
        String currentEventUUID = getIntent().getExtras().getString(SPECIFIC_EVENT);
        if (currentEventUUID != null) {
            mCurrentEvent =
                    Event_Content.get(getApplicationContext()).getEventById(currentEventUUID);
        }

        //set current bundle if parameter is passed
        String currentBundleUUID = getIntent().getExtras().getString(SPECIFIC_BUNDLE);
        if (currentBundleUUID != null) {
            mCurrentItemBundle =
                    Bundle_Content.get(getApplicationContext()).getBundleById(currentBundleUUID);
            ArrayList<Item_Content.Item> bundleItems = mCurrentItemBundle.getItems();
            for(Item_Content.Item item: bundleItems){
                inventoryItems.add(item);
            }
        }

        addItemsAndAccessories(inventoryItems);

        if (noBundles == 0) {
            addBundles(inventoryItems);
        }

        if (mPickInventoryAdapter == null) {
            try{
                mPickInventoryAdapter = new pickItemAdapter(inventoryItems);
                recyclerView.setAdapter(mPickInventoryAdapter);
            }catch (Exception e){
                e.printStackTrace();
            }

        } else {
            mPickInventoryAdapter.setItems(inventoryItems);
            mPickInventoryAdapter.notifyDataSetChanged();
        }
        amountOfAccessories = new ArrayList<>();
        int numberOfAccessories = InventoryItems.getNumberOfAccessories(new ArrayList<InventoryItems>(inventoryItems));
        for (int i = 0; i < numberOfAccessories; i++) {
            amountOfAccessories.add(0);
        }
    }

    private void addItemsAndAccessories(List<InventoryItems> inventoryItems) {
        //show only Accessories and items
        //Add Items
        List<Item_Content.Item> items = Item_Content.get(this).getInventoryItems();
        for (Item_Content.Item item : items) {
            inventoryItems.add(item);
        }

        //Add Accessories
        List<Accessory_Content.Accessory> accessories = Accessory_Content.get(this).getAccessories();
        for (Accessory_Content.Accessory accessory : accessories) {
            inventoryItems.add(accessory);
            uuidsOfPickedAccessories.add(accessory.getId().toString());

        }
    }

    private void addBundles(List<InventoryItems> inventoryItems) {
        //Add Item Bundles
        List<Bundle_Content.ItemBundle> itemBundles = Bundle_Content.get(this).getBundles();
        for (Bundle_Content.ItemBundle itemBundle : itemBundles) {
            inventoryItems.add(itemBundle);
        }
    }

    private class pickItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<InventoryItems> mItems;

        pickItemAdapter(List<InventoryItems> items) {
            this.mItems = items;
        }

        public void setItems(List<InventoryItems> items) {
            mItems = items;
        }

        @Override
        public int getItemViewType(int position) {
            int returnValue = 0;
            switch (mItems.get(position).getType()) {
                case ACCESSORY:
                    returnValue = 0;
                    break;
                case BUNDLE:
                    returnValue = 1;
                    break;
                case ITEM:
                    returnValue = 2;
                    break;
            }
            return returnValue;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.content_pick_accessories, parent, false);
                    return new AccessoryHolder(view);
                case 1:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.content_pick_bundles, parent, false);
                    return new BundleHolder(view);
                case 2:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.content_pick_items, parent, false);
                    return new ItemHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    Accessory_Content.Accessory accessory = (Accessory_Content.Accessory) mItems.get(position);
                    AccessoryHolder accessoryHolder = (AccessoryHolder) holder;
                    accessoryHolder.bindAccessory(accessory);
                    break;
                case 1:
                    Bundle_Content.ItemBundle bundle = (Bundle_Content.ItemBundle) mItems.get(position);
                    BundleHolder bundleHolder = (BundleHolder) holder;
                    bundleHolder.bindBundle(bundle);
                    break;
                case 2:
                    Item_Content.Item item = (Item_Content.Item) mItems.get(position);
                    ItemHolder itemHolder = (ItemHolder) holder;
                    itemHolder.bindItem(item);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        //Accessory Holder
        private class AccessoryHolder extends RecyclerView.ViewHolder {
            View v;
            Accessory_Content.Accessory mAccessory;
            NumberPicker mNumberPicker;
            TextView mName;

            AccessoryHolder(View view) {
                super(view);
                this.v = view;
            }

            void bindAccessory(Accessory_Content.Accessory accessory) {
                this.mAccessory = accessory;
                bindWidgets();
                wireWidgets();
                setWidgets();
            }

            private void bindWidgets() {
                mName = (TextView) v.findViewById(R.id.contentPickAccessories_accessoryName_textView);
                mNumberPicker = (NumberPicker) v.findViewById(R.id.contentPickAccessories_amount_picker);
            }

            private void setWidgets() {
                mName.setText(mAccessory.getName());
                try{
                    if (mCurrentItemBundle != null) {
                        boolean found = false;
                        for (Accessory_Content.Accessory accessory : mCurrentItemBundle.getAccessories()) {
                                if (mAccessory.getId().equals(accessory.getId())) {
                                    int bundleValue =
                                            mCurrentItemBundle.getAccessories()
                                                    .get(mCurrentItemBundle.getAccessories().indexOf(accessory))
                                                    .getTotalQuantity();
                                    int index = InventoryItems.findLocationOfAccessoryInArray(new ArrayList<InventoryItems>(mItems),mAccessory);
                                    amountOfAccessories.set(index,bundleValue);
                                    setNumberPicker(bundleValue, (mAccessory.getOnHand() + bundleValue));
                                    found = true;
                                }
                        }
                        if(!found){
                            setNumberPicker(0,mAccessory.getOnHand());
                        }
                        if(mCurrentItemBundle.getAccessories().size() == 0){
                            setNumberPicker(0, mAccessory.getOnHand());
                        }
                    } else if (mCurrentEvent != null) {
                        boolean found = false;
                        for (Accessory_Content.Accessory accessory : mCurrentEvent.getAccessories()) {
                            if (mAccessory.getId().equals(accessory.getId())) {
                                int eventValue =
                                        mCurrentEvent.getAccessories()
                                                .get(mCurrentEvent.getAccessories().indexOf(accessory))
                                                .getTotalQuantity();
                                int index = InventoryItems.findLocationOfAccessoryInArray(new ArrayList<InventoryItems>(mItems),mAccessory);
                                amountOfAccessories.set(index,eventValue);
                                setNumberPicker(eventValue, (mAccessory.getOnHand() + eventValue));
                                found = true;
                            }
                        }
                        if(!found){
                            setNumberPicker(0,mAccessory.getOnHand());
                        }
                        if(mCurrentEvent.getAccessories().size() == 0){
                            setNumberPicker(0, mAccessory.getOnHand());
                        }
                    } else {
                        setNumberPicker(0, mAccessory.getOnHand());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            /**
             * Set the picker to the correct value for the bundle.
             * @param initialValue
             * @param maxValue
             */
            private void setNumberPicker(int initialValue, int maxValue) {
                mNumberPicker.setMinValue(0);
                mNumberPicker.setMaxValue(maxValue);
                int index = InventoryItems.findLocationOfAccessoryInArray(new ArrayList<InventoryItems>(mItems),mAccessory);
                if (amountOfAccessories.get(index) == initialValue) {
                    mNumberPicker.setValue(initialValue);
                } else {
                    mNumberPicker.setValue(amountOfAccessories.get(index));
                }
            }

            private void wireWidgets() {
                wireNameTextView();
                wireNumberPicker();
            }

            private void wireNameTextView() {
                mName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchAccessoryDetail(v);
                    }
                });
            }

            private void wireNumberPicker(){
                mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        int numberOfAccessories = InventoryItems.getNumberOfAccessories(new ArrayList<InventoryItems>(mItems));
                        if(numberOfAccessories != 0){
                            int index = InventoryItems.findLocationOfAccessoryInArray(new ArrayList<InventoryItems>(mItems),mAccessory);
                            int amount = newVal;
                            amountOfAccessories.set(index, amount);
                        }
                    }
                });
            }

            private void launchAccessoryDetail(View view) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_ID, mAccessory.getId().toString());
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_TYPE, mAccessory.getType().getValue());
                    //TODO add argument for two pane to switch between closing the activity or not.
                    Accessory fragment = new Accessory();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.pick_item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, InventoryItemDetailActivity.class);
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_ID, mAccessory.getId().toString());
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_TYPE, mAccessory.getType().getValue());
                    context.startActivity(intent);
                }
            }
        }

        //Bundle Holder
        private class BundleHolder extends RecyclerView.ViewHolder {
            View v;
            Bundle_Content.ItemBundle mItemBundle;
            CheckBox mPicked;
            TextView mName;

            public BundleHolder(View itemView) {
                super(itemView);
                this.v = itemView;
            }

            void bindBundle(Bundle_Content.ItemBundle itemBundle) {
                this.mItemBundle = itemBundle;
                bindWidgets();
                wireWidgets();
                setWidgets();
            }

            private void bindWidgets() {
                this.mName = (TextView) v.findViewById(R.id.contentPickBundles_bundleName_textView);
                this.mPicked = (CheckBox) v.findViewById(R.id.contentPickBundles_picked_checkBox);
            }

            private void setWidgets() {
                mName.setText(mItemBundle.getName());
                if (mCurrentEvent != null) {
                    for (Bundle_Content.ItemBundle itemBundle : mCurrentEvent.getItemBundles()) {
                        if (itemBundle.getId().equals(mItemBundle.getId())) {
                            mPicked.setChecked(true);
                        }
                    }
                }
            }

            private void wireWidgets() {
                wireNameTextView();
                wirePickedCheckBox();
            }

            private void wireNameTextView() {
                mName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchBundleDetailActivity(v);
                    }
                });
            }

            private void wirePickedCheckBox() {
                mPicked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        uuidsOfPickedBundles.add(mItemBundle.getId().toString());
                    }
                });
            }

            private void launchBundleDetailActivity(View view) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_ID, mItemBundle.getId().toString());
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_TYPE, mItemBundle.getType().getValue());
                    //TODO add argument for two pane to switch between closing the activity or not.
                    Item_Bundle fragment = new Item_Bundle();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.pick_item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, InventoryItemDetailActivity.class);
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_ID, mItemBundle.getId().toString());
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_TYPE, mItemBundle.getType().getValue());

                    context.startActivity(intent);
                }
            }
        }

        //Item Holder
        private class ItemHolder extends RecyclerView.ViewHolder {
            View v;
            Item_Content.Item mItem;
            CheckBox mPicked;
            TextView mName;

            ItemHolder(View view) {
                super(view);
                this.v = view;
            }

            void bindItem(Item_Content.Item item) {
                this.mItem = item;
                bindWidgets();
                wireWidgets();
                setWidgets();

            }

            void bindWidgets() {
                this.mName = (TextView) v.findViewById(R.id.contentPickItems_itemName_textView);
                this.mPicked = (CheckBox) v.findViewById(R.id.contentPickItems_picked_checkBox);
            }

            void setWidgets() {
                this.mName.setText(mItem.getName());
                if (mCurrentEvent != null) {
                    for (Item_Content.Item item : mCurrentEvent.getItems()) {
                        if (mItem.getId().equals(item.getId())) {
                            mPicked.setChecked(true);
                        }
                    }
                }
                if (mCurrentItemBundle != null) {
                    for (Item_Content.Item item : mCurrentItemBundle.getItems()) {
                        if (mItem.getId().equals(item.getId())) {
                            mPicked.setChecked(true);
                        }
                    }
                }
            }

            void wireWidgets() {
                wirePickedCheckBox();
                wireNameTextView();
            }

            void wirePickedCheckBox() {
                this.mPicked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                       if(isChecked){
                           uuidsOfPickedItems.add(mItem.getId().toString());
                       }else {
                           uuidsOfPickedItems.remove(mItem.getId().toString());
                       }
                    }
                });
            }

            void wireNameTextView() {
                this.mName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchInventoryItemDetail(v);
                    }
                });
            }

            private void launchInventoryItemDetail(View view) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_ID, mItem.getId().toString());
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_TYPE, mItem.getType().getValue());
                    Item fragment = new Item();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.pick_item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, InventoryItemDetailActivity.class);
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_ID, mItem.getId().toString());
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_TYPE, mItem.getType().getValue());

                    context.startActivity(intent);
                }
            }
        }
    }
}
