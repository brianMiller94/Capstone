package com.inventory_tracker.company_name.eventinventorytracker.localDatabase.cursorWrappers;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.UUID;

import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Item_Content;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.DB_Schema;

/**
 * Created by brian on 1/14/2017.
 */

public class Items extends CursorWrapper {

    public Items(Cursor cursor){
        super(cursor);
    }

    public Item_Content.Item getItem(){


        UUID idUUID = UUID.fromString(getString(getColumnIndex(DB_Schema.ItemsTable.Columns.ID)));
        String bundleIdUUID = getString(getColumnIndex(DB_Schema.ItemsTable.Columns.BUNDLE_ID));
        UUID bundleUUID;
        if(bundleIdUUID.equals("")){
            bundleUUID = null;
        }else{
            bundleUUID = UUID.fromString(bundleIdUUID);
        }
        String nameString = getString(getColumnIndex(DB_Schema.ItemsTable.Columns.NAME));
        String descriptionString = getString(getColumnIndex(DB_Schema.ItemsTable.Columns.DESCRIPTION));
        String barcodeString = getString(getColumnIndex(DB_Schema.ItemsTable.Columns.BARCODE));
        String serialNumberString = getString(getColumnIndex(DB_Schema.ItemsTable.Columns.SERIAL_NUMBER));



        Item_Content.Item newItem;
        if (bundleUUID != null){
            newItem = new Item_Content.Item(idUUID,bundleUUID);
        }else{
            newItem = new Item_Content.Item(idUUID);
        }

        newItem.setName(nameString);
        newItem.setDescription(descriptionString);
        newItem.setBarcode(barcodeString);
        newItem.setSerialNumber(serialNumberString);

        return newItem;
    }
}
