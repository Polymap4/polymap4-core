/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag.
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
package org.polymap.rhei.data;

import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import org.eclipse.rwt.RWT;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 */
public class Messages {

    private static final String BUNDLE_NAME = RheiDataPlugin.PLUGIN_ID + ".messages"; //$NON-NLS-1$


    private Messages() {
        // prevent instantiation
    }


    public static String get( String key ) {
        ClassLoader cl = Messages.class.getClassLoader();
        try {
            // getBundle() caches the bundles
            ResourceBundle bundle = ResourceBundle.getBundle( BUNDLE_NAME, RWT.getLocale(), cl );
            return bundle.getString( key );
        }
        catch (Exception e) {
            return StringUtils.substringAfterLast( key, "_" );
        }
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
