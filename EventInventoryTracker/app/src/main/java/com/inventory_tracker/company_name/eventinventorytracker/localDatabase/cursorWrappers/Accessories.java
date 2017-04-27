package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Accessory_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema.AccessoriesTable.Columns;

import java.util.UUID;

/**
 * Created by brian on 1/16/2017.
 */

public class Accessories extends CursorWrapper {
    public Accessories(Cursor cursor) {
        super(cursor);
    }

    public Accessory_Content.Accessory getAccessory(){

        UUID idUUID = UUID.fromString(getString(getColumnIndex(Columns.ID)));
        String nameString = getString(getColumnIndex(Columns.NAME));
        int totalQuantityInt = getInt(getColumnIndex(Columns.TOTAL_QUANTITY));
        int onHandInt = getInt(getColumnIndex(Columns.ON_HAND));
        String barcodeString = getString(getColumnIndex(Columns.BARCODE));

        Accessory_Content.Accessory accessory = new Accessory_Content.Accessory(idUUID);
        accessory.setName(nameString);
        accessory.setTotalQuantity(totalQuantityInt);
        accessory.setOnHand(onHandInt);
        accessory.setBarcode(barcodeString);

        return accessory;
    }
}
