package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Utility_Methods;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.AccessoriesTable;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Accessories;

/**
 * Created by brian on 1/16/2017.
 */

public class Accessory_Content extends ContentHelper {

    private static Accessory_Content sAccessoryContent;
    private final ThreadLocal<Context> mContext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;

    private Accessory_Content(Context context){
        mContext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mContext.get()).getWritableDatabase();
    }

    public static Accessory_Content get(Context context){
        if(sAccessoryContent == null){
            sAccessoryContent = new Accessory_Content(context);
        }
        return sAccessoryContent;
    }


    public void add(Accessory accessory) {
        ContentValues values = getContentValues(accessory);
        mDatabase.insertOrThrow(AccessoriesTable.NAME,null,values);
    }


    public void update(Accessory accessory) {
        String uuidString = accessory.getId().toString();
        ContentValues values = getContentValues(accessory);

        mDatabase.update(
                AccessoriesTable.NAME,
                values,
                columnWhereClauseFormat(AccessoriesTable.Columns.ID),
                whereArgsFormatter(uuidString));
    }


    public void delete(Accessory accessory) {
        String uuidString = accessory.getId().toString();

        if (Utility_Methods.isNetworkAvailable(mContext.get())) {
            mDatabase.delete(
                    AccessoriesTable.NAME,
                    columnWhereClauseFormat(AccessoriesTable.Columns.ID),
                    whereArgsFormatter(uuidString));
            Events_Have_Accessories_Content.get(mContext.get()).deleteAccessory(accessory);
            Bundles_Have_Accessories_Content.get(mContext.get()).deleteAccessory(accessory);
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(accessory, mContext.get()));

            Utility_Methods.createToast("Deleted Accessory", mContext.get());
        }else{
            Utility_Methods.createToast("Sorry must be connected to network", mContext.get());
        }
    }

    public Accessory getAccessoryById(String id){
        try (Accessories cursor = queryAccessories(
                columnWhereClauseFormat(AccessoriesTable.Columns.ID),
                whereArgsFormatter(id)
        )) {
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getAccessory();
        }
    }

    public ArrayList<Accessory> mapAccessoriesByUUID(ArrayList<String> uuidStrings){
        ArrayList<Accessory> accessories = new ArrayList<>();
        for (String uuidString:uuidStrings) {
            accessories.add(getAccessoryById(uuidString));
        }
        return accessories;
    }

    public List<Accessory> getAccessories(){
        List<Accessory> accessories = new ArrayList<>();

        Accessories cursor = queryAccessories(null,null);

        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                accessories.add(cursor.getAccessory());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return accessories;
    }

    public static String createTable(){
        return "create table " + AccessoriesTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                AccessoriesTable.Columns.ID + "," +
                AccessoriesTable.Columns.NAME + "," +
                AccessoriesTable.Columns.ON_HAND + "," +
                AccessoriesTable.Columns.TOTAL_QUANTITY + "," +
                AccessoriesTable.Columns.BARCODE + ")";
    }

    private static ContentValues getContentValues(Accessory accessory){
        ContentValues contentValues = new ContentValues();

        contentValues.put(AccessoriesTable.Columns.ID, accessory.getId().toString());
        contentValues.put(AccessoriesTable.Columns.NAME,accessory.getName());
        contentValues.put(AccessoriesTable.Columns.TOTAL_QUANTITY,accessory.getTotalQuantity());
        contentValues.put(AccessoriesTable.Columns.ON_HAND, accessory.getOnHand());
        contentValues.put(AccessoriesTable.Columns.BARCODE, accessory.getBarcode());

        return contentValues;
    }

    private Accessories queryAccessories(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                AccessoriesTable.NAME,
                null, //Columns - Select all
                whereClause,
                whereArgs,
                null, //group by
                null, // having
                null // order by
        );

        return new Accessories(cursor);
    }
    @DynamoDBTable(tableName = "Accessory")
    public static class Accessory extends InventoryItems {
        private UUID id;
        private String name;
        private int totalQuantity;
        private int onHand;
        private String barcode;

        public Accessory() {
            super(InventoryItemType.ACCESSORY);
            this.id = UUID.randomUUID();
        }

        public Accessory(UUID id) {
            super(InventoryItemType.ACCESSORY);
            this.id = id;
        }

        @DynamoDBAttribute(attributeName = "Name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @DynamoDBAttribute(attributeName = "TotalQuantity")
        public int getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        @DynamoDBAttribute(attributeName = "OnHand")
        public int getOnHand() {
            return onHand;
        }

        public void setOnHand(int onHand) {
            this.onHand = onHand;
        }

        @DynamoDBAttribute(attributeName = "Description")
        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
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

        public static void validateAccessory(Accessory accessory) throws Exception {
            try {
                if (accessory.getName() == null || accessory.getName().equals("")) {
                    throw new Exception("Accessory Name can't be null");
                }
                if (accessory.getTotalQuantity() == 0) {
                    throw new Exception("Accessory total quantity can't be null");
                }
                if (accessory.getBarcode() == null || accessory.getBarcode().equals("")) {
                    throw new Exception("Accessory barcode can't be null");
                }
                if (accessory.getOnHand() > accessory.getTotalQuantity()) {
                    throw new Exception("On hand can't be greater than total quantity");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw e;
            }

        }

        @Override
        public String toString() {
            return "Accesory{" +
                    "name='" + name + '\'' +
                    '}';
        }

    }
}
