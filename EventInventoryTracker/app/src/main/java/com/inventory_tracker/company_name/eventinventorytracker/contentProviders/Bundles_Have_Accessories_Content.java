package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Bundles_Have_Accessories_table.Columns;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Accessories;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Bundles_Have_Accessories;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bundles_Have_Accessories_Content extends ContentHelper {

    private static Bundles_Have_Accessories_Content sBundles_have_Accessories_Content;
    private final ThreadLocal<Context> mContext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;

    private Bundles_Have_Accessories_Content(Context context) {
        mContext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mContext.get()).getWritableDatabase();
    }

    public static Bundles_Have_Accessories_Content get(Context context) {
        if (sBundles_have_Accessories_Content == null) {
            sBundles_have_Accessories_Content = new Bundles_Have_Accessories_Content(context);
        }
        return sBundles_have_Accessories_Content;
    }

    public void add(Bundle_Has_Accessory Bundle_Has_Accessory) {
        ContentValues values = getContentValues(Bundle_Has_Accessory);

        mDatabase.insertOrThrow(
                DB_Schema.Bundles_Have_Accessories_table.NAME,
                null,
                values
        );
    }

    public void update(Bundle_Has_Accessory Bundle_Has_Accessory) {
        String uuidOfBundle = Bundle_Has_Accessory.getBundleId().toString();
        String uuidOfAccessory = Bundle_Has_Accessory.getAccessoryId().toString();

        ContentValues values = getContentValues(Bundle_Has_Accessory);

        mDatabase.update(
                DB_Schema.Bundles_Have_Accessories_table.NAME,
                values,
                twoColumnWhereClauseFormat(Columns.BUNDLE_ID, Columns.ACCESSORIES_ID),
                whereArgsFormatter(uuidOfBundle, uuidOfAccessory)
        );
    }

    void deleteBundle(Bundle_Content.ItemBundle itemBundle) {
        String uuidString = itemBundle.getId().toString();

        //clean up Accessories inventory count
        ArrayList<Accessory_Content.Accessory> accessories =new ArrayList<>(getAccessoriesForBundle(itemBundle));
        for (Accessory_Content.Accessory accessory:
             accessories) {
            Accessory_Content.Accessory parentAccessory =
                    Accessory_Content.get(mContext.get()).getAccessoryById(accessory.getId().toString());
            parentAccessory.setOnHand(parentAccessory.getOnHand() + accessory.getTotalQuantity());
            Accessory_Content.get(mContext.get()).update(parentAccessory);
        }

        //Delete Bundle / Accessories Map entry from database
        ArrayList<Bundle_Has_Accessory> mappings = getMappingsForItemBundle(itemBundle);

        mDatabase.delete(
                DB_Schema.Bundles_Have_Accessories_table.NAME,
                columnWhereClauseFormat(Columns.BUNDLE_ID),
                whereArgsFormatter(uuidString)
        );

        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(mappings.get(i),mContext.get()));
        }

    }

    void deleteAccessory(Accessory_Content.Accessory accessory) {
        String uuidString = accessory.getId().toString();
        ArrayList<Bundle_Has_Accessory> mappings = getMappingsForAccessory(accessory);
        mDatabase.delete(
                DB_Schema.Bundles_Have_Accessories_table.NAME,
                columnWhereClauseFormat(Columns.ACCESSORIES_ID),
                whereArgsFormatter(uuidString)
        );
        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(mappings.get(i),mContext.get()));
        }
    }

    private ArrayList<Bundle_Has_Accessory> getMappingsForAccessory(Accessory_Content.Accessory accessory){
        ArrayList<Bundle_Has_Accessory> mappings = new ArrayList<>();
        try{
            Bundles_Have_Accessories cursor = queryBundleHasAccessories(
                    columnWhereClauseFormat(Columns.ACCESSORIES_ID),
                    whereArgsFormatter(accessory.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }else{
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    mappings.add(cursor.getBundle_have_accessories());
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mappings;
    }

    private ArrayList<Bundle_Has_Accessory> getMappingsForItemBundle(Bundle_Content.ItemBundle bundle){
        ArrayList<Bundle_Has_Accessory> mappings = new ArrayList<>();
        try{
            Bundles_Have_Accessories cursor = queryBundleHasAccessories(
                    columnWhereClauseFormat(Columns.BUNDLE_ID),
                    whereArgsFormatter(bundle.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }else{
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    mappings.add(cursor.getBundle_have_accessories());
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mappings;
    }

    /**
     * Returns a List of accessory objects that map to the passed bundle.
     * Queries: Bundle_Has_Accessories
     * on: Item Bundle UUID
     * Cursor Has: UUID of Item bundle, UUID of accessory, quantity in bundle.
     *
     * @param itemBundle This is the bundle you want to get the accessories for
     * @return List of Accessories
     * @author Brian Miller
     */
    List<Accessory_Content.Accessory> getAccessoriesForBundle(Bundle_Content.ItemBundle itemBundle) {
        ArrayList<Accessory_Content.Accessory> accessories = new ArrayList<>();
        String uuidString = itemBundle.getId().toString();
        try {
            Bundles_Have_Accessories cursor = queryBundleHasAccessories(
                    columnWhereClauseFormat(Columns.BUNDLE_ID),
                    whereArgsFormatter(uuidString));
            if (cursor.getCount() == 0) {
                return accessories;
            } else {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Bundles_Have_Accessories_Content.Bundle_Has_Accessory bundle_has_accessory = cursor.getBundle_have_accessories();
                    Accessory_Content.Accessory accessory =
                            Accessory_Content.get(mContext.get()).
                                    getAccessoryById(bundle_has_accessory.getAccessoryId().toString());
                    accessory.setOnHand(bundle_has_accessory.getQuantity());
                    accessory.setTotalQuantity(bundle_has_accessory.getQuantity());
                    accessories.add(accessory);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessories;
    }

    /**
     * Although I can't imagine this being used, it is here to future proof the application
     * this will take an accessory and return all bundles related to that accessory.
     *
     * @param accessory the accessory you want to check against
     * @return a list of Item bundles that have that accessory
     * @author Brian Miller
     */
    List<Bundle_Content.ItemBundle> getBundleForAccessories(Accessory_Content.Accessory accessory) {
        //Create empty array of object to create
        ArrayList<Bundle_Content.ItemBundle> itemBundles = new ArrayList<>();
        //get uuid of searching object, standard to other Content manager get methods
        String uuidString = accessory.getId().toString();
        //Start Try block to catch errors
        try {
            //Create cursor for many to many table, searching on uuid of passed object
            Bundles_Have_Accessories cursor = queryBundleHasAccessories(
                    columnWhereClauseFormat(Columns.ACCESSORIES_ID),
                    whereArgsFormatter(uuidString));
            //Return empty array if there are no mattches
            if (cursor.getCount() == 0) {
                return itemBundles;
            } else {
                //Loop through cursor
                //Cursor starts at index -1, needs this command to start at first record.
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    //Get individual many to many linking class object
                    Bundles_Have_Accessories_Content.Bundle_Has_Accessory bundle_has_accessory = cursor.getBundle_have_accessories();
                    //Create object that was searched for
                    Bundle_Content.ItemBundle itemBundle =
                            Bundle_Content.get(mContext.get()).
                                    getBundleById(bundle_has_accessory.getBundleId().toString());
                    //Add to array list
                    itemBundles.add(itemBundle);
                    //go to next cursor record.
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Return filled array list.
        return itemBundles;
    }

    public Bundle_Has_Accessory getSpecificEntry(UUID accessoryId, UUID bundleID){
        try {
            Bundles_Have_Accessories cursor = queryBundleHasAccessories(
                    twoColumnWhereClauseFormat(Columns.ACCESSORIES_ID,Columns.BUNDLE_ID),
                    whereArgsFormatter(accessoryId.toString(),bundleID.toString())
            );
            if(cursor.getCount() == 0){
                return null;
            }else {
                cursor.moveToFirst();
                return cursor.getBundle_have_accessories();
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Bundle_Has_Accessory> getAllEntries(){
        List<Bundle_Has_Accessory> bundle_has_accessories = new ArrayList<>();
        try (Bundles_Have_Accessories cursor = queryBundleHasAccessories(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                bundle_has_accessories.add(cursor.getBundle_have_accessories());
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bundle_has_accessories;
    }

    /**
     * This will add an accessory to a bundle.
     *
     * @param accessoryToAddToBundle the accessory to add
     * @param bundle                 the bundle to add it to
     * @author Brian Miller
     */
    void addAccessoryEntry(Accessory_Content.Accessory accessoryToAddToBundle, Bundle_Content.ItemBundle bundle) {
        //Create many to many entry
        int accessoryQuantity = accessoryToAddToBundle.getTotalQuantity();
        Bundle_Has_Accessory bundle_has_accessory =
                new Bundle_Has_Accessory(bundle.getId(), accessoryToAddToBundle.getId(), accessoryQuantity);
        this.add(bundle_has_accessory);
        //update parent accessory object's quantity
        Accessory_Content.Accessory updatedAccessory = Accessory_Content.get(mContext.get())
                .getAccessoryById(accessoryToAddToBundle.getId().toString());
        updatedAccessory.setOnHand(updatedAccessory.getOnHand() - accessoryQuantity);
        Accessory_Content.get(mContext.get()).update(updatedAccessory);
    }

    /**
     * @return SQL String to create the Bundles_Have_Accessories table.
     * @author Brian Miller
     */

    public static String createTable() {
        return "create table " + DB_Schema.Bundles_Have_Accessories_table.NAME + "(" +
                Columns.ACCESSORIES_ID + "," +
                Columns.BUNDLE_ID + "," +
                Columns.QUANTITY + ")";
    }

    /**
     * Get the content values for the Bundle_Has_Accessory content.
     *
     * @param bundleHasAccessories Item to pull into content values
     * @return Content values of Item matching the Bundle_Has_Accessories table
     * @author Brian Miller
     */
    private static ContentValues getContentValues(Bundle_Has_Accessory bundleHasAccessories) {
        ContentValues values = new ContentValues();

        values.put(Columns.ACCESSORIES_ID, bundleHasAccessories.getAccessoryId().toString());
        values.put(Columns.BUNDLE_ID, bundleHasAccessories.getBundleId().toString());
        values.put(Columns.QUANTITY, bundleHasAccessories.getQuantity());

        return values;
    }

    /**
     * Generic Query for bundle has accessories table. Will return all columns,only allows for searching based off a column
     * no group by, having, order by, or column selecting is allowed.
     *
     * @param whereClause The column you are searching on
     * @param whereArgs The value to search for
     * @return a Bundles_Have_Accessories cursor
     * @author Brian Miller
     */
    private Bundles_Have_Accessories queryBundleHasAccessories(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                DB_Schema.Bundles_Have_Accessories_table.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new Bundles_Have_Accessories(cursor);
    }

    /**
     * Many to many class, mapping Bundles and Accessories
     * Must be passed a full object to be created, only allows for updating the quantity
     * If you want to add a different Accessory you will need to create a new Object.
     * @author Brian Miller
     */
    @DynamoDBTable(tableName = "Bundle_Has_Accessory")
    public static class Bundle_Has_Accessory {
        UUID bundleId;
        UUID accessoryId;
        int quantity;

        /**
         * This is ONLY for amazon sync, do you use in normal creation of instance
         */
        public Bundle_Has_Accessory(){

        }

        public Bundle_Has_Accessory(UUID bundleId, UUID accessoryId, int quantity) {
            this.bundleId = bundleId;
            this.accessoryId = accessoryId;
            this.quantity = quantity;
        }

        @DynamoDBHashKey(attributeName = "bundleId")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getBundleId() {
            return bundleId;
        }

        public void setBundleId(UUID bundleId) {
            this.bundleId = null;
            this.bundleId = bundleId;
        }

        public void setAccessoryId(UUID accessoryId) {
            this.accessoryId = null;
            this.accessoryId = accessoryId;
        }

        @DynamoDBHashKey(attributeName = "accessoryId")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getAccessoryId() {
            return accessoryId;
        }

        @DynamoDBAttribute(attributeName = "quantity")
        int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
