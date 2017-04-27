package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Events_Have_Accessories_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Events_Have_Accessories_Table.Columns;

import java.util.UUID;

/**
 * Created by brian on 3/4/2017.
 */

public class Events_Have_Accessories extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public Events_Have_Accessories(Cursor cursor) {
        super(cursor);
    }

    public Events_Have_Accessories_Content.Event_Has_Accessory getEvent_Has_Accessory(){

        UUID idOfEvent = UUID.fromString(getString(getColumnIndex(Columns.EVENT_ID)));
        UUID idOfAccessory = UUID.fromString(getString(getColumnIndex(Columns.ACCESSORY_ID)));
        int quantity = getInt(getColumnIndex(Columns.QUANTITY));

        return new Events_Have_Accessories_Content.Event_Has_Accessory(idOfAccessory,idOfEvent,quantity);
    }

}
