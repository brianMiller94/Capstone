package com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Marshaller_Classes;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.util.UUID;

/**
 * Created by brian.miller on 4/18/2017.
 */

public class UuidConverter implements DynamoDBMarshaller<UUID> {

    @Override
    public String marshall(UUID getterReturnResult) {
        return getterReturnResult.toString();
    }

    @Override
    public UUID unmarshall(Class<UUID> clazz, String obj) {
        String test = obj;
        UUID testUUID = UUID.fromString(test);
        return UUID.fromString(obj);
    }
}
