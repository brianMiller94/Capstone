package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Events_Have_Items_Table.Columns;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brian on 3/5/2017.
 */

public class Events_Have_Items_Content extends ContentHelper {

    private static final String TABLE_NAME = DB_Schema.Events_Have_Items_Table.NAME;

    private static Events_Have_Items_Content sEvents_Have_Items_Content;
    private final ThreadLocal<Context> mContext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;

    public Events_Have_Items_Content(Context context){
        mContext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mContext.get()).getWritableDatabase();
    }

    public static Events_Have_Items_Content get(Context context){
        if(sEvents_Have_Items_Content == null){
            sEvents_Have_Items_Content = new Events_Have_Items_Content(context);
        }
        return sEvents_Have_Items_Content;
    }

    public void add(Event_Has_Item event_has_item){
        ContentValues values = getContentValues(event_has_item);

        mDatabase.insertOrThrow(
                TABLE_NAME,
                null,
                values
        );
    }

    public void update(Event_Has_Item event_has_item){
        String uuidStringItem = event_has_item.getItemUUID().toString();
        String uuidStringEvent = event_has_item.getEventUUID().toString();

        ContentValues values = getContentValues(event_has_item);

        mDatabase.update(
                TABLE_NAME,
                values,
                twoColumnWhereClauseFormat(Columns.EVENT_ID ,Columns.ITEM_ID),
                whereArgsFormatter(uuidStringEvent, uuidStringItem)
        );
    }

    void deleteItem(Item_Content.Item item){
        String uuidStringItem = item.getId().toString();
        ArrayList<Event_Has_Item> mappings = getMappingsForItems(item);
        mDatabase.delete(
                TABLE_NAME,
                columnWhereClauseFormat(Columns.ITEM_ID),
                whereArgsFormatter(uuidStringItem)
        );
        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(mappings.get(i),mContext.get()));

        }
    }
    void deleteEvent(Event_Content.Event event){
        String uuidStringEvent = event.getId().toString();
        ArrayList<Event_Has_Item> mappings = getMappingsForEvent(event);
        mDatabase.delete(
                TABLE_NAME,
                columnWhereClauseFormat(Columns.EVENT_ID),
                whereArgsFormatter(uuidStringEvent)
        );
        for (int i = 0; i < mappings.size(); i++) {
            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(mappings.get(i),mContext.get()));
        }
    }

    private ArrayList<Event_Has_Item> getMappingsForItems(Item_Content.Item item){
        ArrayList<Event_Has_Item> mappings = new ArrayList<>();
        try{
            Events_Have_Items cursor = queryEvents_Have_Items(
                    columnWhereClauseFormat(Columns.ITEM_ID),
                    whereArgsFormatter(item.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                mappings.add(cursor.getEvent_Has_Item());
                cursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return mappings;
    }

    private ArrayList<Event_Has_Item> getMappingsForEvent(Event_Content.Event event){
        ArrayList<Event_Has_Item> mappings = new ArrayList<>();
        try{
            Events_Have_Items cursor = queryEvents_Have_Items(
                    columnWhereClauseFormat(Columns.EVENT_ID),
                    whereArgsFormatter(event.getId().toString())
            );
            if(cursor.getCount() == 0){
                return mappings;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                mappings.add(cursor.getEvent_Has_Item());
                cursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return mappings;
    }

    /**
     * Maps Items to an event, items are only returned if they are not part of a bundle.
     * @param event event you want to fetch items for
     * @return List of items for event
     * @author Brian Miller
     */
    List<Item_Content.Item> getItemsForEvent(Event_Content.Event event) {
        ArrayList<Item_Content.Item> items = new ArrayList<>();
        String uuidString = event.getId().toString();
        try {
            Events_Have_Items cursor = queryEvents_Have_Items(
                    columnWhereClauseFormat(Columns.EVENT_ID),
                    whereArgsFormatter(uuidString));
            if (cursor.getCount() == 0) {
                return items;
            } else {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Event_Has_Item event_has_item = cursor.getEvent_Has_Item();
                    Item_Content.Item itemBundle =
                            Item_Content.get(mContext.get()).
                                    getInventoryItemById(event_has_item.getItemUUID().toString());
                    items.add(itemBundle);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public Event_Has_Item getSpecificEntry(UUID eventUUID, UUID itemUUID){
        try(Events_Have_Items cursor = queryEvents_Have_Items(
                twoColumnWhereClauseFormat(Columns.EVENT_ID, Columns.ITEM_ID),
                whereArgsFormatter(eventUUID.toString(),itemUUID.toString())
        )){
            cursor.moveToFirst();
            return cursor.getEvent_Has_Item();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Event_Has_Item> getAllEntries(){
        List<Event_Has_Item> list = new ArrayList<>();
        try(Events_Have_Items cursor = queryEvents_Have_Items(null,null)){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                list.add(cursor.getEvent_Has_Item());
                cursor.moveToNext();
            }
        }
        return list;
    }

    void wireItemAndEvent(Item_Content.Item item, Event_Content.Event event){
        Event_Has_Item event_has_item = new Event_Has_Item(event.getId(), item.getId());
        this.add(event_has_item);
    }

    public static String createTable(){
        return "create table " + TABLE_NAME + "(" +
                Columns.EVENT_ID + "," +
                Columns.ITEM_ID + ")";
    }

    private static ContentValues getContentValues(Event_Has_Item event_has_item){
        ContentValues values = new ContentValues();

        values.put(Columns.EVENT_ID, event_has_item.getEventUUID().toString());
        values.put(Columns.ITEM_ID, event_has_item.getItemUUID().toString());

        return values;
    }

    private Events_Have_Items queryEvents_Have_Items(String whereclause, String [] whereArgs){
        Cursor cursor = mDatabase.query(
                TABLE_NAME,
                null,
                whereclause,
                whereArgs,
                null,
                null,
                null
        );
        return new Events_Have_Items(cursor);
    }

    @DynamoDBTable(tableName = "Events_Have_Items")
    public static class Event_Has_Item{
        UUID eventUUID;
        UUID itemUUID;

        public Event_Has_Item(){

        }

        public Event_Has_Item(UUID eventUUID, UUID itemUUID) {
            this.eventUUID = eventUUID;
            this.itemUUID = itemUUID;
        }

        @DynamoDBHashKey(attributeName = "Event_id")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getEventUUID() {
            return eventUUID;
        }

        @DynamoDBHashKey(attributeName = "Item_id")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getItemUUID() {
            return itemUUID;
        }

        public void setEventUUID(UUID eventUUID) {
            this.eventUUID = eventUUID;
        }

        public void setItemUUID(UUID itemUUID) {
            this.itemUUID = itemUUID;
        }
    }
}
