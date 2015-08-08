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
package org.polymap.recordstore.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;

import org.polymap.recordstore.IRecordState;
import org.polymap.recordstore.IRecordStore;
import org.polymap.recordstore.IRecordStore.Updater;
import org.polymap.recordstore.RecordModel;
import org.polymap.recordstore.ResultSet;
import org.polymap.recordstore.SimpleQuery;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AbstractRecordStoreTest
        extends TestCase {

    private static Log log = LogFactory.getLog( AbstractRecordStoreTest.class );


    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.alkis.recordstore.lucene", "debug" );
    }
    
    protected IRecordStore          store;
    
    private long                    start;
    
    
    public AbstractRecordStoreTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
    }


    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void test() throws Exception {
        int loops = 100;
        createRecords( loops );
        readRecords( loops );
        queryRecords( loops );
    }


//    public void tstThreaded() throws Exception {
//        final int loops = 250;
//        final int threads = 4;
//        createRecords( loops*threads );
//        
//        List<Future> results = new ArrayList();
//        for (int i=0; i<threads; i++) {
//            results.add( Polymap.executorService().submit( new Callable() {
//                public Object call() throws Exception {
////                    readRecords( loops );
//                    queryRecords( loops );
//                    return new Object();
//                }
//            } ) );
//        }
//        // wait for results
//        for (Future result : results) {
//            result.get();
//        }
//    }

    
    protected void createRecords( int loops ) throws Exception {
        start = System.currentTimeMillis();
        final Timer timer = new Timer();
        Updater updater = store.prepareUpdate();
        try {
            for (int i=0; i<loops; i++) {
                TestRecord record = new TestRecord( store.newRecord() );
                record.payload.put( "LuceneRecordStore store = new LuceneRecordStore( new File LuceneRecordStore store = new LuceneRecordStore( new File LuceneRecordStore store = new LuceneRecordStore( new File" );
                record.type.put( "2" );
                record.count.put( i );
                updater.store( record.state() );
            }
            
            updater.apply();
        }
        catch (Exception e) {
            updater.discard();
            throw e;
        }
        log.info( "Records created: " + loops + " in " + timer.elapsedTime() + 
                "ms -> " + (double)timer.elapsedTime()/loops + "ms/loop" );
    }
        
    
    protected void readRecords( int loops ) throws Exception {
        final Timer timer = new Timer();
        int found = 0;
        for (int i=0; i<loops; i++) {
            IRecordState record = store.get( String.valueOf( start+i ) );
            if (record != null) {
                found ++;
            }
        }
        log.info( "Records read: " + found + " in " + timer.elapsedTime() + "ms -> " 
                + (double)timer.elapsedTime()/loops + "ms/loop" );
    }

    
    protected void queryRecords( int loops ) throws Exception {
        final Timer timer = new Timer();
        int found = 0;
        for (int i=0; i<loops; i++) {
//            RecordQuery query = new SimpleQuery()
//                    .eq( TestRecord.TYPE.count.name(), String.valueOf( i ) )
//                    .eq( TestRecord.TYPE.type.name(), "2" )
//                    .setMaxResults( 1 );
            
            SimpleQuery query = new SimpleQuery().setMaxResults( 1 );
            TestRecord template = new TestRecord( query );
            template.count.put( i );
            template.type.put( "2" );
            
            ResultSet result = store.find( query );

            //assertTrue( result.count() == 0);
            if (result.count() > 0) {
                found ++;
            }
            
//            TestRecord record = new TestRecord( result.get( 0 ) );
//            dummy = record.type.get();
        }
        log.info( "Records queried: " + found + " in " + timer.elapsedTime() + "ms -> " 
                + (double)timer.elapsedTime()/loops + "ms/loop" );
        
        store.close();
    }

    
    /**
     * 
     */
    public static class TestRecord
            extends RecordModel {
        
        public static final TestRecord  TYPE = type( TestRecord.class );
        
        public TestRecord( IRecordState state ) {
            super( state );
        }
        
        public Property<String>         type = new Property<String>( "type" );
        
        public Property<Integer>        count = new Property<Integer>( "count" );
        
        public Property<String>         payload = new Property<String>( "payload" );
        
    }

}
