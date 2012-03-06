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
package org.polymap.core.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple facility to measure elapsed time. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Timer {

    private static Log log = LogFactory.getLog( Timer.class );
    
    private long        start = -1;
    
    public Timer() {
        start();
    }
    
    public Timer start() {
        start = System.currentTimeMillis();
        return this;
    }
    
    public Timer stop() {
        start = -1;
        return this;
    }
    
    public boolean isStarted() {
        return start > 0;
    }
    
    public long elapsedTime() {
        return isStarted() ? System.currentTimeMillis() - start : 0;
    }
    
    public void print() {
        System.out.println( "Time: " + elapsedTime() + "ms" );
    }
    
}
