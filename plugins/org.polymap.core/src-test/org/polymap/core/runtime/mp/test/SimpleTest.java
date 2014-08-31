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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.polymap.core.runtime.mp.AsyncExecutor;
import org.polymap.core.runtime.mp.ForEach;
import org.polymap.core.runtime.mp.SyncExecutor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SimpleTest
        extends TestCase {

    private static Log log = LogFactory.getLog( SimpleTest.class );
    
    private List<String>        source;
    
    private List<StringBuilder> result;
    

    public void setUp() throws Exception {
        source = Arrays.asList( "Sushi", "ist", "prima!" );
    }

    
    public void tearDown() throws Exception {
        System.out.println( "Result: " + result );
    }

    
    public void testAsync() {
        ForEach.defaultExecutorFactory = new AsyncExecutor.AsyncFactory();
        process();
    }
    
    
    public void testSync() {
        ForEach.defaultExecutorFactory = new SyncExecutor.SyncFactory();
        process();
    }
    
    
    protected void process() {
        result = Lists.newArrayList( ForEach.in( source )
            .doParallel( new Function<String,StringBuilder>() {
                 public final StringBuilder apply( String elm ) {
                      return new StringBuilder( elm );
                  }
             })
            .doParallel( new Function<StringBuilder,StringBuilder>() {
                 public final StringBuilder apply( StringBuilder elm ) {
                     return elm.insert( 0, '\'' ).append( '\'' );
                 }
             } ) );
    }
    
}
