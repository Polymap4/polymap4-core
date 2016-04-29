/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.model;

/**
 * Commonly used web save fonts from
 * http://www.w3schools.com/cssref/css_websafe_fonts.asp
 *
 * @author Steffen Stundzig
 */
public enum FontFamily {
    georgia("Georgia, serif"), palatino("\"Palatino Linotype\", \"Book Antiqua\", Palatino, serif"), times(
            "\"Times New Roman\", Times, serif"), arial("Arial, Helvetica, sans-serif"), arialBlack(
                    "\"Arial Black\", Gadget, sans-serif"), comicSans("\"Comic Sans MS\", cursive, sans-serif");

    private String value;


    FontFamily( final String value ) {
        this.value = value;
    }
    
    public String value() {
        return value;
    }
}
