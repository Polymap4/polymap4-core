/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.feature.buffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.ListenerList;

/**
 * Global/static table of last-modified timestamps for every layer. This helps to
 * figure if the features of a data store has been changed by another user. This does
 * not go to the underlying store to check if the data has been modified by another
 * party outside this JVM.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureStoreVersion {

    private static Log log = LogFactory.getLog( FeatureStoreVersion.class );
    
    public static final long            UNMODIFIED = 0;
    
    private static int                  MAX_MAP_SIZE = 1024;
    
    private static Map<String,Long>     versions = new HashMap();
    
    private static ReadWriteLock        rwLock = new ReentrantReadWriteLock();
    
    private static ListenerList<IFeatureStoreListener> listeners 
            = new ListenerList( ListenerList.EQUALITY, ListenerList.WEAK );


    /**
     * Adds a <b>weak</b> reference to the given listener. Has no effect if the same
     * listener is already registered.
     * <p/>
     * The listener is called from inside a Job. So make sure to synchronize changes
     * to the UI with the {@link Display}.
     * 
     * @param listener
     * @return True if the listener was not yet registered.
     */
    public static boolean addListener( IFeatureStoreListener listener ) {
        return listeners.add( listener );
    }
    
    public static boolean removeListener( IFeatureStoreListener listener ) {
        return listeners.remove( listener );
    }
    
    
    public static long forLayer( ILayer layer ) {
        assert layer != null;
        try {
            rwLock.readLock().lock();
            
            Long result = versions.get( layer.id() );
            return result != null ? result : UNMODIFIED;
        }
        finally {
            rwLock.readLock().unlock();
        }
        
    }
    
    
    public static long checkSetForLayer( ILayer layer, long expected )
    throws ConcurrentModificationException {
        assert layer != null;
        try {
            rwLock.writeLock().lock();

            long newVersion = System.currentTimeMillis();
            Long old = versions.put( layer.id(), newVersion );
            if (old != null && old != expected) {
                versions.put( layer.id(), old );
                throw new ConcurrentModificationException();
            }
            
            if (versions.size() > MAX_MAP_SIZE) {
                log.warn( "Global versions map has reached MAX_MAP_SIZE: " + versions.size() );
            }
            
            fireEvent( layer, newVersion );
            
            return newVersion;
        }
        finally {
            rwLock.writeLock().unlock();
        }
    }

    
    private static void fireEvent( final ILayer layer, final long newVersion ) {
        Job job = new Job( "FeatureStoreListeners" ) {
            
            protected IStatus run( IProgressMonitor monitor ) {
                FeatureStoreEvent ev = new FeatureStoreEvent( layer.id(), layer.id(), newVersion );
                
                for (IFeatureStoreListener listener : listeners) {
                    if (monitor.isCanceled() || Thread.interrupted()) {
                        return Status.CANCEL_STATUS;
                    }
                    try {
                        listener.featureStoreChange( ev );
                    }
                    catch( Exception e ) {
                        log.warn( e );
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setPriority( Job.DECORATE );
        job.schedule();
    }
    
}
