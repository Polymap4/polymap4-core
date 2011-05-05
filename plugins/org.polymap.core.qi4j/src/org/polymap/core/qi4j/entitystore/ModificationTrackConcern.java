/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Falko Bräutigam. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.polymap.core.qi4j.entitystore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.lang.ref.WeakReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;

import org.polymap.core.model.Entity;
import org.polymap.core.qi4j.event.ModelChangeTracker;

/**
 * Concern that helps EntityStores to track entity modifications together with
 * {@link GlobalEntityVersions}.
 * <p/>
 * It caches the versions of state that it loads, and forgets them when the
 * state is committed. For normal operation this means that it does not have to
 * go down to the underlying store to get the current version. Whenever there is
 * a concurrent modification the store will most likely have to check with the
 * underlying store what the current version is.
 * <p/>
 * This implementation does never go to the underlying store. It holds all
 * versions of every entity that was saved for the livetime of this store
 * (probably JVM run).
 */
public abstract class ModificationTrackConcern
        extends ConcernOf<EntityStore>
        implements EntityStore {

    private static Log log = LogFactory.getLog( ModificationTrackConcern.class );

    
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module ) {
        final EntityStoreUnitOfWork uow = next.newUnitOfWork( usecase, module );
        return new ModificationTrackEntityStoreUnitOfWork( uow, module );
    }

    
    public static boolean hasChanged( Entity entity ) {
        assert entity != null;

        log.warn( "No check currently!" );
        return false;
        
//        Long entityLastModified = entityLastModified( entity.state() );  //entity._lastModified().get();
//        Long storeLastModified = versions.get( EntityReference.parseEntityReference( entity.id() ) );
//        
//        log.debug( "hasChanged(): entity: " + entity.id() );
//        log.debug( "    entity=" + entityLastModified );
//        log.debug( "    store= " + storeLastModified );
//        return storeLastModified != null && !entityLastModified.equals( storeLastModified );
    }

    
    /**
     * 
     */
    private class ModificationTrackEntityStoreUnitOfWork
            implements EntityStoreUnitOfWork {

        private final EntityStoreUnitOfWork     delegate;

        private Module                          module;

        /** 
         * If nobody else holds a hard reference to a particular entityState,
         * then it is probably not changed and it is ok to loose the reference
         * and so not checking concurrent modification on next apply();
         */
        private List<WeakReference<EntityState>> loaded = new ArrayList();


        public ModificationTrackEntityStoreUnitOfWork( EntityStoreUnitOfWork uow, Module module ) {
            this.delegate = uow;
            this.module = module;
        }


        public String identity() {
            return delegate.identity();
        }


        public EntityState newEntityState( EntityReference anIdentity,
                EntityDescriptor entityDescriptor )
                throws EntityStoreException {
            return delegate.newEntityState( anIdentity, entityDescriptor );
        }


        public StateCommitter apply()
                throws EntityStoreException {
            
            // check concurrent modification
            List<EntityReference> changed = new ArrayList();
            final List<EntityState> updated = new ArrayList();
            final long now = System.currentTimeMillis();
            
            final ModelChangeTracker tracker = ModelChangeTracker.instance();

            for (Iterator<WeakReference<EntityState>> it=loaded.iterator(); it.hasNext(); ) {
                EntityState entityState = it.next().get();

                // check WeakReference
                if (entityState == null) {
                    log.debug( "apply(): entityState reclaimed by GC !!!" ); 
                    it.remove();
                    continue;
                }
                
                switch (entityState.status()) {
                    case LOADED: {
                        continue;
                    }
                    case NEW:
                    case UPDATED: {
                        // check concurrent change
                        Long entityLastModified = entityLastModified( entityState );

                        boolean valid = tracker.isConcurrentlyCommitted( entityState.identity().identity(),
                                entityState.entityDescriptor().type(), entityLastModified );
                        
                        if (!valid) {
                            log.debug( "CHANGED: " + entityState.identity() 
                                    + ", lastModified=" + entityLastModified );
                            changed.add( entityState.identity() );
                        }
                        
                        updated.add( entityState );
                        break;
                    }                        
                    case REMOVED: {
                        it.remove();
                        break;
                    }                        
                }
            }
            if (!changed.isEmpty()) {
                throw new ConcurrentEntityStateModificationException( changed );
            }            

            //
            final StateCommitter committer = delegate.apply();

            //
            return new StateCommitter() {

                public void commit() {
                    committer.commit();
                    
                    // updateVersions
                    // XXX check race cond between different uow commits?
                    for (EntityState entityState : updated) {
                        Long lastModified = entityLastModified( entityState );
                        log.debug( "COMMIT: updating global timestamp of entity: " + entityState.identity() 
                                + "\n    lastModified=" + lastModified );
                        
                        Long stored = tracker.storedEntityVersion( 
                                entityState.identity().identity(),
                                entityState.entityDescriptor().type() );
                        
                        if (lastModified == null || lastModified.equals( stored )) {
                            throw new RuntimeException( );
                        }
                    }
                }

                public void cancel() {
                    committer.cancel();
                }
            };
        }


        public void discard() {
            try {
                delegate.discard();
            }
            finally {
                // XXX remove my loaded entity from the global versions table if
                // no other uow holds a reference to this entity
            }
        }


        public EntityState getEntityState( EntityReference identity )
        throws EntityStoreException, EntityNotFoundException {
            
            EntityState result = delegate.getEntityState( identity );
            
            Long storeLastModified = ModelChangeTracker.instance().storedEntityVersion(
                    result.identity().identity(), result.entityDescriptor().type() );
            
            Long entityLastModified = entityLastModified( result );
            if (storeLastModified != null && entityLastModified != null && !storeLastModified.equals( entityLastModified )) {
                throw new EntityStoreException( "Loaded entity has different version as globally recorded version." );
            }
            
            loaded.add( new WeakReference( result ) );
            return result;
        }

        
        private Long entityLastModified( EntityState entityState ) {
            return entityState.lastModified();
        }

    }

}
