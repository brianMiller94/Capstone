package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Bundles_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Events_Have_Bundles_Table.Columns;

import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Events_Have_Bundles extends CursorWrapper {
    public Events_Have_Bundles(Cursor cursor) {
        super(cursor);
    }

    public Events_Have_Bundles_Content.Event_Has_Bundle getEvent_Has_Bundle(){

        UUID idOfBundle = UUID.fromString(getString(getColumnIndex(Columns.BUNDLE_ID)));
        UUID idOfEvent = UUID.fromString(getString(getColumnIndex(Columns.EVENT_ID)));

        return new Events_Have_Bundles_Content.Event_Has_Bundle(idOfBundle,idOfEvent);
    }
}
