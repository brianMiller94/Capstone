package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Bundle_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.BundlesTable.Columns;

import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Bundles extends CursorWrapper {
    public Bundles(Cursor cursor) {
        super(cursor);
    }

    public Bundle_Content.ItemBundle getBundle(){

        Bundle_Content.ItemBundle bundle = new Bundle_Content.ItemBundle(UUID.fromString(getString(getColumnIndex(Columns.ID))));


        String nameString = getString(getColumnIndex(Columns.NAME));
        String descriptionString = getString(getColumnIndex(Columns.DESCRIPTION));
        String barcodeString = getString(getColumnIndex(Columns.BARCODE));


        bundle.setBarcode(barcodeString);
        bundle.setDescription(descriptionString);
        bundle.setName(nameString);

        return bundle;
    }
}
