package com.inventory_tracker.company_name.eventinventorytracker.contentProviders;

/**
 * Created by brian on 1/21/2017.
 */

abstract class ContentHelper {

    static String columnWhereClauseFormat(String columnName){
        return columnName + " = ?";
    }

    static String columnLikeWhereClauseFormat(String columnName){
        return columnName + " LIKE ?";
    }

    static String twoColumnWhereClauseFormat(String columnNameOne, String columnNameTwo){
        return columnNameOne + " = ? AND " + columnNameTwo + " = ?";
    }

    static String[] whereArgsFormatter(int integer){
        return new String[] {Integer.toString(integer)};
    }

    static String[] whereArgsFormatter(boolean bool){
        return new String[] {Boolean.toString(bool)};
    }

    static String[] whereArgsFormatter(String string){
        return new String[] {string};
    }

    static String[] whereArgsFormatter(String idOne, String idTwo){
        return new String[] {idOne, idTwo};
    }

    static String[] whereLikeArgsFormatter(String string){
        return new String[]{"%" + string + "%"};
    }




}
