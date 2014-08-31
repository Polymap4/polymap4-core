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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.mp.AsyncExecutor;
import org.polymap.core.runtime.mp.ForEach;
import org.polymap.core.runtime.mp.Producer;
import org.polymap.core.runtime.mp.SyncExecutor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MPTest
        extends TestCase {

    private static Log log = LogFactory.getLog( MPTest.class );

    private int                 arraySize = 1000*1000;
    
    private List<Function>      procs = new ArrayList();

    private Timer               timer;
    

    public MPTest() {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
    }
    

    protected void setUp() throws Exception {
        timer = new Timer();
    }


    protected void tearDown() throws Exception {
        System.out.println( "*** Time: " + timer.elapsedTime() + "ms" );
    }


    public void testAsync() throws Exception {
        ForEach.defaultExecutorFactory = new AsyncExecutor.AsyncFactory();
        process();   
    }


    public void testSync() throws Exception {
        ForEach.defaultExecutorFactory = new SyncExecutor.SyncFactory();
        process();   
    }


    protected void process() {
        int count = Iterables.size( ForEach.in( new SBProducer() ).chunked( 1000 )
            .doParallel( new UpperCase() )
            .doParallel( new LowerCase() )
            .doParallel( new UpperCase() )
            .doParallel( new LowerCase() )
            .doParallel( new Quote() ) );

        System.out.println( ForEach.defaultExecutorFactory.getClass().getSimpleName() + " -- Result: " + count );
    }


    public void testPlain() throws Exception {
        for (StringBuilder elm : new SBProducer()) {
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
            elm.insert( 0, '\'' ).append( '\'' );
        }
    }


    private final class SBProducer
            extends Producer<StringBuilder> {

        String          s;
        
        public SBProducer() {
            StringBuilder sb = new StringBuilder( 1024 );
            sb.append( "source = new ArrayList( arraySize ); source = new ArrayList( arraySize ); StringBuilder sb = new StringBuilder();" );
            sb.append( "source = new ArrayList( arraySize ); source = new ArrayList( arraySize ); StringBuilder sb = new StringBuilder();" );
            sb.append( "source = new ArrayList( arraySize ); source = new ArrayList( arraySize ); StringBuilder sb = new StringBuilder();" );
            s = sb.toString();
        }

        public StringBuilder produce() {
            return new StringBuilder( s );
        }

        public int size() {
            return arraySize;
        }
    };


    private final class Quote
            implements Function<StringBuilder,StringBuilder> {

        public final StringBuilder apply( StringBuilder elm ) {
            return elm.insert( 0, '\'' ).append( '\'' );
        }
    }


    private final class LowerCase
            implements Function<StringBuilder,StringBuilder> {

        public final StringBuilder apply( StringBuilder elm ) {
            for (int i = 0; i < elm.length(); i++) {
                char c = Character.toLowerCase( elm.charAt( i ) );
                elm.setCharAt( i, c );
            }
            return elm;
        }
    }


    private final class UpperCase
            implements Function<StringBuilder,StringBuilder> {

        public final StringBuilder apply( StringBuilder elm ) {
            for (int i = 0; i < elm.length(); i++) {
                char c = Character.toUpperCase( elm.charAt( i ) );
                elm.setCharAt( i, c );
            }
            return elm;
        }
    }
    
}
