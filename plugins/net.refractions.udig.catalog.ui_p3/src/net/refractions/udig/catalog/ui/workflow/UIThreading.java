/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004-2008, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog.ui.workflow;


/**
 * Threading strategy that uses the calling thread to execute jobs.
 * 
 * @author falko
 * @since 1.2.0
 */
public class UIThreading 
        implements ThreadingStrategy {
    
    public synchronized void init() {
    }
    
    
    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    
    public void shutdown() {
    }

    
    public void run( final Runnable runnable ) {
        runnable.run();
    }

}
