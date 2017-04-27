package com.inventory_tracker.company_name.eventinventorytracker.localDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundles_Have_Accessories_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Accessories_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Bundles_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Items_Content;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers.Events_Have_Items;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by brian on 1/14/2017.
 */

public class BaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "Inventory.db";

    public BaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try{
            sqLiteDatabase.execSQL(Accessory_Content.createTable());
            sqLiteDatabase.execSQL(Bundle_Content.createTable());
            sqLiteDatabase.execSQL(Bundles_Have_Accessories_Content.createTable());
            sqLiteDatabase.execSQL(Event_Content.createTable());
            sqLiteDatabase.execSQL(Events_Have_Accessories_Content.createTable());
            sqLiteDatabase.execSQL(Events_Have_Bundles_Content.createTable());
            sqLiteDatabase.execSQL(Events_Have_Items_Content.createTable());
            sqLiteDatabase.execSQL(Item_Content.createTable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
