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
package org.polymap.core.data.image.cache304;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.RecordModel;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CachedTile
        extends RecordModel {

    private static Log log = LogFactory.getLog( CachedTile.class );
    
    public static final CachedTile      TYPE = type( CachedTile.class );
    

    public CachedTile( IRecordState record ) {
        super( record );
    }

    public Property<Long>       lastModified = new Property<Long>( "lastModified" );
    
    public Property<Long>       lastAccessed = new Property<Long>( "lastAccessed" );
    
    public Property<Integer>    width = new Property<Integer>( "width" );
    
    public Property<Integer>    height = new Property<Integer>( "height" );
    
    public Property<String>     style = new Property<String>( "style" );
    
    public Property<String>     layerId = new Property<String>( "layerid" );
    
    public Property<Double>     minx = new Property<Double>( "minx" );
    
    public Property<Double>     miny = new Property<Double>( "miny" );
    
    public Property<Double>     maxx = new Property<Double>( "maxx" );
    
    public Property<Double>     maxy = new Property<Double>( "maxy" );
    
    public Property<byte[]>     data = new Property<byte[]>( "data" );
    
}
