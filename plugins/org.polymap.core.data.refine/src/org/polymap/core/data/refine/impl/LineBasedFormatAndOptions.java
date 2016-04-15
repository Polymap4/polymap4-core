package org.polymap.core.data.refine.impl;

import org.json.JSONObject;

public class LineBasedFormatAndOptions
        extends FormatAndOptions {

    public LineBasedFormatAndOptions( JSONObject jsonObject ) {
        super( jsonObject );
    }


    public void setHeaderLines( int headerLines ) {
        put( "headerLines", headerLines );
    }


    public int headerLines() {
        return Integer.parseInt( store().optString( "headerLines", "1" ) );
    }


    public void setIgnoreLines( int ignoreLines ) {
        put( "ignoreLines", ignoreLines );
    }


    public int ignoreLines() {
        return Integer.parseInt( store().optString( "ignoreLines", "0" ) );
    }


    public void setSkipDataLines( int skipDataLines ) {
        put( "skipDataLines", skipDataLines );
    }


    public int skipDataLines() {
        return Integer.parseInt( store().optString( "skipDataLines", "0" ) );
    }

}