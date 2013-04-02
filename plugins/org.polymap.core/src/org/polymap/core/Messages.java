/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core;

import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import org.eclipse.rwt.RWT;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.MessagesImpl;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 *         <li>24.06.2009: created</li>
 * @version $Revision$
 */
public class Messages {

    private static final String BUNDLE_NAME = CorePlugin.PLUGIN_ID + ".messages"; //$NON-NLS-1$

    private static final MessagesImpl instance = new MessagesImpl( BUNDLE_NAME, Messages.class.getClassLoader() );

    
    private Messages() {
    }

    public static String get( String key, Object... args ) {
        return instance.get( key, args );
    }
    
    public static IMessages forClass( Class type ) {
        return instance.forClass( type );
    }

    public static IMessages forPrefix( String _prefix ) {
        return instance.forPrefix( _prefix );
    }

    public static String getForClass( String keySuffix ) {
        Exception e = new Exception();
        e.fillInStackTrace();
        StackTraceElement[] trace = e.getStackTrace();
        StackTraceElement elm = trace[trace.length-1];
        
        StringBuffer key = new StringBuffer( 64 );
        key.append( StringUtils.substringAfterLast( elm.getClassName(), "." ) ) 
                .append( "_" )
                .append( key );
        
        ClassLoader cl = Messages.class.getClassLoader();
        // getBundle() caches the bundles
        ResourceBundle bundle =
                ResourceBundle.getBundle( BUNDLE_NAME, RWT.getLocale(), cl );
        return bundle.getString( key.toString() );
    }
    
    public static Messages get() {
        Class clazz = Messages.class;
        return (Messages)RWT.NLS.getISO8859_1Encoded( BUNDLE_NAME, clazz );
    }

}
