/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.model2.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * The {@link SimpleModelTest} with {@link IRecordStore}/Lucene backend.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class LuceneSimpleModelTest
        extends SimpleModelTest {

    private static final Log log = LogFactory.getLog( LuceneSimpleModelTest.class );

    protected IRecordStore          store;

    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore.lucene", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.feature.recordstore", "debug" );
    }

    
    public LuceneSimpleModelTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        store = new LuceneRecordStore();
        repo = EntityRepository.newConfiguration()
                .setStore( new RecordStoreAdapter( store ) )
                .setEntities( new Class[] {Employee.class} )
                .create();
        uow = repo.newUnitOfWork();
    }

    
    protected Object stateId( Object state ) {
        return ((IRecordState)state).id();    
    }


}
