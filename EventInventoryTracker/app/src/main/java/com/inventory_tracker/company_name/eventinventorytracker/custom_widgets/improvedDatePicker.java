package com.inventory_tracker.company_name.eventinventorytracker.custom_widgets;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by brian on 4/4/2017.
 */

public class improvedDatePicker extends android.widget.DatePicker {
    public improvedDatePicker(Context context) {
        super(context);
    }

    public improvedDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public improvedDatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public improvedDatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    @Override
    public void updateDate(int year, int month, int day){
        super.updateDate(year,month,day);
    }
    public void updateDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        updateDate(year,month,day);
    }
    public Date getDate(){
        int year = getYear();
        int month = getMonth();
        int day = getDayOfMonth();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        return calendar.getTime();
    }
}
