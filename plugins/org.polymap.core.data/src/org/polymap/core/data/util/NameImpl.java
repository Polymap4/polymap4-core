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
package org.polymap.core.data.util;

import org.opengis.feature.type.Name;

import org.apache.commons.lang.StringUtils;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NameImpl
        extends org.geotools.feature.NameImpl {

    public static final String  DEFAULT_SEPARATOR = ":";
    
    /**
     * 
     *
     * @param name The String to parse.
     * @return Newly created {@link Name} instance.
     */
    public static final Name parse( String name ) {
        return parse( name, DEFAULT_SEPARATOR );
    }
    
    /**
     * 
     *
     * @param name The String to parse.
     * @param separator The separator to use for parsing.
     * @return Newly created {@link Name} instance.
     */
    public static final Name parse( String name, String separator ) {
        assert name != null && name.length() > 0 && separator != null && separator.length() > 0;
        if (name.contains( separator )) {
            return new NameImpl( StringUtils.substringBeforeLast( name, separator ),
                    separator,
                    StringUtils.substringAfterLast( name, separator ) );
        }
        else {
            return new NameImpl( name );
        }
    }

    
    // instance *******************************************
    
    
    public NameImpl( String localpart ) {
        this( null, DEFAULT_SEPARATOR, localpart );
    }

    public NameImpl( String namespace, String localpart ) {
        this( namespace, DEFAULT_SEPARATOR, localpart );
    }

    public NameImpl( String namespace, String separator, String localpart ) {
        super( namespace, separator, localpart );
        assert localpart != null && separator != null;
    }

//    @Override
//    public String getURI() {
//        return Joiner.on( getSeparator() ).skipNulls().join( getNamespaceURI(), getLocalPart() );
//    }
    
}
