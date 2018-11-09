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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.Bundle;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;

/**
 * Simple {@link Log} factory that reads settings from a properties file.
 * 
 * @see SimpleLog
 * @author Falko Bräutigam
 */
@SuppressWarnings( "restriction" )
public class SimpleLogFactory {

    private static final Level      DEFAULT_LEVEL = Level.INFO;

    private static final String     LOG_PROPERTIES = "simplelog.properties";
    
    private static List<Pair<String,Level>> levels = new ArrayList();
    
    private static LogFormat        format = new SimpleLogFormat();
    
    /**
     * Init: real properties file. 
     */
    static {
        Bundle bundle = ((EquinoxClassLoader)SimpleLogFactory.class.getClassLoader()).getBundle();
        System.out.print( "Initializing SimpleLog for '" +  bundle.getSymbolicName() + "'... " );
        File workspace = InternalPlatform.getDefault().getStateLocation( bundle, true )
                .toFile().getParentFile().getParentFile().getParentFile();
        try (
            InputStream fin = new FileInputStream( new File( workspace, LOG_PROPERTIES) );
            InputStream in = new BufferedInputStream( fin );
        ){
            Properties props = new Properties();
            props.load( in );
            props.entrySet().forEach( entry -> levels.add( Pair.of( 
                    (String)entry.getKey(), 
                    Level.valueOf( entry.getValue().toString().toUpperCase() ) ) ) );
            System.out.println( "done." );
        }
        catch (FileNotFoundException e) {
            System.out.println( "no properties file found." );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public static Log getLog( Class clazz ) {
        return getLog( clazz.getName() );
    }
    
    
    public static Log getLog( String name ) {
        // find most significant entry/level for this class
        Level level = DEFAULT_LEVEL;
        String longestPrefix = "";
        for (Pair<String,Level> entry : levels) {
            if (name.startsWith( entry.getKey() ) 
                    && entry.getKey().length() > longestPrefix.length()) {
                level = entry.getValue();
                longestPrefix = entry.getKey();        
            }
        }
        return level.equals( Level.INFO ) 
                ? new SimpleInfoLog( name, level, format )
                : new SimpleLog( name, level, format );
    }

}