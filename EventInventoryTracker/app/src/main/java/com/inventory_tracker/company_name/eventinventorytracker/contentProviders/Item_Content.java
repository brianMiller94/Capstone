package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import android.util.Log;

import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Utility_Methods;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.DateConverter;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.ItemsTable.Columns;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Items;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import org.jetbrains.annotations.Contract;


public class Item_Content extends ContentHelper {

    private static Item_Content sItemsContent;
    private final ThreadLocal<Context> mcontext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;

    public Item_Content(Context context){
        mcontext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mcontext.get()).getWritableDatabase();
    }

    public static Item_Content get(Context context){
        if (sItemsContent == null){
            sItemsContent = new Item_Content(context);
        }
        return sItemsContent;
    }

    public void add(Item item){
        ContentValues values = getContentValues(item);
        
        mDatabase.insertOrThrow(
                DB_Schema.ItemsTable.NAME,
                null,
                values);
    }

    public void update(Item item){
        String uuidString = item.getId().toString();
        ContentValues values = getContentValues(item);

        mDatabase.update(
                DB_Schema.ItemsTable.NAME,
                values,
                columnWhereClauseFormat(Columns.ID),
                whereArgsFormatter(uuidString)
        );
    }

    void clearBundleMappings(Bundle_Content.ItemBundle itemBundle){
        ArrayList<Item> items = new ArrayList<>(getInventoryItemsInBundle(itemBundle.getId().toString()));
        for (Item item:
             items) {
            item.setBundleId(null);
            this.update(item);
        }
    }

    public void delete(Item item){
        String uuidString = item.getId().toString();

        if (Utility_Methods.isNetworkAvailable(mcontext.get())) {
            mDatabase.delete(
                    DB_Schema.ItemsTable.NAME,
                    columnWhereClauseFormat(Columns.ID),
                    whereArgsFormatter(uuidString)
            );
            Events_Have_Items_Content.get(mcontext.get()).deleteItem(item);
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(item,mcontext.get()));
            Utility_Methods.createToast("Item Deleted",mcontext.get());
        } else {
            Utility_Methods.createToast("Must be connected to a network",mcontext.get());
        }
    }

    public Item getInventoryItemById(String id){
        try(
                Items cursor = queryInventoryItems(
                columnWhereClauseFormat(Columns.ID),
               whereArgsFormatter(id))
        ){
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getItem();
        }
    }

    public List<Item> getInventoryNameLike(String nameLikeString){
        try{
            Items cursor = queryInventoryItems(
                    columnLikeWhereClauseFormat(Columns.ID),
                    whereLikeArgsFormatter(nameLikeString)
            );
            if(cursor.getCount() == 0){
                return null;
            }else{
                ArrayList<Item> itemsFound = new ArrayList<>();
                populateListNoBundledItems(itemsFound, cursor);
                return itemsFound;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Item> mapInventoryItemsByUUID(ArrayList<String> uuidStrings){
        ArrayList<Item> items = new ArrayList<>();
        for (String uuid: uuidStrings) {
            items.add(getInventoryItemById(uuid));
        }
        return items;
    }

    /**
     * Gets all inventory Items that are not part of a bundle
     * @return List of Items not in a bundle
     */
    public List<Item> getInventoryItems(){
        List<Item> items = new ArrayList<>();

        Items cursor = queryInventoryItems(null, null);

        populateListNoBundledItems(items,cursor);

        return items;
    }

    public List<Item> getInventoryItemsForSync(){
        List<Item> items = new ArrayList<>();

        Items cursor = queryInventoryItems(null, null);

        populateList(items,cursor);

        return items;
    }

    /**
     * Gets all inventory items that are part of a particular bundle
     * @param bundleIdUUIDString String of the bundle UUID
     * @return List of items in bundle passed
     */
    public List<Item> getInventoryItemsInBundle(String bundleIdUUIDString){
        List<Item> items = new ArrayList<>();

        Items cursor = queryInventoryItems(
                columnWhereClauseFormat(Columns.BUNDLE_ID),
                whereArgsFormatter(bundleIdUUIDString)
        );

        populateList(items, cursor);

        return items;
    }

    private void populateList(List<Item> listsOfItems, Items cursor){
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                listsOfItems.add(cursor.getItem());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
    }

    private void populateListNoBundledItems(List<Item> listsOfItems, Items cursor){
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Item newItem = cursor.getItem();
                if(newItem.getBundleId() == null){
                    listsOfItems.add(newItem);
                }
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
    }

    @Contract(pure = true)
    public static String createTable(){
        return "create table " + DB_Schema.ItemsTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                Columns.ID + "," +
                Columns.BUNDLE_ID + "," +
                Columns.NAME + "," +
                Columns.DESCRIPTION + "," +
                Columns.BARCODE + "," +
                Columns.SERIAL_NUMBER + ")";
    }


    private static ContentValues getContentValues(Item item){
        ContentValues values = new ContentValues();

        values.put(Columns.ID, item.getId().toString());
        UUID bundleId = item.getBundleId();
        if(bundleId == null){
            values.put(Columns.BUNDLE_ID, "");
        }else{
            values.put(Columns.BUNDLE_ID, bundleId.toString());
        }
        values.put(Columns.NAME, item.getName());
        values.put(Columns.DESCRIPTION, item.getDescription());
        values.put(Columns.BARCODE, item.getBarcode());
        values.put(Columns.SERIAL_NUMBER, item.getSerialNumber());

        return values;
    }

    private Items queryInventoryItems(String whereClause, String [] whereArgs){
        Cursor cursor = mDatabase.query(
                DB_Schema.ItemsTable.NAME,
                null, //Columns -- select all
                whereClause,
                whereArgs,
                null, // group by
                null, // having
                null //ordered by
        );

        return new Items(cursor);
    }
    /**
     * A single Item, used by the content provider.     *
     */
    @DynamoDBTable(tableName = "items")
    public static class Item extends InventoryItems {

        UUID id;
        UUID bundleId;
        String name;
        String description;
        String barcode;
        String serialNumber;


        public Item() {
            super(InventoryItemType.ITEM);
            this.id = UUID.randomUUID();
        }

        public Item(UUID id){
            super(InventoryItemType.ITEM);
            this.id = id;
        }

        public Item(UUID id, UUID bundleId){
            super(InventoryItemType.ITEM);
            this.id = id;
            this.bundleId = bundleId;
        }

        @DynamoDBHashKey(attributeName = "ID")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getId() {
            return id;
        }

        public void setId(UUID id){
            this.id = null;
            this.id = id;
        }
        @DynamoDBAttribute(attributeName = "bundleId")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getBundleId() {
            try{
                return bundleId;
            }catch (Exception e){
                return null;
            }
        }

        public void setBundleId(UUID bundleId) {
            this.bundleId = bundleId;
        }

        @DynamoDBAttribute(attributeName = "Name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @DynamoDBAttribute(attributeName = "Description")
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @DynamoDBAttribute(attributeName = "Barcode")
        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        @DynamoDBAttribute(attributeName = "SerialNumber")
        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    '}';
        }

        public static void validateItem(Item item) throws Exception{
            try {
                if(item.getName() == null || item.getName().equals("")){
                    throw new Exception("Item name can't be null");
                }
                if(item.getDescription() == null || item.getDescription().equals("")){
                    throw new Exception("Item description can't be null");
                }
                if(item.getBarcode() == null || item.getBarcode().equals("")){
                    throw new Exception("Item barcode can't be null");
                }
                if(item.getSerialNumber() == null || item.getSerialNumber().equals("")){
                    throw new Exception("Item Serial number can't be null");
                }
            }catch (Exception e){
                e.printStackTrace();
                throw e;
            }
        }
    }
}
