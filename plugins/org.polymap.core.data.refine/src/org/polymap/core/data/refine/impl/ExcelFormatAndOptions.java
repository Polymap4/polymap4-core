/*
 * polymap.org Copyright (C) @year@ individual contributors as indicated by
 * the @authors tag. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.data.refine.impl;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class ExcelFormatAndOptions
        extends LineBasedFormatAndOptions {

    public class SheetRecord {

        public String name;

        public int    rows;


        public SheetRecord( String name, int rows ) {
            this.name = name;
            this.rows = rows;
        }

    }


    public static ExcelFormatAndOptions createDefault() {
        try {
            return new ExcelFormatAndOptions( new JSONObject(
                    "{\"storeBlankRows\":false,\"sheetRecords\":[{\"name\":\"Sheet1\",\"rows\":69,\"selected\":true}],\"includeFileSources\":false,\"skipDataLines\":0,\"headerLines\":1,\"ignoreLines\":-1,\"storeBlankCellsAsNulls\":true}}" ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public ExcelFormatAndOptions( JSONObject jsonObject ) {
        super( jsonObject );
        sheetRecords = null;
    }

    private List<SheetRecord> sheetRecords;


    public List<SheetRecord> sheetRecords() {
        JSONArray sheets = store().optJSONArray( "sheetRecords" );
        if (sheetRecords == null) {
            sheetRecords = Lists.newArrayList();
            for (int i = 0; i < sheets.length(); i++) {
                JSONObject sheet = sheets.getJSONObject( i );
                sheetRecords.add(
                        new SheetRecord( sheet.getString( "name" ), sheet.getInt( "rows" ) ) );
            }
        }
        return sheetRecords;
    }


    public void setSheet( int sheet ) {
        JSONArray sheets = new JSONArray();
        sheets.put( sheet );
        store().put( "sheets", sheets );
    }


    public int sheets() {
        return Integer.parseInt( store().optString( "sheets", "0" ) );
    }

}
