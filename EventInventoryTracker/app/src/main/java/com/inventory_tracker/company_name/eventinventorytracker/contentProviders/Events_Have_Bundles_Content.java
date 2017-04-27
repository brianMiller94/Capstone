package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Events_Have_Bundles_Table.Columns;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Accessories;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Bundles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Events_Have_Bundles_Content extends ContentHelper {

    private final static String TABLE_NAME = DB_Schema.Events_Have_Bundles_Table.NAME;

    private static Events_Have_Bundles_Content sEvents_Have_BundlesContent;
    private final ThreadLocal<Context> mContext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;

    public Events_Have_Bundles_Content(Context context){
        mContext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mContext.get()).getWritableDatabase();
    }

    public static Events_Have_Bundles_Content get(Context context){
        if(sEvents_Have_BundlesContent == null){
            sEvents_Have_BundlesContent = new Events_Have_Bundles_Content(context);
        }
        return sEvents_Have_BundlesContent;
    }

    public void add(Event_Has_Bundle event_has_bundle) {
        ContentValues values = getContentValues(event_has_bundle);
        mDatabase.insertOrThrow(
                TABLE_NAME,
                null,
                values
        );
    }
    public void update(Event_Has_Bundle event_has_bundle) {
        String uuidOfBundle = event_has_bundle.getIdOfBundle().toString();
        String uuidOfEvent = event_has_bundle.getIdOfEvent().toString();

        ContentValues values = getContentValues(event_has_bundle);

        mDatabase.update(
                TABLE_NAME,
                values,
                twoColumnWhereClauseFormat(Columns.BUNDLE_ID,Columns.EVENT_ID),
                whereArgsFormatter(uuidOfBundle,uuidOfEvent)
        );
    }

    void deleteEvent(Event_Content.Event event){
        String uuidStringEvent = event.getId().toString();
        ArrayList<Event_Has_Bundle> mappings = getMappingsForEvent(event);
        mDatabase.delete(
                TABLE_NAME,
                columnWhereClauseFormat(Columns.EVENT_ID),
                whereArgsFormatter(uuidStringEvent)
        );
        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(mappings.get(i),mContext.get()));
        }
    }

    void deleteBundle(Bundle_Content.ItemBundle bundle) {
        String uuidStringBundle = bundle.getId().toString();
        ArrayList<Event_Has_Bundle> mappings = getMappingsForBundles(bundle);
        mDatabase.delete(
                TABLE_NAME,
                columnWhereClauseFormat(Columns.BUNDLE_ID),
                whereArgsFormatter(uuidStringBundle)
        );
        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(mappings.get(i),mContext.get()));
        }
    }

    private ArrayList<Event_Has_Bundle> getMappingsForBundles(Bundle_Content.ItemBundle itemBundle){
        ArrayList<Event_Has_Bundle> mappings = new ArrayList<>();
        try{
            Events_Have_Bundles cursor = queryEvents_Have_Bundles(
                    columnWhereClauseFormat(Columns.BUNDLE_ID),
                    whereArgsFormatter(itemBundle.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                mappings.add(cursor.getEvent_Has_Bundle());
                cursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return mappings;
    }

    private ArrayList<Event_Has_Bundle> getMappingsForEvent(Event_Content.Event event){
        ArrayList<Event_Has_Bundle> mappings = new ArrayList<>();
        try{
            Events_Have_Bundles cursor = queryEvents_Have_Bundles(
                    columnWhereClauseFormat(Columns.EVENT_ID),
                    whereArgsFormatter(event.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                mappings.add(cursor.getEvent_Has_Bundle());
                cursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return mappings;
    }

    List<Bundle_Content.ItemBundle> getItemBundlesForEvent(Event_Content.Event event) {
        ArrayList<Bundle_Content.ItemBundle> itemBundles = new ArrayList<>();
        String uuidString = event.getId().toString();
        try {
            Events_Have_Bundles cursor = queryEvents_Have_Bundles(
                    columnWhereClauseFormat(Columns.EVENT_ID),
                    whereArgsFormatter(uuidString));
            if (cursor.getCount() == 0) {
                return itemBundles;
            } else {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Event_Has_Bundle event_has_accessory = cursor.getEvent_Has_Bundle();
                    Bundle_Content.ItemBundle itemBundle =
                            Bundle_Content.get(mContext.get()).
                                    getBundleById(event_has_accessory.getIdOfBundle().toString());
                    itemBundles.add(itemBundle);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemBundles;
    }

    public Event_Has_Bundle getSpecificEntry(UUID EventUUID, UUID BundleUUID){
        try(Events_Have_Bundles cursor = queryEvents_Have_Bundles(
                twoColumnWhereClauseFormat(Columns.EVENT_ID,Columns.BUNDLE_ID),
                whereArgsFormatter(EventUUID.toString(),BundleUUID.toString())
        )){
            cursor.moveToFirst();
            return cursor.getEvent_Has_Bundle();
        }catch (Exception e){
            return null;
        }
    }

    public List<Event_Has_Bundle> getAllEntries(){
        List<Event_Has_Bundle> list = new ArrayList<>();
        try(Events_Have_Bundles cursor = queryEvents_Have_Bundles(null,null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                list.add(cursor.getEvent_Has_Bundle());
                cursor.moveToNext();
            }
        }
        return list;
    }

    void wireItemBundleToEvent(Bundle_Content.ItemBundle itemBundle, Event_Content.Event event){
        Event_Has_Bundle event_has_bundle = new Event_Has_Bundle(itemBundle.getId(), event.getId());
        this.add(event_has_bundle);
    }
    public static String createTable() {
        return "create table " + TABLE_NAME + "(" +
                Columns.BUNDLE_ID + "," +
                Columns.EVENT_ID + ")";
    }

    private static ContentValues getContentValues(Event_Has_Bundle event_has_bundle){
        ContentValues values = new ContentValues();

        values.put(Columns.BUNDLE_ID , event_has_bundle.getIdOfBundle().toString());
        values.put(Columns.EVENT_ID, event_has_bundle.getIdOfEvent().toString());

        return values;
    }

    private Events_Have_Bundles queryEvents_Have_Bundles(String whereClause, String [] whereArgs){
        Cursor cursor = mDatabase.query(
                TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new Events_Have_Bundles(cursor);
    }
    @DynamoDBTable(tableName = "Events_Have_Bundles")
    public static class Event_Has_Bundle{
        UUID idOfBundle;
        UUID idOfEvent;

        public Event_Has_Bundle(){

        }

        public Event_Has_Bundle(UUID idOfBundle, UUID idOfEvent){
            this.idOfBundle = idOfBundle;
            this.idOfEvent = idOfEvent;
        }
        @DynamoDBHashKey(attributeName = "bundleId")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getIdOfBundle() {
            return idOfBundle;
        }

        @DynamoDBHashKey(attributeName = "eventId")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getIdOfEvent() {
            return idOfEvent;
        }

        public void setIdOfBundle(UUID idOfBundle) {
            this.idOfBundle = idOfBundle;
        }

        public void setIdOfEvent(UUID idOfEvent) {
            this.idOfEvent = idOfEvent;
        }
    }
}
