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
package org.polymap.rhei.data.entitystore.lucene;

import org.apache.lucene.document.Document;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entitystore.EntityStore;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * EntityStore backed by a Lucene index and the
 * {@link org.polymap.core.runtime.recordstore} API.
 * <p/>
 * Each entity is stored as a {@link Document}/{@link IRecordState} with different
 * field mapping to the fields of the entity.
 * 
 * @see ServiceComposite
 * @see org.qi4j.api.configuration.Configuration
 */
@Mixins( 
        LuceneEntityStoreMixin.class 
)
public interface LuceneEntityStoreService
        extends LuceneSearcher, EntityStore, ServiceComposite/*, EntityStateVersions*/, Activatable {    
}
