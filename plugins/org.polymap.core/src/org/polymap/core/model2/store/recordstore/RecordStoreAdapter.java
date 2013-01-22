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
package org.polymap.core.model2.store.recordstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreSPI;
import org.polymap.core.model2.store.StoreUnitOfWork;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordStoreAdapter
        implements StoreSPI {

    private static Log log = LogFactory.getLog( RecordStoreAdapter.class );
    
    protected EntityRepository      repo;
    
    protected IRecordStore          store;

    private StoreRuntimeContext     context;
    
    
    public RecordStoreAdapter( IRecordStore store ) {
        this.store = store;
    }


    @SuppressWarnings("hiding")
    public void init( StoreRuntimeContext context ) {
        this.context = context;
        this.repo = context.getRepository();
    }


    public void close() {
    }


    public Object stateId( Object state ) {
        return ((IRecordState)state).id();
    }


    public StoreUnitOfWork createUnitOfWork() {
        return new RecordStoreUnitOfWork( context, this );
    }

}
