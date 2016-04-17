/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.style.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.locking.OptimisticLocking;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.recordstore.IRecordStore;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class StyleRepository
        implements AutoCloseable {

    private static Log log = LogFactory.getLog( StyleRepository.class );
    
    private EntityRepository        repo;

    
    /**
     * Creates a repository instance backed by a {@link LuceneRecordStore} in the
     * given dataDir.
     * 
     * @param dataDir The directory of the database or null, for testing in-memory.
     * @throws IOException 
     */
    public StyleRepository( File dataDir ) throws IOException {
        IRecordStore store = LuceneRecordStore.newConfiguration()
                .indexDir.put( dataDir )
                .create();
        
        repo = EntityRepository.newConfiguration()
                .entities.set( new Class[] {
                        FeatureStyle.class, 
                        AttributeValue.class, 
                        ConstantColor.class, 
                        ConstantNumber.class,
                        ConstantNumbersFromFilter.class,
                        ConstantBoolean.class,
                        PointStyle.class} )
                .store.set( 
                        new OptimisticLocking(
                        //new FulltextIndexer( fulltextIndex, new TypeFilter( Waldbesitzer.class ), newArrayList( wbTransformer ),
                        new RecordStoreAdapter( store ) ) )
                .create();
    }


    @Override
    public void close() throws Exception {
        if (repo != null) {
            repo.close();
        }
    }


    public UnitOfWork newUnitOfWork() {
        return repo.newUnitOfWork();
    }
    
}
