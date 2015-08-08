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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.recordstore.lucene.LuceneRecordStore;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneRecordStoreTest
        extends AbstractRecordStoreTest {

    private static Log log = LogFactory.getLog( LuceneRecordStoreTest.class );

    
    public LuceneRecordStoreTest( String name ) {
        super( name );
    }

    protected void setUp() throws Exception {
        store = new LuceneRecordStore( new File( "/tmp", "LuceneRecordStore" ), true );
        
//      store = new LuceneRecordStore();
        
//      Cache<Object,Document> cache = CacheManager.instance().newCache( 
//              new CacheConfig().setDefaultElementSize( 1024 ) );
//      store.setDocumentCache( cache );
    }

    protected void tearDown() throws Exception {
        log.info( "closing store..." );
//        store.close();
    }
    
}
