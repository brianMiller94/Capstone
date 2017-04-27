package com.inventory_tracker.company_name.eventinventorytracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.TextView;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.InventoryItems;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Accessory;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Item;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;


public class ItemListActivityWithSearch extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private InventoryItemAdapter mInventoryItemAdapter;
    private List<InventoryItems> mainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_inventory_items_with_search);

     //    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
     //   setSupportActionBar(toolbar);
     //    toolbar.setTitle(getTitle());

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
                        break;
                    case R.id.tab_inventory:
                        break;
                    case R.id.tab_events:
                        intent = new Intent(ItemListActivityWithSearch.this, EventListActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            }
        });

        setupRecyclerView((RecyclerView) findViewById(R.id.item_list));

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_inventory);
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
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        List<InventoryItems> inventoryItems = new ArrayList<>();

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

        if (mInventoryItemAdapter == null){
            mInventoryItemAdapter = new InventoryItemAdapter(inventoryItems);
            recyclerView.setAdapter(mInventoryItemAdapter);
        }else{
            mInventoryItemAdapter.setItems(inventoryItems);
            mInventoryItemAdapter.notifyDataSetChanged();
        }

        View includeLayout = findViewById(R.id.itemListLayout);

        EditText searchET = (EditText) includeLayout.findViewById(R.id.searchET);
        searchET.addTextChangedListener(new SearchInventory());
    }

    public class InventoryItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<InventoryItems> mItems;

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
            View view = null;
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
                    bundleHolder.bindBundleItem(bundle);
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

        // bundle holder
        public class BundleHolder extends RecyclerView.ViewHolder{
            public final View mView;

            public BundleHolder(View view){
                super(view);
                mView = view;
            }

            public void bindBundleItem(Bundle_Content.ItemBundle bundle){

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

        //item holder
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

    private class SearchInventory implements TextWatcher {
        private String filter = "";
        private boolean ignore = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 0) {
                filter = s.toString();
            }
            if (ignore) {
                mInventoryItemAdapter.mItems = InventoryItems.getFilteredItems(mainList, s.toString(), ItemListActivityWithSearch.this);;
                mInventoryItemAdapter.notifyDataSetChanged();
            }
            mInventoryItemAdapter.notifyDataSetChanged();
            ignore = !ignore;
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


}
