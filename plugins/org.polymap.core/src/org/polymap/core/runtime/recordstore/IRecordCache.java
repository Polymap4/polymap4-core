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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IRecordCache {

    public IRecordState get( Object id, RecordLoader loader )
    throws Exception;
    
    public IRecordState put( IRecordState record )
    throws Exception;
    
    public void remove( IRecordState record )
    throws Exception;


    /**
     * Give cache implementations the change to run get/set in
     * {@link IRecordCache#get(Object, RecordLoader)} as atomar operation.
     */
    public interface RecordLoader {
        
        public IRecordState load( Object id ) throws Exception;
        
    }

}
