package com.inventory_tracker.company_name.eventinventorytracker.localDatabase;

/**
 * Created by brian on 1/14/2017.
 */

public class DB_Schema {

    public static final class AccessoriesTable{
        public static final String NAME = "Accessories";

        public static final class Columns{
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String TOTAL_QUANTITY = "totalQuantity";
            public static final String ON_HAND = "onHand";
            public static final String BARCODE = "barcode";
        }
    }

    public static final class BundlesTable {
        public static final String NAME = "Bundles";

        public static class Columns{
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String DESCRIPTION = "description";
            public static final String BARCODE = "barcode";

        }
    }

    public static final class Bundles_Have_Accessories_table{
        public static final String NAME = "Bundles_have_Accessories";

        public static final class Columns{
            public static final String BUNDLE_ID = "Bundle_id";
            public static final String ACCESSORIES_ID = "Accessory_id";
            public static final String QUANTITY = "quantity";
        }
    }

    public static final class EventsTable {
        public static final String NAME = "Events";

        public static final class Columns{
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String DATE_START = "dateStart";
            public static final String DATE_END = "dateEnd";
            public static final String DESCRIPTION = "description";
            public static final String ADDRESS = "address";
            public static final String CITY = "city";
            public static final String STATE = "state";
            public static final String ZIP = "zip";
        }
    }

    public static final class Events_Have_Accessories_Table {
        public static final String NAME = "Events_Have_Accessories";

        public static final class Columns{
            public static final String EVENT_ID = "Event_id";
            public static final String ACCESSORY_ID = "Accessory_id";
            public static final String QUANTITY = "quantity";
        }
    }

    public static final class Events_Have_Bundles_Table {
        public static final String NAME = "Events_Have_Bundles";

        public static final class Columns{
            public static final String EVENT_ID = "Event_id";
            public static final String BUNDLE_ID = "Bundle_id";
        }
    }

    public static final class Events_Have_Items_Table {
        public static final String NAME = "Events_Have_Items";

        public static final class Columns{
            public static final String EVENT_ID = "Event_id";
            public static final String ITEM_ID = "Item_id";
        }
    }

    public static final class ItemsTable{
        public static final String NAME = "Items";

        public static final class Columns{
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String DESCRIPTION = "description";
            public static final String BARCODE = "barcode";
            public static final String SERIAL_NUMBER = "serialNumber";
            public static final String BUNDLE_ID = "bundle_id";
        }
    }

}
