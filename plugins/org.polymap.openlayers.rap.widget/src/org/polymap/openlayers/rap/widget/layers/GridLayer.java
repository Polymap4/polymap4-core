/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.polymap.openlayers.rap.widget.layers;

import org.polymap.openlayers.rap.widget.base_types.Size;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class GridLayer
        extends HTTPRequestLayer {

    /**
     * Setting singletile to true moves the layer into single-tile mode,
     * meaning that one tile will be loaded. The tile's size will be determined
     * by the 'ratio'
     * property. When the tile is dragged such that it does not cover the
     * entire viewport, it is reloaded.
     * 
     * @param Boolean singletile
     * 
     */

    public void setSingleTile( boolean singletile ) {
        setObjAttr( "singleTile", singletile );
    }


    public void setTileSize( Size size ) {
        setObjAttr( "tileSize", size );
        
    }


    /**
     * Used only when in single-tile mode, this specifies the
     * ratio of the size of the single tile to the size of the map.
     * 
     * @param Double ratio - the ratio used when in single tile mode ( default
     *        1.5 )
     */

    public void setRatio( Double ratio ) {
        setObjAttr( "ratio", ratio );
    }


    /**
     * Used only when in gridded mode, this specifies the number of
     * extra rows and colums of tiles on each side which will
     * surround the minimum grid tiles to cover the map.
     * 
     * @param  Int buffer - the buffer size ( default 2 )
     * 
     */

    public void setBuffer( int buffer ) {
        setObjAttr( "buffer", buffer );
    }

}
