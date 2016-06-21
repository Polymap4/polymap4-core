/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.catalog.local;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IUpdateableMetadata;
import org.polymap.core.catalog.IUpdateableMetadataCatalog;
import org.polymap.core.catalog.MetadataQuery;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.store.StoreSPI;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalMetadataCatalog
        implements IUpdateableMetadataCatalog {

    private static Log log = LogFactory.getLog( LocalMetadataCatalog.class );

    private EntityRepository        repo;
    
    private UnitOfWork              uow;
    
    
    public LocalMetadataCatalog( StoreSPI store ) {
        repo = EntityRepository.newConfiguration()
                .entities.set( new Class[] {LocalMetadata.class} )
                .store.set( store )
                .create();
        
        uow = repo.newUnitOfWork();
    }
    
    
    @Override
    public String getTitle() {
        return "Project resources";
    }


    @Override
    public String getDescription() {
        return "Local project resources";
    }


    @Override
    public void close() {
        uow.close();
        repo.close();
    }
    
    
    @Override
    public Optional<? extends IMetadata> entry( String identifier, IProgressMonitor monitor ) {
        org.polymap.model2.query.ResultSet<LocalMetadata> rs = uow.query( LocalMetadata.class )
                .where( Expressions.eq( LocalMetadata.TYPE.identifier, identifier ) )
                .maxResults( 2 )
                .execute();
        
        if (rs.size() > 1) {
            throw new IllegalStateException( "More than one result for identifier: " + identifier );
        }
        return rs.stream().findFirst();
    }


    @Override
    public MetadataQuery query( String query, IProgressMonitor monitor ) {
        return new MetadataQuery() {
            @Override
            public ResultSet execute() {
                // execute the query
                org.polymap.model2.query.ResultSet<LocalMetadata> rs = uow.query( LocalMetadata.class )
                        .maxResults( maxResults.get() )
                        .execute();
                
                // build ResultSet
                return new ResultSet() {
                    @Override
                    public Iterator<IMetadata> iterator() {
                        return rs.stream().map( md -> (IMetadata)md ).iterator();
                    }
                    @Override
                    public int size() {
                        return rs.size();
                    }
                    @Override
                    public void close() {
                        rs.close();
                    }
                    @Override
                    public void finalize() throws Throwable {
                        close();
                    }
                };
            }
        };
    }

    
    @Override
    public Updater prepareUpdate() {
        return new Updater() {

            private UnitOfWork  updaterUow = uow.newUnitOfWork();
            
            @Override
            public void close() {
                updaterUow.close();
            }

            @Override
            public void commit() throws Exception {
                updaterUow.commit();
                // XXX check/prevent concurrent updates?
                uow.commit();
            }
            
            @Override
            public void newEntry( Consumer<IUpdateableMetadata> initializer ) {
                uow.createEntity( LocalMetadata.class, null, (LocalMetadata prototype) -> {
                    prototype.setIdentifier( UUID.randomUUID().toString() );
                    log.info( "UUID: " + prototype.getIdentifier() );
                    initializer.accept( prototype );
                    return prototype;
                });
            }

            @Override
            public void updateEntry( String identifier, Consumer<IUpdateableMetadata> updater ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }

            @Override
            public void removeEntry( String identifier ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        };
    }
    
}
