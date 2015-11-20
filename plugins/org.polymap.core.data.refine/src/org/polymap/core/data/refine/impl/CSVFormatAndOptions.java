/*
 * polymap.org 
 * Copyright (C) @year@ individual contributors as indicated by the @authors tag. 
 * All rights reserved.
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

import org.json.JSONObject;

public class CSVFormatAndOptions extends LineBasedFormatAndOptions {

    public static CSVFormatAndOptions createDefault() {
        try {
            return new CSVFormatAndOptions( new JSONObject(
                    "{\"separator\":\"\\t\",\"ignoreLines\":-1,\"headerLines\":1,\"skipDataLines\":0,\"limit\":-1,\"storeBlankRows\":false,"
                            + "\"guessCellValueTypes\":false,\"processQuotes\":true,\"storeBlankCellsAsNulls\":true,\"includeFileSources\":false}" ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    public void putAll(JSONObject newValues) {
        super.putAll(newValues);
        // disable quote character during import
        setQuoteCharacter("\0");
        put("storeBlankRows", false);
//        setProcessQuotes(false);
    }

    public CSVFormatAndOptions(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String encoding() {
        return store().optString("encoding");
    }

    public void setEncoding(String encoding) {
        put("encoding", encoding);
    }

    public String separator() {
        return store().optString("separator");
    }

    public void setSeparator(String separator) {
        put("separator", separator);
    }

    public String quoteCharacter() {
        return store().optString("quoteCharacter");
    }

    public void setQuoteCharacter(String quoteCharacter) {
        put("quoteCharacter", quoteCharacter);
    }

    public boolean processQuotes() {
        return store().optBoolean("processQuotes");
    }

    public void setProcessQuotes(boolean processQuotes) {
        put("processQuotes", processQuotes);
    }
}
