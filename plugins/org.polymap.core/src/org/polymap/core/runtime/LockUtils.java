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
package org.polymap.core.runtime;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LockUtils {

    private static Log log = LogFactory.getLog( LockUtils.class );


    /**
     * Executes the given callable inside read lock of the given lock. The call()
     * method may aquire the write lock during execution. Both locks are released
     * after this method returns.
     * 
     * @param <V>
     * @param rwLock
     * @param task
     * @return The result of the given {@link #task}.
     * @throws Exception The exception from the {@link #task}.
     */
    public static <V> V withReadLock( ReentrantReadWriteLock rwLock, Callable<V> task )
    throws Exception {
        try {
            rwLock.readLock().lock();
            
            return task.call();
        }
        finally {
            if (rwLock.writeLock().isHeldByCurrentThread()) {
                rwLock.readLock().lock();
                rwLock.writeLock().unlock();
            }
            rwLock.readLock().unlock();
        }
    }
    
    
    /**
     * Executes the given runnable inside read lock of the given lock.
     *
     * @see #withReadLock(ReentrantReadWriteLock, Callable)
     * @throws Exception The exception from the {@link #task}.
     */
    public static void withReadLock( ReentrantReadWriteLock rwLock, final Runnable task ) {
        try {
            withReadLock( rwLock, new Callable() {
                public Object call() throws Exception {
                    task.run();
                    return null;
                }
            });
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public static void upgrade( ReentrantReadWriteLock rwLock ) {
        rwLock.readLock().unlock();
        rwLock.writeLock().lock();
    }
    
}
