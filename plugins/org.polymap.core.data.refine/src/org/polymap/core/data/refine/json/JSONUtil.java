package org.polymap.core.data.refine.json;

import org.json.JSONException;
import org.json.JSONObject;

public final class JSONUtil {

    public static String getString( JSONObject container, String keysAsString, String defaultValue )
            throws JSONException {
        container = findContainer( container, keysAsString );
        if (container == null) {
            return defaultValue;
        }
        String result = container.getString( lastKey( keysAsString ) );
        return result == null ? defaultValue : result;
    }


    private static String lastKey( String keysAsString ) {
        String[] keys = keysAsString.split( "\\." );
        String lastKey = keys[keys.length - 1];
        return lastKey;
    }


    private static JSONObject findContainer( JSONObject container, String keysAsString ) throws JSONException {
        String[] keys = keysAsString.split( "\\." );
        String lastKey = keys[keys.length - 1];
        if (keys.length > 1) {
            container = getObject( container,
                    keysAsString.substring( 0, keysAsString.lastIndexOf( lastKey ) - 1 ) );
        }
        return container;
    }


    public static JSONObject getObject( final JSONObject in, final String keysAsString )
            throws JSONException {
        JSONObject out = in;
        String[] keys = keysAsString.split( "\\." );
        for (String key : keys) {
            if (out == null) {
                return null;
            }
            // check for array
            int indexStart = key.indexOf( "[" );
            if (indexStart != -1) {
                int index = Integer.valueOf( key.substring( indexStart + 1, key.length() - 1 ) );
                key = key.substring( 0, indexStart );
                out = out.getJSONArray( key ).getJSONObject( index );
            }
            else {
                out = out.getJSONObject( key );
            }
        }
        return out;
    }


    public static Boolean getBoolean( JSONObject container, String keysAsString,
            Boolean defaultValue ) throws JSONException {
        container = findContainer( container, keysAsString );
        if (container == null) {
            return defaultValue;
        }
        Boolean result = container.getBoolean( lastKey( keysAsString ) );
        return result == null ? defaultValue : result;
    }


    public static Integer getInteger( JSONObject container, String keysAsString, Integer defaultValue ) throws JSONException {
        container = findContainer( container, keysAsString );
        if (container == null) {
            return defaultValue;
        }
        Integer result = container.getInt( lastKey( keysAsString ) );
        return result == null ? defaultValue : result;
    }

}
