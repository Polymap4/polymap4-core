/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.openlayers.rap.widget.util;

/**
 * Simple helper to build HTML/JavaScript code strings.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Stringer {

    public static final int     DEFAULT_CAPACITY = 1024;
    
    private StringBuilder       builder = new StringBuilder( DEFAULT_CAPACITY );
    
    private String              sep = "";
    
    private String              nullReplace = null;
    
    
    public Stringer() {
    }

    public Stringer( Object first, Object... parts ) {
        add( first, parts );
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public Stringer replaceNulls( String replace ) {
        assert replace != null;
        nullReplace = replace;
        return this;
    }
    
    public Stringer separator( String _sep ) {
        this.sep = _sep;
        return this;
    }
    
    public Stringer add( Object first, Object... parts ) {
        append( first );
        for (Object part : parts) {
            append( part );
        }
        return this;
    }

    public Stringer add( Object[] parts ) {
        for (Object part : parts) {
            append( part );
        }
        return this;        
    }

    public Stringer add( Iterable parts ) {
        for (Object part : parts) {
            append( part );
        }
        return this;        
    }

    protected void append( Object part ) {
        if (part == null) {
            if (nullReplace == null) {
                throw new IllegalArgumentException( "String part is null and no null replacement was given." );
            }
            else {
                part = nullReplace;
            }
        }
        if (builder.length() > 0 && sep.length() > 0) {
            builder.append( sep );
        }
        builder.append( part.toString() );
    }
    
}
