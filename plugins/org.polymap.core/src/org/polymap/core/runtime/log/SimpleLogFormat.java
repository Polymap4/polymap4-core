/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.runtime.log;

import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.rightPad;

import org.apache.commons.lang3.StringUtils;

/**
 * Simple, hard-coded output format.
 *
 * @author Falko Bräutigam
 */
public final class SimpleLogFormat
        implements LogFormat {

    protected static final int      MAX_NAME_WIDTH = 23;

    @Override
    public void log( String name, Level level, Object msg, Throwable... e ) {
        StringBuilder buf = new StringBuilder( 128 );
        // level
        buf.append( "[" ).append( level ).append( "]" ).append( rightPad( "", 5 - level.toString().length() ) );
        // (class)name
        String simplename = StringUtils.substringAfterLast( name, "." );
        buf.append( " " ).append( abbreviate( rightPad( simplename, MAX_NAME_WIDTH ), MAX_NAME_WIDTH ) ).append( ":" );
        buf.append( " " ).append(  msg.toString() );

        if (level.ordinal() >= Level.WARN.ordinal()) {
            System.err.println( buf.toString() );
        }
        else {
            System.out.println( buf.toString() );                
        }

        for (int i=0; i<e.length; i++) {
            if (e[i] != null) {
                e[i].printStackTrace( System.err );
            }
        }
    }

}