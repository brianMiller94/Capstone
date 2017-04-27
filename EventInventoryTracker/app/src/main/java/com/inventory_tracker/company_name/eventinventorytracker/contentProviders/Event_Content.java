package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.DateConverter;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes.UuidConverter;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_Object;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Utility_Methods;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_Delete_async;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.BaseHelper;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.EventsTable.Columns;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.EventsTable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Accessories;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Bundles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Event_Content extends ContentHelper {

    private static Event_Content sEventsContent;
    private final ThreadLocal<Context> mcontext = new ThreadLocal<>();
    private SQLiteDatabase mDatabase;

    public Event_Content(Context context){
        mcontext.set(context.getApplicationContext());
        mDatabase = new BaseHelper(mcontext.get()).getWritableDatabase();
    }

    public static Event_Content get(Context context){
        if(sEventsContent == null){
            sEventsContent = new Event_Content(context);
        }
        return sEventsContent;
    }


    public void add(Event event) {
        ContentValues contentValues = getContentValues(event);

        mDatabase.insertOrThrow(
                EventsTable.NAME,
                null,
                contentValues
        );
        addInventory(event);
    }
    public void update(Event event) {
        String uuidString = event.getId().toString();
        ContentValues contentValues = getContentValues(event);

        mDatabase.update(
                EventsTable.NAME,
                contentValues,
                columnWhereClauseFormat(Columns.ID),
                whereArgsFormatter(uuidString)
        );
        addInventory(event);
    }
    public void delete(Event event) {
        String uuidString = event.getId().toString();

        if (Utility_Methods.isNetworkAvailable(mcontext.get())) {

            Events_Have_Accessories_Content.get(mcontext.get()).deleteEvent(event);
            Events_Have_Bundles_Content.get(mcontext.get()).deleteEvent(event);
            Events_Have_Items_Content.get(mcontext.get()).deleteEvent(event);

            new Amazon_Sync_Delete_async().execute(new Combo_Context_Object(event,mcontext.get()));

            mDatabase.delete(
                    EventsTable.NAME,
                    columnWhereClauseFormat(Columns.ID),
                    whereArgsFormatter(uuidString)
            );

            Utility_Methods.createToast("Event Deleted",mcontext.get());
        } else {
            Utility_Methods.createToast("Sorry must be connected to a network",mcontext.get());
        }
    }

    private void addInventory(Event event){
        //Add accessories
        Events_Have_Accessories_Content.get(mcontext.get()).deleteEvent(event);
        for (Accessory_Content.Accessory accessory: event.getAccessories()) {
            Events_Have_Accessories_Content.get(mcontext.get()).wireAccessoryToEvent(accessory,event);
        }

        //add Item bundles
        Events_Have_Bundles_Content.get(mcontext.get()).deleteEvent(event);
        for (Bundle_Content.ItemBundle itemBundle: event.getItemBundles()) {
            Events_Have_Bundles_Content.get(mcontext.get()).wireItemBundleToEvent(itemBundle,event);
        }

        //add items
        Events_Have_Items_Content.get(mcontext.get()).deleteEvent(event);
        for (Item_Content.Item item:event.getItems()) {
            Events_Have_Items_Content.get(mcontext.get()).wireItemAndEvent(item,event);
        }
    }

    public Event getEventById(String id){
        Event event = null;
        try(
                Events cursor = queryEvents(
                        columnWhereClauseFormat(Columns.ID),
                        whereArgsFormatter(id))
        ){
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            event = cursor.getEvent();
            event.setItems(new ArrayList<>(Events_Have_Items_Content.get(mcontext.get()).getItemsForEvent(event)));
            event.setAccessories(new ArrayList<>(Events_Have_Accessories_Content.get(mcontext.get()).getAccessoriesForEvent(event)));
            event.setItemBundles(new ArrayList<>(Events_Have_Bundles_Content.get(mcontext.get()).getItemBundlesForEvent(event)));
        }catch(Exception e){
            e.printStackTrace();
            int blah;
        }
        return event;
    }

    private void mapEvent(Event event){

        Events_Have_Accessories_Content.get(mcontext.get())
            .getAccessoriesForEvent(event);
    }

    public List<Event> getEvents(){
        List<Event> events = new ArrayList<>();

        Events cursor = queryEvents(null,null);

        populateList(events,cursor);

        return events;
    }

    private void populateList(List<Event> listOfEvents, Events cursor){
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                listOfEvents.add(cursor.getEvent());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
    }

    public static String createTable() {
        return "create table " + EventsTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                Columns.ADDRESS + "," +
                Columns.CITY + "," +
                Columns.DATE_END + "," +
                Columns.DATE_START + "," +
                Columns.DESCRIPTION + "," +
                Columns.ID + "," +
                Columns.NAME + "," +
                Columns.STATE + "," +
                Columns.ZIP + ")";
    }

    private static ContentValues getContentValues(Event event){
        ContentValues values = new ContentValues();

        values.put(Columns.ADDRESS, event.getAddress());
        values.put(Columns.CITY, event.getCity());
        values.put(Columns.DATE_END, event.getDateEnd().toString());
        values.put(Columns.DATE_START, event.getDateStart().toString());
        values.put(Columns.DESCRIPTION, event.getDescription());
        values.put(Columns.ID, event.getId().toString());
        values.put(Columns.NAME, event.getName());
        values.put(Columns.STATE, event.getState());
        values.put(Columns.ZIP, event.getZip());

        return values;
    }

    private Events queryEvents(String whereClause, String [] whereArgs){
        Cursor cursor = mDatabase.query(
                DB_Schema.EventsTable.NAME,
                null, //Columns -- select all
                whereClause,
                whereArgs,
                null, // group by
                null, // having
                null //ordered by
        );

        return new Events(cursor);
    }

    @DynamoDBTable(tableName = "Events")
    public static class Event{
        private UUID id;
        private String name;
        private Date dateStart;
        private Date dateEnd;
        private String description;
        private String address;
        private String city;
        private String State;
        private String Zip;

        private ArrayList<Item_Content.Item> items;
        private ArrayList<Accessory_Content.Accessory> accessories;
        private ArrayList<Bundle_Content.ItemBundle> itemBundles;

        public Event(){
            this.id = UUID.randomUUID();
            itemBundles = new ArrayList<>();
            accessories = new ArrayList<>();
            items = new ArrayList<>();
            blankAddress();
        }

        public Event(UUID id){
            this.id = id;
            itemBundles = new ArrayList<>();
            accessories = new ArrayList<>();
            items = new ArrayList<>();
            blankAddress();
        }

        /**
         * While this feature would be nice I don't have the time to create it.
         */
        private void blankAddress(){
            this.address = "";
            this.city = "";
            this.State = "";
            this.Zip = "";
        }
        @DynamoDBHashKey(attributeName = "ID")
        @DynamoDBMarshalling(marshallerClass = UuidConverter.class)
        public UUID getId() {
            return id;
        }

        @DynamoDBAttribute(attributeName = "Name")
        public String getName() {
            return name;
        }

        @DynamoDBAttribute(attributeName = "dateStart")
        @DynamoDBMarshalling(marshallerClass = DateConverter.class)
        public Date getDateStart() {
            return dateStart;
        }

        @DynamoDBAttribute(attributeName = "dateEnd")
        @DynamoDBMarshalling(marshallerClass = DateConverter.class)
        public Date getDateEnd() {
            return dateEnd;
        }

        @DynamoDBAttribute(attributeName = "description")
        public String getDescription() {
            return description;
        }

        @DynamoDBAttribute(attributeName = "address")
        public String getAddress() {
            return address;
        }

        @DynamoDBAttribute(attributeName = "city")
        public String getCity() {
            return city;
        }

        @DynamoDBAttribute(attributeName = "state")
        public String getState() {
            return State;
        }

        @DynamoDBAttribute(attributeName = "zip")
        public String getZip() {
            return Zip;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDateStart(Date dateStart) {
            this.dateStart = dateStart;
        }

        public void setDateEnd(Date dateEnd) {
            this.dateEnd = dateEnd;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setState(String state) {
            State = state;
        }

        public void setZip(String zip) {
            Zip = zip;
        }
        @DynamoDBIgnore
        public ArrayList<Item_Content.Item> getItems() {
            return items;
        }
        @DynamoDBIgnore
        public void setItems(ArrayList<Item_Content.Item> items) {
            this.items = items;
        }

        public void addItem(Item_Content.Item item){
            this.items.add(item);
        }

        public void removeItem(Item_Content.Item item){
            this.items.remove(item);
        }
        @DynamoDBIgnore
        public ArrayList<Accessory_Content.Accessory> getAccessories() {
            return accessories;
        }
        @DynamoDBIgnore
        public void setAccessories(ArrayList<Accessory_Content.Accessory> accessories) {
            this.accessories = accessories;
        }
        @DynamoDBIgnore
        public void addAccessory(Accessory_Content.Accessory accessory ,int quantity){
            Accessory_Content.Accessory accessory1 = accessory;
            accessory1.setOnHand(quantity);
            accessory1.setTotalQuantity(quantity);
            this.accessories.add(accessory1);
        }
        @DynamoDBIgnore
        public void removeAccessory(Accessory_Content.Accessory accessory){
            this.accessories.remove(accessory);
        }
        @DynamoDBIgnore
        public ArrayList<Bundle_Content.ItemBundle> getItemBundles() {
            return itemBundles;
        }
        @DynamoDBIgnore
        public void setItemBundles(ArrayList<Bundle_Content.ItemBundle> itemBundles) {
            this.itemBundles = itemBundles;
        }

        public void addItemBundle(Bundle_Content.ItemBundle itemBundle){
            this.itemBundles.add(itemBundle);
        }

        public void removeItemBundle(Bundle_Content.ItemBundle itemBundle){
            this.itemBundles.remove(itemBundle);
        }
        public void dropAll(){
            this.itemBundles = new ArrayList<>();
            this.accessories = new ArrayList<>();
            this.items = new ArrayList<>();
        }

        public static void validateEvent(Event event) throws Exception{

            try {
                if(event.getName() == null || event.getName().equals("")){
                    throw new Exception("Event name can't be null");
                }
                if(event.getDescription() == null || event.getDescription().equals("")){
                    throw new Exception("Event description can't be null");
                }
            }catch (Exception e){
                e.printStackTrace();
                throw e;
            }
        }
    }
}
