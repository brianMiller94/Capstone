package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.EventsTable.Columns;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Events extends CursorWrapper {
    public Events(Cursor cursor) {
        super(cursor);
    }

    public Event_Content.Event getEvent(){
        UUID idUUID = UUID.fromString(getString(getColumnIndex(Columns.ID)));

        Event_Content.Event event = new Event_Content.Event(idUUID);
        event.setName(getString(getColumnIndex(Columns.NAME)));
        event.setDescription(getString(getColumnIndex(Columns.DESCRIPTION)));
        event.setAddress(getString(getColumnIndex(Columns.ADDRESS)));
        event.setCity(getString(getColumnIndex(Columns.CITY)));
        String startDate = getString(getColumnIndex(Columns.DATE_START));
        String endDate = getString(getColumnIndex(Columns.DATE_END));
        DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        try{
            event.setDateStart(formatter.parse(startDate));
            event.setDateEnd(formatter.parse(endDate));
        }catch (Exception e){
            e.printStackTrace();
        }
        event.setState(getString(getColumnIndex(Columns.STATE)));
        event.setZip(getString(getColumnIndex(Columns.ZIP)));

        return event;
    }
}
