package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brian on 2/21/2017.
 */

public abstract class InventoryItems {
    public enum InventoryItemType{
        ACCESSORY("Accessory"),
        BUNDLE("Bundle"),
        ITEM("Item");

        String value;
        InventoryItemType(String value){
            this.value = value;
        }
        public String getValue(){
            return this.value;
        }
    }

    private InventoryItemType type;

    public InventoryItems(InventoryItemType type){
        this.type = type;
    }

    public InventoryItemType getType() {
        return type;
    }

    /**
     * Used in generating the parallel array for accessories in pick inventory activity.
     * @param inventoryItems Array list of the Super Class Inventory Items
     * @return number of Item that are of the Accessory Type
     */
    public static int getNumberOfAccessories(ArrayList<InventoryItems> inventoryItems){
        int numberOfItems = 0;
        for(InventoryItems inventoryItem : inventoryItems){
            if(inventoryItem.getType().equals(InventoryItemType.ACCESSORY)){
                numberOfItems = numberOfItems + 1;
            }
        }
        int blah;
        return numberOfItems;
    }
    public static int findLocationOfAccessoryInArray(ArrayList<InventoryItems> inventoryItems,
                                                     Accessory_Content.Accessory accessory){
        int location = 0;
        int numberOfFoundAccessories = -1;
        for(int i = 0; i < inventoryItems.size(); i++){
            if(inventoryItems.get(i).getType().equals(InventoryItemType.ACCESSORY)){
                numberOfFoundAccessories += 1;
                Accessory_Content.Accessory testAccessory = (Accessory_Content.Accessory) inventoryItems.get(i);
                if(testAccessory.getId().equals(accessory.getId())){
                    location = numberOfFoundAccessories;
                }
            }
        }
        return location;
    }

    public static List<InventoryItems> getFilteredItems(List<InventoryItems> list, String filter, Context context) {
        List<InventoryItems> filteredList = new ArrayList<>();

        for (int i=0; i<list.size(); i++) {
            Log.i("TAG", "Filter: " + filter + "\n" +
                    "Item: " + list.get(i).toString().toLowerCase() + "\n" +
                            "Equal? " + list.get(i).toString().toLowerCase().contains(filter.toLowerCase())
            );
            if (list.get(i).toString().toLowerCase().contains(filter.toLowerCase())){
                filteredList.add(list.get(i));
            }
        }
        return filteredList;
    }
}
