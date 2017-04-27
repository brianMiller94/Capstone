package com.inventory_tracker.company_name.eventinventorytracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Accessory;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Item_Bundle;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Item;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * Item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class InventoryItemDetailActivity extends AppCompatActivity {

    public static final String ARG_ITEM_TYPE = "item_type";
    public static final String ARG_ITEM_ID = "item_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_master_inventory_items);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            String valueOfType = getIntent().getStringExtra(ARG_ITEM_TYPE);
            String valueOfId = getIntent().getStringExtra(ARG_ITEM_ID);
            switch (valueOfType){
                case "Item":
                    generateItemDetail(arguments, valueOfId);
                    break;
                case "Accessory":
                    generateAccessoryDetail(arguments, valueOfId);
                    break;
                case "Bundle":
                    generateBundleDetail(arguments, valueOfId);
                    break;
            }
        }
    }

    private void generateBundleDetail(Bundle bundle, String bundleID){
        bundle.putString(ARG_ITEM_ID, bundleID);
        Item_Bundle bundleDetailFragment = new Item_Bundle();
        bundleDetailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, bundleDetailFragment)
                .commit();
    }

    private void generateAccessoryDetail(Bundle bundle, String accessoryId){
        bundle.putString(ARG_ITEM_ID,accessoryId);
        Accessory accessoryDetailFragment = new Accessory();
        accessoryDetailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, accessoryDetailFragment)
                .commit();
    }

    private void generateItemDetail(Bundle bundle, String itemId){
        bundle.putString(ARG_ITEM_ID, itemId);
        Item itemDetailFragment = new Item();
        itemDetailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, itemDetailFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
