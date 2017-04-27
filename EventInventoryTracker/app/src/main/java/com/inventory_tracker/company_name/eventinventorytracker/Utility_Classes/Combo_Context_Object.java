package com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes;

import android.content.Context;

/**
 * Created by brian on 4/19/2017.
 */

public class Combo_Context_Object {
    Object object;
    Context context;

    public Combo_Context_Object(Object object, Context context) {
        this.object = object;
        this.context = context;
    }

    public Object getObject() {
        return object;
    }

    public Context getContext() {
        return context;
    }
}
