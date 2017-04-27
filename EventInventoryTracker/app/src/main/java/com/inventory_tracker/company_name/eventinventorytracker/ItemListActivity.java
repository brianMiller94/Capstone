package com.inventory_tracker.company_name.eventinventorytracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.*;
import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.CheckIn;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_View;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Filter_Array;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.InventoryItems;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.*;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Accessory;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Event;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Item;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Item_Bundle;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_async;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;


public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private InventoryItemAdapter mInventoryItemAdapter;
    EditText searchBar;

    List<InventoryItems> inventoryItems;

    int selected_position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_inventory_items);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());



        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.item_detail_container) != null) {
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(ItemListActivity.this);
                        builder.setTitle("Pick a Category");
                        builder.setItems(categories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = null;
                                switch (which){
                                    case 0:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item.class);
                                        break;
                                    case 1:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory.class);
                                        break;
                                    case 2:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item_Bundle.class);
                                        break;
                                    case 3:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Event.class);
                                        break;
                                    default:
                                        Toast.makeText(ItemListActivity.this, "no item", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(i);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.tab_inventory:
                        break;
                    case R.id.tab_events:
                        intent = new Intent(ItemListActivity.this, EventListActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0,0);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(ItemListActivity.this);
                        builder.setTitle("Pick a Category");
                        builder.setItems(categories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = null;
                                switch (which){
                                    case 0:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item.class);
                                        break;
                                    case 1:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory.class);
                                        break;
                                    case 2:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item_Bundle.class);
                                        break;
                                    case 3:
                                        i = new Intent(ItemListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Event.class);
                                        break;
                                    default:
                                        Toast.makeText(ItemListActivity.this, "no item", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(i);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.tab_inventory:
                        break;
                    case R.id.tab_events:
                        intent = new Intent(ItemListActivity.this, EventListActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            }
        });

        searchBar = (EditText) findViewById(R.id.searchET);
        wireSearchEditText();
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
                new AlertDialog.Builder(ItemListActivity.this)
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
    public void onResume(){
        super.onResume();
        setupRecyclerView((RecyclerView) findViewById(R.id.item_list));
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_inventory);
    }

    @Override
    public void onPause(){
        super.onPause();
        overridePendingTransition(0,0);
    }

    private void wireSearchEditText(){
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchBar.getText().toString().equals("")){
                    addTotalInventory();
                }else {
                    addTotalInventory();
                    inventoryItems = Filter_Array.filterInventoryItemArray(inventoryItems,s.toString());
                }
                mInventoryItemAdapter.setItems(inventoryItems);
                mInventoryItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        addTotalInventory();

        if (mInventoryItemAdapter == null){
            mInventoryItemAdapter = new InventoryItemAdapter(inventoryItems);
            recyclerView.setAdapter(mInventoryItemAdapter);
        }else{
            mInventoryItemAdapter.setItems(inventoryItems);
            mInventoryItemAdapter.notifyDataSetChanged();
        }
    }

    private void addTotalInventory(){
        inventoryItems = new ArrayList<>();

        //Add Items
        //List<Item_Content.Item> items = Item_Content.get(this).getInventoryItems("WHERE 1=1", new String[0]);
        List<Item_Content.Item> items = Item_Content.get(this).getInventoryItems();
        for(Item_Content.Item item: items){
            inventoryItems.add(item);
        }
        //Add Accessories
        List<Accessory_Content.Accessory> accessories = Accessory_Content.get(this).getAccessories();
        for(Accessory_Content.Accessory accessory: accessories){
            inventoryItems.add(accessory);
        }

        //Add Bundles
        List<Bundle_Content.ItemBundle> bundles = Bundle_Content.get(this).getBundles();
        for(Bundle_Content.ItemBundle bundle: bundles){
            inventoryItems.add(bundle);
        }
    }

    private class InventoryItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private List<InventoryItems> mItems;
        private int focusedItem = 0;

            public InventoryItemAdapter(List<InventoryItems> items) {
                mItems = items;
            }

            public void setItems(List<InventoryItems> items){
                mItems = items;
            }

            @Override
            public int getItemViewType(int position) {
                int returnValue = 0;
                switch (mItems.get(position).getType()){
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
            switch (viewType){
                case 0:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.content_accessories, parent, false);
                    return new AccessoryHolder(view);
                case 1:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.content_bundles ,parent,false);
                    return new BundleHolder(view);
                case 2:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.contenet_items, parent,false);
                    return new ItemHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            switch (holder.getItemViewType()){
                case 0:
                    Accessory_Content.Accessory accessory = (Accessory_Content.Accessory) mItems.get(position);
                    AccessoryHolder accessoryHolder = (AccessoryHolder) holder;
                    accessoryHolder.bindAccessory(accessory);
                    break;
                case 1:
                    Bundle_Content.ItemBundle bundle = (Bundle_Content.ItemBundle) mItems.get(position);
                    BundleHolder bundleHolder = (BundleHolder) holder;
                    bundleHolder.bindItemBundle(bundle);
                    break;
                case 2:
                    Item_Content.Item item = (Item_Content.Item) mItems.get(position);
                    ItemHolder itemHolder = (ItemHolder) holder;
                    itemHolder.bindInventoryItem(item);
                    break;
            }

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }



        private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
            int tryFocusItem = focusedItem + direction;

            // If still within valid bounds, move the selection, notify to redraw, and scroll
            if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
                notifyItemChanged(focusedItem);
                focusedItem = tryFocusItem;
                notifyItemChanged(focusedItem);
                lm.scrollToPosition(focusedItem);
                return true;
            }

            return false;
        }

        // bundle holder
        class BundleHolder extends RecyclerView.ViewHolder{
            final View mView;
            //Widgets
            private TextView mItemBundleName;

            //Item Bundle

            Bundle_Content.ItemBundle mItemBundle;

            BundleHolder(View view){
                super(view);
                mView = view;
            }

            void bindItemBundle(Bundle_Content.ItemBundle bundle){
                this.mItemBundle = bundle;

                //Initialize widgets
                mItemBundleName = (TextView) mView.findViewById(R.id.contentBundles_Name_TextView);

                //Set Widgets
                mItemBundleName.setText(mItemBundle.getName());

                //Wire Widgets
                mItemBundleName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchBundleDetailActivity(mView);
                    }
                });
            }
            private void launchBundleDetailActivity(View view){
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_ID, mItemBundle.getId().toString());
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_TYPE, mItemBundle.getType().getValue());
                    //TODO add argument for two pane to switch between closing the activity or not.
                    Item_Bundle fragment = new Item_Bundle();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
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

        //accessory holder
        public class AccessoryHolder extends RecyclerView.ViewHolder{
            public final View mView;

            //Widgets
            private TextView mAccessoryName;
            private TextView mDescription;
            private TextView mOnHand;

            //Accessory
            private Accessory_Content.Accessory mAccessory;

            public AccessoryHolder(View view){
                super(view);
                mView = view;
            }
            public void bindAccessory(Accessory_Content.Accessory accessory){
                this.mAccessory = accessory;

                //Init Widgets
                mAccessoryName =
                        (TextView) mView.findViewById(R.id.contentAccessories_Name_TextView);

                //Set widget properties
                mAccessoryName.setText(mAccessory.getName());

                //wire widgets
                mAccessoryName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchAccessoryDetail(mView);
                    }
                });
            }
            private void launchAccessoryDetail(View view){
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_ID, mAccessory.getId().toString());
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_TYPE, mAccessory.getType().getValue());
                    //TODO add argument for two pane to switch between closing the activity or not.
                    Accessory fragment = new Accessory();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
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

        //Item holder
        public class ItemHolder extends RecyclerView.ViewHolder {
            public final View mView;
            private TextView mItemName;
            //private TextView mDescription;
            //private CheckBox mCheckedIn;
            public Item_Content.Item mItem;

            public ItemHolder(View view) {
                super(view);
                mView = view;
            }

            private void wireItemNameTextView(){
                mItemName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchInventoryItemDetail(view);
                    }
                });
            }

            private void wireDescriptionTextView(){

            }

            private void launchInventoryItemDetail(View view){
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_ID, mItem.getId().toString());
                    arguments.putString(InventoryItemDetailActivity.ARG_ITEM_TYPE, mItem.getType().getValue());
                    //TODO add argument for two pane to switch between closing the activity or not.
                    Item fragment = new Item();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, InventoryItemDetailActivity.class);
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_ID, mItem.getId().toString());
                    intent.putExtra(InventoryItemDetailActivity.ARG_ITEM_TYPE, mItem.getType().getValue());

                    context.startActivity(intent);
                }
            }

            public void bindInventoryItem(Item_Content.Item item)
            {
                mItem = item;

                //init widgets

                mItemName = (TextView) mView.findViewById(R.id.item_content_ItemName_TextView);

                //wire events
                wireItemNameTextView();

                //set values
                mItemName.setText(item.getName());
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }
    }


}
