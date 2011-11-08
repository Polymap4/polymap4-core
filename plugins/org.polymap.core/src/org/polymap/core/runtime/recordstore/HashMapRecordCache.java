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
package org.polymap.core.runtime.recordstore;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Record cache cased on a {@link HashMap} <b>without</b> any synchronization. This
 * can be used only for testing or pure single threaded access. The size of the
 * HashMap is not limited. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class HashMapRecordCache
        implements IRecordCache {

    private static Log log = LogFactory.getLog( HashMapRecordCache.class );

    private Map<Object,IRecordState>    cache = new HashMap( 4096 );
    
    
    public IRecordState get( Object id, RecordLoader loader )
    throws Exception {
        IRecordState result = cache.get( id );
        if (result == null) {
            result = loader.load( id );
            cache.put( id, result );
        }
        return result;
    }


    public IRecordState put( IRecordState record )
    throws Exception {
        //log.info( "cache: size=" + cache.size() + ", id=" + record.id() );
        return cache.put( record.id(), record );
    }


    public void remove( IRecordState record )
    throws Exception {
        cache.remove( record.id() );
    }
    
}
