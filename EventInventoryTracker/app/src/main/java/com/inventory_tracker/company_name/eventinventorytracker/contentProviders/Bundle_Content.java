package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Utility_Methods;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Accessory;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Bundles;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.BundlesTable.Columns;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Bundles_Have_Accessories;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Bundle_Content extends ContentHelper {

    private static Bundle_Content sBundleContent;
    private final ThreadLocal<Context> mcontext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;

    public Bundle_Content(Context context) {
        mcontext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mcontext.get()).getWritableDatabase();
    }

    public static Bundle_Content get(Context context) {
        if (sBundleContent == null) {
            sBundleContent = new Bundle_Content(context);
        }
        return sBundleContent;
    }

    public void add(ItemBundle bundle) {
        ContentValues values = getContentValues(bundle);

        mDatabase.insertOrThrow(
                DB_Schema.BundlesTable.NAME,
                null,
                values
        );
        addInventory(bundle);
    }

    public void update(ItemBundle bundle) {
        String uuidString = bundle.getId().toString();
        ContentValues values = getContentValues(bundle);

        mDatabase.update(
                DB_Schema.BundlesTable.NAME,
                values,
                columnWhereClauseFormat(Columns.ID),
                whereArgsFormatter(uuidString)
        );
        addInventory(bundle);
    }

    private void addInventory(ItemBundle bundle){
        //Add the Item to the bundle
        Item_Content.get(mcontext.get()).clearBundleMappings(bundle);
        for (Item_Content.Item item : bundle.getItems()) {
            item.setBundleId(bundle.getId());
            Item_Content.get(mcontext.get()).update(item);
        }

        //Add accessories entry
        Bundles_Have_Accessories_Content.get(mcontext.get()).deleteBundle(bundle);
        for (Accessory_Content.Accessory accessory : bundle.getAccessories()) {
            Bundles_Have_Accessories_Content.get(mcontext.get())
                    .addAccessoryEntry(accessory, bundle);
        }
    }

    public void delete(ItemBundle bundle) {
        String uuidString = bundle.getId().toString();

        if (Utility_Methods.isNetworkAvailable(mcontext.get())) {
            mDatabase.delete(
                    DB_Schema.BundlesTable.NAME,
                    columnWhereClauseFormat(Columns.ID),
                    whereArgsFormatter(uuidString)
            );
            //Clear Accessories
            Bundles_Have_Accessories_Content.get(mcontext.get()).deleteBundle(bundle);
            //Clear Events
            Events_Have_Bundles_Content.get(mcontext.get()).deleteBundle(bundle);
            //clean up Item links
            Item_Content.get(mcontext.get()).clearBundleMappings(bundle);

            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(bundle,mcontext.get()));

            Utility_Methods.createToast("Deleted Bundle",mcontext.get());
        } else {
            Utility_Methods.createToast("Sorry Must be connected to the internet",mcontext.get());
        }
    }

    public ItemBundle getBundleById(String id) {
        try (
                Bundles cursor = queryBundles(
                        columnWhereClauseFormat(Columns.ID),
                        whereArgsFormatter(id))
        ) {
            if (cursor.getCount() == 0) {
                return null;
            } else {
                cursor.moveToFirst();
                ItemBundle bundle = cursor.getBundle();
                addItemsToBundle(bundle);
                addAccessoriesToBundle(bundle);
                return bundle;
            }
        }
    }

    public ArrayList<ItemBundle> mapBundlesByUUID(ArrayList<String> uuidStrings) {
        ArrayList<ItemBundle> bundles = new ArrayList<>();
        for (String uuid : uuidStrings) {
            ItemBundle bundle = getBundleById(uuid);
            addItemsToBundle(bundle);
            addAccessoriesToBundle(bundle);
            bundles.add(bundle);
        }
        return bundles;
    }

    public void addItemsToBundle(ItemBundle bundle) {
        UUID uuid = bundle.getId();
        ArrayList<Item_Content.Item> items =
                new ArrayList<>
                        (Item_Content.get(mcontext.get())
                                .getInventoryItemsInBundle(uuid.toString()));
        bundle.setItems(items);
    }

    public void addAccessoriesToBundle(ItemBundle bundle) {
        ArrayList<Accessory_Content.Accessory> accessories = new ArrayList<>(
                Bundles_Have_Accessories_Content.get(mcontext.get()).
                        getAccessoriesForBundle(bundle)
        );
        bundle.setAccessories(accessories);
    }

    /**
     * Gets all bundles that are currently in the local database.
     *
     * @return list of all bundles in the database.
     */
    public List<ItemBundle> getBundles() {
        List<ItemBundle> bundles = new ArrayList<>();

        Bundles cursor = queryBundles(null, null);

        populateList(bundles, cursor);


        return bundles;
    }


    private void populateList(List<ItemBundle> listOfBundles, Bundles cursor) {
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                listOfBundles.add(cursor.getBundle());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
    }

    @Contract(pure = true)
    public static String createTable() {
        return "create table " + DB_Schema.BundlesTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                Columns.ID + "," +
                Columns.NAME + "," +
                Columns.DESCRIPTION + "," +
                Columns.BARCODE + ")";
    }

    private static ContentValues getContentValues(ItemBundle bundle) {
        ContentValues values = new ContentValues();

        values.put(Columns.ID, bundle.getId().toString());
        values.put(Columns.NAME, bundle.getName());
        values.put(Columns.DESCRIPTION, bundle.getDescription());
        values.put(Columns.BARCODE, bundle.getBarcode());

        return values;
    }

    private Bundles queryBundles(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                DB_Schema.BundlesTable.NAME,
                null, //Columns -- select all
                whereClause,
                whereArgs,
                null, // group by
                null, // having
                null // ordered by
        );
        return new Bundles(cursor);
    }

    @DynamoDBTable(tableName = "Bundles")
    public static class ItemBundle extends InventoryItems {
        UUID id;
        String name;
        String Description;
        String barcode;
        ArrayList<Item_Content.Item> items;
        ArrayList<Accessory_Content.Accessory> accessories;

        public ItemBundle() {
            super(InventoryItemType.BUNDLE);
            this.id = UUID.randomUUID();
            items = new ArrayList<>();
            accessories = new ArrayList<>();
        }

        public ItemBundle(UUID id) {
            super(InventoryItemType.BUNDLE);
            this.id = id;
            items = new ArrayList<>();
            accessories = new ArrayList<>();
        }

        @DynamoDBHashKey(attributeName = "ID")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = null;
            this.id = id;
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
            return Description;
        }

        public void setDescription(String description) {
            Description = description;
        }

        @DynamoDBAttribute(attributeName = "Barcode")
        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        @DynamoDBIgnore
        public ArrayList<Item_Content.Item> getItems() {
            return items;
        }

        @DynamoDBIgnore
        public void setItems(ArrayList<Item_Content.Item> items) {
            this.items = items;
        }

        @DynamoDBIgnore
        public ArrayList<Accessory_Content.Accessory> getAccessories() {
            return accessories;
        }

        @DynamoDBIgnore
        public void setAccessories(ArrayList<Accessory_Content.Accessory> accessories, ArrayList<Integer> numberPerAccessories) {
            this.accessories = accessories;
            for (int i = 0; i < accessories.size(); i++) {
                this.accessories.get(i).setOnHand(numberPerAccessories.get(i));
                this.accessories.get(i).setTotalQuantity(numberPerAccessories.get(i));
            }
        }

        @DynamoDBIgnore
        public void setAccessories(ArrayList<Accessory_Content.Accessory> accessories) {
            this.accessories = accessories;
        }

        @DynamoDBIgnore
        public void addItem(Item_Content.Item item) {
            this.items.add(item);
        }

        @DynamoDBIgnore
        public void removeItem(Item_Content.Item item) {
            this.items.remove(item);
        }

        public void addAccessory(Accessory_Content.Accessory accessory, int quantity) {
            Accessory_Content.Accessory newAccessory = new Accessory_Content.Accessory(accessory.getId());
            newAccessory.setOnHand(quantity);
            newAccessory.setTotalQuantity(quantity);
            this.accessories.add(newAccessory);
        }
        public void removeAccessory(Accessory_Content.Accessory accessory){
            this.accessories.remove(findAccessoryById(accessory.getId().toString()));
            int blah = 5;
        }
        public void dropAll(){
            this.accessories = new ArrayList<>();
            this.items = new ArrayList<>();
        }
        private Accessory_Content.Accessory findAccessoryById(String uuidOfAccessory){
            Accessory_Content.Accessory accessory = null;
            for(Accessory_Content.Accessory accessoryPart : this.accessories){
                if(accessoryPart.getId().toString().equals(uuidOfAccessory)){
                    accessory = accessoryPart;
                }
            }
            return accessory;
        }

        public void deleteAccessory(Accessory_Content.Accessory accessory) {
            this.accessories.remove(accessory);
        }

        public static void validateBundle(ItemBundle itemBundle) throws Exception{
            try {
                if(itemBundle.getName() == null || itemBundle.getName().equals("")){
                    throw new Exception("Bundle name can't be null");
                }
                if(itemBundle.getDescription() == null || itemBundle.getDescription().equals("")){
                    throw new Exception("Bundle description can't be null");
                }
                if(itemBundle.getBarcode() == null || itemBundle.getBarcode().equals("")){
                    throw new Exception("Bundle barcode can't be null");
                }
            }catch (Exception e){
                e.printStackTrace();
                throw e;
            }
        }
    }


}

