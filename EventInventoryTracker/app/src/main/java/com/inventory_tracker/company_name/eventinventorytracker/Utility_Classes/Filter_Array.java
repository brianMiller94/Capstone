package com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.InventoryItems;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brian on 4/23/2017.
 */

public class Filter_Array {

    public static ArrayList<InventoryItems> filterInventoryItemArray(List<InventoryItems> inventoryItems, String filter){
        ArrayList<InventoryItems> matchingItems = new ArrayList<>();

        for (InventoryItems inventoryItem:
             inventoryItems) {
            Accessory_Content.Accessory accessory;
            Bundle_Content.ItemBundle bundle;
            Item_Content.Item item;
            //Cast Inventory item into correct type and match off name
            switch (inventoryItem.getType()){
                case ACCESSORY:
                    accessory = (Accessory_Content.Accessory) inventoryItem;
                    String accessoryName = accessory.getName();
                    if(accessoryName.toLowerCase().contains(filter.toLowerCase())){
                        matchingItems.add(accessory);
                    }
                    break;
                case BUNDLE:
                    bundle = (Bundle_Content.ItemBundle) inventoryItem;
                    String bundleName = bundle.getName();
                    if(bundleName.toLowerCase().contains(filter.toLowerCase())){
                        matchingItems.add(bundle);
                    }
                    break;
                case ITEM:
                    item = (Item_Content.Item) inventoryItem;
                    String itemName = item.getName();
                    if(itemName.toLowerCase().contains(filter.toLowerCase())){
                        matchingItems.add(item);
                    }
                    break;
            }

        }

        return matchingItems;
    }

    public static ArrayList<Event_Content.Event> filterEventArray(ArrayList<Event_Content.Event> events, String filter){
        ArrayList<Event_Content.Event> matchingEvents = new ArrayList<>();
        for (Event_Content.Event event:
             events) {
            if(event.getName().toLowerCase().contains(filter.toLowerCase())){
                matchingEvents.add(event);
            }
        }
        return matchingEvents;
    }
}
