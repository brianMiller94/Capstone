package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundles_Have_Accessories_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.Bundles_Have_Accessories_table.Columns;

import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Bundles_Have_Accessories extends CursorWrapper {
    public Bundles_Have_Accessories(Cursor cursor) {
        super(cursor);
    }

    public Bundles_Have_Accessories_Content.Bundle_Has_Accessory getBundle_have_accessories(){
        UUID idOfAccessory = UUID.fromString(getString(getColumnIndex(Columns.ACCESSORIES_ID)));
        UUID idOfBundle = UUID.fromString(getString(getColumnIndex(Columns.BUNDLE_ID)));
        int quantity = getInt(getColumnIndex(Columns.QUANTITY));

        return new Bundles_Have_Accessories_Content.Bundle_Has_Accessory(idOfBundle,idOfAccessory,quantity);

    }
}
