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
    georgia("Georgia, Serif"), palatino("Palatino Linotype, Book Antiqua, Palatino, Serif"), times(
            "Times New Roman, Times, Serif"), arial("Arial, Helvetica, SansSerif"), arialBlack(
                    "Arial Black, SansSerif"), comicSans("Comic Sans MS, SansSerif");

    private String value;


    FontFamily( final String value ) {
        this.value = value;
    }
    
    public String value() {
        return value;
    }
    
    public String[] families() {
        return value.split( "," );
    }
    
}
