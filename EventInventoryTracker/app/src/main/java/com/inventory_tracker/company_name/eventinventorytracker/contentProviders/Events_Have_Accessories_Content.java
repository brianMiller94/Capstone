package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Accessories;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Events_Have_Accessories_Table.Columns;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brian on 3/5/2017.
 */

public class Events_Have_Accessories_Content extends ContentHelper {

    private static Events_Have_Accessories_Content sEvents_Have_AccessoriesContent;
    private final ThreadLocal<Context> mContext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;
    private final static String TABLE_NAME = DB_Schema.Events_Have_Accessories_Table.NAME;

    public Events_Have_Accessories_Content(Context context) {
        mContext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mContext.get()).getWritableDatabase();
    }

    public static Events_Have_Accessories_Content get(Context context) {
        if (sEvents_Have_AccessoriesContent == null) {
            sEvents_Have_AccessoriesContent = new Events_Have_Accessories_Content(context);
        }
        return sEvents_Have_AccessoriesContent;
    }

    public void add(Event_Has_Accessory event_has_accessory) {
        ContentValues values = getContentValues(event_has_accessory);

        mDatabase.insertOrThrow(
                TABLE_NAME,
                null,
                values
        );
    }

    public void update(Event_Has_Accessory event_has_accessory) {
        String uuidStringOfAccessory = event_has_accessory.getIdOfAccessory().toString();
        String uuidStringOfEvent = event_has_accessory.getIdOfEvent().toString();

        ContentValues values = getContentValues(event_has_accessory);

        mDatabase.update(
                TABLE_NAME,
                values,
                twoColumnWhereClauseFormat(Columns.ACCESSORY_ID, Columns.EVENT_ID),
                whereArgsFormatter(uuidStringOfAccessory, uuidStringOfEvent)
        );
    }

    void deleteAccessory(Accessory_Content.Accessory accessory) {
        String uuidStringAccessory = accessory.getId().toString();
        ArrayList<Event_Has_Accessory> mappings = getMappingsForAccessories(accessory);
        mDatabase.delete(
                TABLE_NAME,
                columnWhereClauseFormat(Columns.ACCESSORY_ID),
                whereArgsFormatter(uuidStringAccessory)
        );
        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(mappings.get(i),mContext.get()));
        }
    }

    void deleteEvent(Event_Content.Event event) {
        String uuidStringEvent = event.getId().toString();

        //clean up Accessories inventory count
        ArrayList<Accessory_Content.Accessory> accessories =new ArrayList<>(getAccessoriesForEvent(event));
        for (Accessory_Content.Accessory accessory:
                accessories) {
            Accessory_Content.Accessory parentAccessory =
                    Accessory_Content.get(mContext.get()).getAccessoryById(accessory.getId().toString());
            parentAccessory.setOnHand(parentAccessory.getOnHand() + accessory.getTotalQuantity());
            Accessory_Content.get(mContext.get()).update(parentAccessory);
        }
        ArrayList<Event_Has_Accessory> mappings = getMappingsForEvent(event);
        mDatabase.delete(
                DB_Schema.Events_Have_Accessories_Table.NAME,
                columnWhereClauseFormat(Columns.EVENT_ID),
                whereArgsFormatter(uuidStringEvent)
        );
        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(event,mContext.get()));
        }
    }

    private ArrayList<Event_Has_Accessory> getMappingsForAccessories(Accessory_Content.Accessory accessory){
        ArrayList<Event_Has_Accessory> mappings = new ArrayList<>();
        try{
            Events_Have_Accessories cursor = queryEvents_Have_Accessories(
                    columnWhereClauseFormat(Columns.ACCESSORY_ID),
                    whereArgsFormatter(accessory.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                mappings.add(cursor.getEvent_Has_Accessory());
                cursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return mappings;
    }

    private ArrayList<Event_Has_Accessory> getMappingsForEvent(Event_Content.Event event){
        ArrayList<Event_Has_Accessory> mappings = new ArrayList<>();
        try{
            Events_Have_Accessories cursor = queryEvents_Have_Accessories(
                    columnWhereClauseFormat(Columns.EVENT_ID),
                    whereArgsFormatter(event.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                mappings.add(cursor.getEvent_Has_Accessory());
                cursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return mappings;
    }

    /**
     * This will return all accessories tied to event. This will not return accessories tied to
     * bundles as those will be returned with the bundles rather than trying to map them post
     * retrieval.
     *
     * @param event event to get accessories for.
     * @return List of Accessory objects
     * @author Brian Miller
     */
    List<Accessory_Content.Accessory> getAccessoriesForEvent(Event_Content.Event event) {
        ArrayList<Accessory_Content.Accessory> accessories = new ArrayList<>();
        String uuidString = event.getId().toString();
        try {
            Events_Have_Accessories cursor = queryEvents_Have_Accessories(
                    columnWhereClauseFormat(Columns.EVENT_ID),
                    whereArgsFormatter(uuidString));
            if (cursor.getCount() == 0) {
                return accessories;
            } else {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Event_Has_Accessory event_has_accessory = cursor.getEvent_Has_Accessory();
                    Accessory_Content.Accessory accessory =
                            Accessory_Content.get(mContext.get()).
                                    getAccessoryById(event_has_accessory.getIdOfAccessory().toString());
                    accessory.setOnHand(event_has_accessory.getQuantity());
                    accessory.setTotalQuantity(event_has_accessory.getQuantity());
                    accessories.add(accessory);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessories;
    }

    public Event_Has_Accessory getSpecificEntry(UUID EventUUID, UUID AccessoryUUID){
        try {
            Events_Have_Accessories cursor = queryEvents_Have_Accessories(
                    twoColumnWhereClauseFormat(Columns.EVENT_ID, Columns.ACCESSORY_ID),
                    whereArgsFormatter(EventUUID.toString(),AccessoryUUID.toString())
            );
            if(cursor.getCount() == 0){
                return null;
            }else{
                cursor.moveToFirst();
                return cursor.getEvent_Has_Accessory();
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Event_Has_Accessory> getAllEntries(){
        List<Event_Has_Accessory> list = new ArrayList<>();
        try(Events_Have_Accessories cursor = queryEvents_Have_Accessories(null, null)){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                list.add(cursor.getEvent_Has_Accessory());
                cursor.moveToNext();
            }
        }
        return list;
    }

    void wireAccessoryToEvent(Accessory_Content.Accessory accessory, Event_Content.Event event){
        int accessoryQuantity = accessory.getOnHand();
        Event_Has_Accessory event_has_accessory = new Event_Has_Accessory(accessory.getId(),
                event.getId(),accessoryQuantity);
        this.add(event_has_accessory);

        Accessory_Content.Accessory updatedAccessory = Accessory_Content.get(mContext.get()).
                getAccessoryById(accessory.getId().toString());
        updatedAccessory.setOnHand(updatedAccessory.getOnHand() - accessoryQuantity);
        Accessory_Content.get(mContext.get()).update(updatedAccessory);
    }

    public static String createTable() {
        return "create Table " + TABLE_NAME + "(" +
                Columns.ACCESSORY_ID + "," +
                Columns.EVENT_ID + "," +
                Columns.QUANTITY + ")";
    }

    private static ContentValues getContentValues(Event_Has_Accessory event_has_accessory) {
        ContentValues values = new ContentValues();

        values.put(Columns.ACCESSORY_ID, event_has_accessory.getIdOfAccessory().toString());
        values.put(Columns.EVENT_ID, event_has_accessory.getIdOfEvent().toString());
        values.put(Columns.QUANTITY, event_has_accessory.getQuantity());

        return values;
    }

    private Events_Have_Accessories queryEvents_Have_Accessories(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new Events_Have_Accessories(cursor);
    }

    @DynamoDBTable(tableName = "Events_Have_Accessories")
    public static class Event_Has_Accessory {
        UUID idOfAccessory;
        UUID idOfEvent;
        int quantity;

        Event_Has_Accessory() {

        }

        public Event_Has_Accessory(UUID idOfAccessory, UUID idOfEvent, int quantity) {
            this.idOfAccessory = idOfAccessory;
            this.idOfEvent = idOfEvent;
            this.quantity = quantity;
        }

        @DynamoDBHashKey(attributeName = "Accessory_ID")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getIdOfAccessory() {
            return idOfAccessory;
        }

        @DynamoDBHashKey(attributeName = "Event_Id")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getIdOfEvent() {
            return idOfEvent;
        }

        public void setIdOfAccessory(UUID idOfAccessory) {
            this.idOfAccessory = idOfAccessory;
        }

        public void setIdOfEvent(UUID idOfEvent) {
            this.idOfEvent = idOfEvent;
        }

        @DynamoDBAttribute(attributeName = "quantity")
        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
