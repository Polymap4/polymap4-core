/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime;

import java.util.Random;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.config.Check;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.NumberRangeValidator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RejectedExecutionHandlers {

    private static Log log = LogFactory.getLog( RejectedExecutionHandlers.class );
    
    /**
     * Block the main thread if no thread is currently available in the {@link ThreadPoolExecutor}. 
     */
    public static class Blocking
            extends Configurable
            implements RejectedExecutionHandler {
        
        @DefaultInt( 1000 )
        @Check( value=NumberRangeValidator.class, args={"10","100000"} )
        public Config<Blocking,Integer> sleepMillis;
        
        @DefaultInt( 1000 )
        @Check( value=NumberRangeValidator.class, args={"10","100000"} )
        public Config<Blocking,Integer> sleepMillisIncrease;
        
        private Random                  rand = new Random();
        
        private Runnable                lastRejected;
        
        private int                     rejectCount;
        
        
        @Override
        public void rejectedExecution( Runnable r, ThreadPoolExecutor executor ) {
            rejectCount = lastRejected == r ? rejectCount+1 : 1;
            lastRejected = r;
            
            try {
                int actual =  rand.nextInt( sleepMillis.get() ) + ((rejectCount-1) * sleepMillisIncrease.get());
                log.debug( "sleep: " + actual );
                Thread.sleep( actual );
            } 
            catch (InterruptedException e) {}

            executor.execute( r );
        }
    }

}
