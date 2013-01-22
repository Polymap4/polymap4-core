/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.runtime.PolymapThreadPoolExecutor;
import org.polymap.core.runtime.Timer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ExecutorPerfTest
        extends TestCase {

    private static Log log = LogFactory.getLog( ExecutorPerfTest.class );
    
    private static int          runTimeSec = 10;
    
    
    public ExecutorPerfTest() {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
    }
    
    
    public void testExecutorService() throws Exception {
        ExecutorService executor = PolymapThreadPoolExecutor.newInstance();
        Timer timer = new Timer();
        // execute
        List<Future> futures = new LinkedList();
        while (timer.elapsedTime()/1000 < runTimeSec) {
            futures.add( executor.submit( new Task() ) );
        }
        // wait to complete
        for (Future future : futures) {
            future.get();
        }
        log.info( "ExecutorService: task=" + futures.size() + ", time=" + timer.elapsedTime() 
                + "ms, task/s=" + (futures.size()/(timer.elapsedTime()/1000)) );
    }
    
    
    public void testJobs() throws Exception {
        Timer timer = new Timer();
        // execute
        List<Job> jobs = new LinkedList();
        while (timer.elapsedTime()/1000 < runTimeSec) {
            Job job = new Job( "" ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    new Task().run();
                    return Status.OK_STATUS;
                }
            };
            job.schedule();
            jobs.add( job );
        }
        // wait to complete
        for (Job job : jobs) {
            job.join();
        }
        log.info( "Eclipse Jobs: task=" + jobs.size() + ", time=" + timer.elapsedTime() 
                + "ms, task/s=" + (jobs.size()/(timer.elapsedTime()/1000)) );
    }
    
    
    /**
     * Task: sort n Integer via Collections.sort() 
     */
    class Task
            implements Runnable {
        
        public void run() {
            List<Integer> nums = new ArrayList();
            for (int i=0; i<100; i++) {
                nums.add( i );
            }
            Collections.shuffle( nums );
            Collections.sort( nums );
        }
        
    }
    
}
