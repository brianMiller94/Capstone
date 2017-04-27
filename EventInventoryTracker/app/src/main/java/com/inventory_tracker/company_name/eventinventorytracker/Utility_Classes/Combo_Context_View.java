package com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes;

import android.content.Context;
import android.view.View;

/**
 * Created by brian on 4/18/2017.
 */

public class Combo_Context_View {
    Context context;
    View progressBar;

    public Combo_Context_View(Context context, View progressBar) {
        this.context = context;
        this.progressBar = progressBar;
    }

    public Context getContext() {
        return context;
    }

    public View getProgressBar() {
        return progressBar;
    }
}
