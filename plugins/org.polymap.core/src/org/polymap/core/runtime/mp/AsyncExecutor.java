/* 
 * polymap.org
 * Copyright (C) 2011-2013, Falko Bräutigam. All rigths reserved.
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
package org.polymap.core.runtime.mp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.mp.ForEach.Processor;
import org.polymap.core.runtime.mp.ForEachExecutor.ProcessorContext;

/**
 * Executes a {@link ForEach} loop using a queue of concurrently working threads.
 * <p/>
 * The {@link #queueCapacity} defines the maximum number tasks/chunks that are
 * processed concurrently. It is set to: <code>2*number_of_processors</code> by
 * default.
 * <p/>
 * The {@link #chunkSize} defines the number of elements that are passed to a thread
 * for one processing step. The default is set to:
 * <code>source.size()/(queueCapacity*4)</code>. That is, for a 2 core system the
 * executor tries to start up to 4 concurrent threads. Every thread executes a total
 * of 4 chunks. The entire source collection is split into 16 processing chunks.
 * <p/>
 * <b>Implementation Note:</b>
 * <p/>
 * It seems that different JDKs have very different characteristics when executing
 * concurrent threads.
 * 
 * <pre>
 *   -XX:+UseConcMarkSweepGC 
 *   -XX:+UseParNewGC
 * </pre>
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AsyncExecutor<S,T>
        implements ForEachExecutor<S,T>, ProcessorContext {

    private static Log log = LogFactory.getLog( AsyncExecutor.class );

    private static final int                DEFAULT_PIPELINE_QUEUE_CAPACITY = 
            2 * Runtime.getRuntime().availableProcessors();
    
    private static ThreadPoolExecutor       executorService = (ThreadPoolExecutor)Polymap.executorService();
    

    public static class AsyncFactory implements Factory {
        public ForEachExecutor newExecutor( ForEach foreach ) {
            return new AsyncExecutor( foreach );
        }
    }
    
    
    // instance *******************************************
    
    private ForEach                 forEach;
    
    private Iterator<S>             source;
    
    private ProcessorContext[]      chain;
    
    /** 
     * The number of elements in every chunk. The default is set to:
     * <code>source.size()/(queueCapacity*4)</code>.
     */
    private int                     chunkSize = -1;

    /** The number of the next chunk that is enqueued. */
    private int                     nextChunkNum = 0;
    
    /**
     * The maximum number tasks/chunks that are processed concurrently.
     */
    private int                     queueCapacity = DEFAULT_PIPELINE_QUEUE_CAPACITY;
    
    /**
     * The number of currently enqueued tasks/chunks.
     */
    private AtomicInteger           queueSize = new AtomicInteger( 0 );
    
    private BlockingQueue<Chunk>    results = new LinkedTransferQueue();  //new ArrayBlockingQueue( queueCapacity );
    
    private Iterator                currentResultChunk;
    
    private T                       next;


    /**
     * Creates a new executor with default settings.
     * <p/>
     * The queueCapacity is set to: <code>2 * numOfProcs</code>.
     * <p/>
     * The number of elements in every chunk is set to:
     * <code>source.size() / (queueCapacity * 4)</code>.
     * 
     * @param forEach
     */
    public AsyncExecutor( ForEach<S,T> forEach ) {
        this.forEach = forEach;
        this.source = forEach.source().iterator();
        
        // preset chunkSize
        if (forEach.source() instanceof Collection) {
            Collection sourceColl = (Collection)forEach.source();
            chunkSize = Math.max( 1, sourceColl.size() / (queueCapacity*4) );
        }
        else {
            chunkSize = 1000;
        }
        
        // create processor contexts
        List<Processor> procs = forEach.processors();
        chain = new ProcessorContext[ procs.size() + 1 ];
        for (int i=0; i<procs.size(); i++) {
            chain[i] = new AsyncProcessorContext( procs.get( i ), i );
        }
        // put me at the end of the chain
        chain[ procs.size() ] = this;
        
        // fill pipeline
        for (int i=0; i<queueCapacity && source.hasNext(); i++) {
            feedNextChunk();
        }
    }

    
    public void setChunkSize( int chunkSize ) {
        this.chunkSize = chunkSize > 0 ? chunkSize : this.chunkSize;
    }


    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        else if (currentResultChunk != null && currentResultChunk.hasNext()) {
            next = (T)currentResultChunk.next();
        }
        else {
            while (!isEndOfProcessing()) {                
                try {
                    Chunk chunk = results.poll( 100, TimeUnit.MILLISECONDS );
                    if (chunk != null) {
                        log.debug( "Took chunk from queue. chunkSize=" + chunk.elements.size() );
                        
                        // do this in main thread (instead in put()) to prevent #results queue
                        // to get very big if reading is slower than processing
                        if (source.hasNext()) {
                            feedNextChunk();
                        }
                    
                        currentResultChunk = chunk.elements.iterator();
                        if (currentResultChunk.hasNext()) {
                            next = (T)currentResultChunk.next();
                            return true;
                        }
                    }
                }
                catch (InterruptedException e) {
                    throw new RuntimeException( e );
                }
            }
        }
        return next != null;
    }

    
    public T next() {
        if (hasNext()) {
            T result = next;
            next = null;
            return result;
        }
        else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    // ***
    protected boolean isEndOfProcessing() {
        return queueSize.get() == 0 && results.size() == 0;
    }
    
    protected int remainingCapacity() {
        return queueCapacity - queueSize.get();    
    }
    
    protected void enqueue( Callable task ) {
        executorService.submit( task );
    }


    protected void feedNextChunk() {
        ArrayList chunk = new ArrayList( chunkSize );
        for (int i=0; source.hasNext() && i<chunkSize; i++) {
            chunk.add( source.next() );
        }
        log.debug( "Putting chunk to queue. chunkSize=" + chunk.size() + ", remainingCatacity=" + remainingCapacity() );
        queueSize.incrementAndGet();
        chain[0].put( new Chunk( chunk, nextChunkNum++ ) );
    }
    
    
    public void put( Chunk chunk ) {
        try {
            results.put( chunk );
            queueSize.decrementAndGet();
            log.debug( "Result chunk. queueSize=" + queueSize );
        }
        catch (InterruptedException e) {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * 
     */
    private final class AsyncProcessorContext
            implements ProcessorContext {

        protected int             chainNum;
        
        protected Processor       proc;
        
        protected Lock            serialProcessorLock = new ReentrantLock();
        
        protected Condition       inOrder = serialProcessorLock.newCondition();
        
        protected int             lastChunkNum = -1;
        
        
        protected AsyncProcessorContext( Processor proc, int chainNum ) {
            this.proc = proc;
            this.chainNum = chainNum;
        }


        public void put( final Chunk chunk ) {
            log.debug( "Got chunk. chainNum=" + chainNum + ", chunkSize=" + chunk.elements.size() );
            
            // wait for previous chunks if serial
            if (proc.maxThreads == 1) {
                try {
                    serialProcessorLock.lock();
                    while (chunk.chunkNum > lastChunkNum+1) {
                        try {
                            log.debug( "    Serial: awaiting 'inOrder' condition. chainNum=" + chainNum + ", nextChunkNum=" + chunk.chunkNum + ", lastChunk=" + lastChunkNum );
                            inOrder.await();
                            log.debug( "    Serial: signaled. 'inOrder' condition. chainNum=" + chainNum + ", nextChunkNum=" + chunk.chunkNum + ", lastChunk=" + lastChunkNum );
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException( e );
                        }
                    }
                }
                finally {
                    serialProcessorLock.unlock();
                }
            }
            
            // enqueue new task
            enqueue( new Callable() {
                public Object call() throws Exception {
                    log.debug( "Thread started: " + Thread.currentThread().getName() + ", chainNum=" + chainNum + ", chunkSize=" + chunk.elements.size() );

                    int size = chunk.elements.size();
                    for (int i=0; i<size; i++) {
                        chunk.elements.set( i, proc.delegate.apply( chunk.elements.get( i ) ) );
                    }

                    log.debug( "ASync task processed. chainNum=" + chainNum + ", chunkSize=" + chunk.elements.size() );

                    if (proc.maxThreads == 1) {
                        try {
                            serialProcessorLock.lock();                            
                            lastChunkNum = chunk.chunkNum;
                            log.debug( "    Serial: chainNum=" + chainNum + ", lastChunkNum=" + lastChunkNum );
                            inOrder.signalAll();
                        }
                        finally {
                            serialProcessorLock.unlock();
                        }
                    }

                    chain[ chainNum+1 ].put( chunk );
                    return null;
                }
            });
        }

    }
    
}
