package org.polymap.core.data.refine.impl;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class FormatAndOptions {

    private final JSONObject store;


    public static FormatAndOptions createDefault() {
        return new FormatAndOptions( new JSONObject() );
    }

    public FormatAndOptions( JSONObject jsonObject ) {
        store = jsonObject;
    }


    protected void put( String key, Object value ) {
        try {
            store.put( key, value );
        }
        catch (JSONException e) {
            throw new RuntimeException( e );
        }
    }


    public JSONObject store() {
        return store;
    }


    @Override
    public String toString() {
        return store.toString();
    }


    public String format() {
        return store().optString( "format" );
    }


    public void setFormat( String format ) {
        put( "format", format );
    }

    @SuppressWarnings("unchecked")
    public void putAll( JSONObject newValues ) {
        newValues.keys().forEachRemaining( key -> {
            try {
                store.put( (String)key, newValues.get( (String)key ));
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        } );
    }
}
