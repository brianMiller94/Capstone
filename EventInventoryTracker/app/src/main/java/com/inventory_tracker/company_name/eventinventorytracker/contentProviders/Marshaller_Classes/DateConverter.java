package com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by brian.miller on 4/18/2017.
 */

public class DateConverter implements DynamoDBMarshaller<Date> {
    @Override
    public String marshall(Date getterReturnResult) {
        return getterReturnResult.toString();
    }

    @Override
    public Date unmarshall(Class<Date> clazz, String obj) {
        DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date date = new Date();
        try{
            date = formatter.parse(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
        return date;
    }
}
