package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Items_Content;

import java.util.UUID;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Events_Have_Items_Table.Columns;

/**
 * Created by brian on 3/4/2017.
 */

public class Events_Have_Items extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public Events_Have_Items(Cursor cursor) {
        super(cursor);
    }

    public Events_Have_Items_Content.Event_Has_Item getEvent_Has_Item(){
        UUID idOfEvent = UUID.fromString(getString(getColumnIndex(Columns.EVENT_ID)));
        UUID idOfItem = UUID.fromString(getString(getColumnIndex(Columns.ITEM_ID)));

        return new Events_Have_Items_Content.Event_Has_Item(idOfEvent,idOfItem);
    }
}
