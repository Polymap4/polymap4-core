/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.runtime.mp.test;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.mp.AsyncExecutor;
import org.polymap.core.runtime.mp.ForEach;
import org.polymap.core.runtime.mp.Parallel;
import org.polymap.core.runtime.mp.Processor;
import org.polymap.core.runtime.mp.SyncExecutor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MPTest
        extends TestCase {

    private static Log log = LogFactory.getLog( MPTest.class );

    private int                 arraySize = 500*1000;
    
    private List<String>        source;
    
    private List                result;
    
    private List<Processor>     procs = new ArrayList();

    private Timer               timer;
    
    
    public MPTest() {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );

        timer = new Timer();
        StringBuilder sb = new StringBuilder( "StringBuilder sb = new StringBuilder();" );
        
        source = new ArrayList( arraySize );
        for (int i=0; i<arraySize; i++) {
            source.add( "source = new ArrayList( arraySize ); source = new ArrayList( arraySize ); StringBuilder sb = new StringBuilder();" + String.valueOf( i*1000 ) );
        }
        System.out.println( "Data created: " + timer.elapsedTime() + "ms" );
    }
    

    protected void setUp() throws Exception {
        timer = new Timer();
    }


    protected void tearDown() throws Exception {
        System.out.println( "*** Result: " + result.size() );
        System.out.println( "*** Time: " + timer.elapsedTime() + "ms" );
    }


    public void testAsync()
    throws Exception {
        ForEach.executorFactory = new AsyncExecutor.AsyncFactory();
        process();   
    }


    public void testSync()
    throws Exception {
        ForEach.executorFactory = new SyncExecutor.SyncFactory();
        process();   
    }


    protected void process() {
        result = ForEach.in( source )/* .chunked( 10000 ) */
            .doFirst( new Parallel<StringBuilder,String>() {
                public StringBuilder process( String elm ) {
                    return new StringBuilder( elm );
                }
            })
            .doNext( new UpperCase() )
            .doNext( new LowerCase() )
            .doNext( new Quote() )
            .asList();
    }


    public void testPlain()
    throws Exception {
        result = new ArrayList();
        for (String s : source) {
            StringBuilder elm = new StringBuilder( s );
            // upperCase
            for (int i = 0; i < elm.length(); i++) {
                char c = Character.toUpperCase( elm.charAt( i ) );
                elm.setCharAt( i, c );
            }
            // lowerCase
            for (int i = 0; i < elm.length(); i++) {
                char c = Character.toLowerCase( elm.charAt( i ) );
                elm.setCharAt( i, c );
            }
            // quote
            result.add( elm.insert( 0, '\'' ).append( '\'' ) );
        }
    }


    private final class Quote
            implements Parallel<StringBuilder, StringBuilder> {

        public final StringBuilder process( StringBuilder elm ) {
            return elm.insert( 0, '\'' ).append( '\'' );
        }
    }


    private final class LowerCase
            implements Parallel<StringBuilder, StringBuilder> {

        public final StringBuilder process( StringBuilder elm ) {
            for (int i = 0; i < elm.length(); i++) {
                char c = Character.toLowerCase( elm.charAt( i ) );
                elm.setCharAt( i, c );
            }
            return elm;
        }
    }


    private final class UpperCase
            implements Parallel<StringBuilder, StringBuilder> {

        public final StringBuilder process( StringBuilder elm ) {
            for (int i = 0; i < elm.length(); i++) {
                char c = Character.toUpperCase( elm.charAt( i ) );
                elm.setCharAt( i, c );
            }
            return elm;
        }
    }
    
}
