/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.mapeditor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.mapeditor.RenderManager.RenderLayerDescriptor;
import org.polymap.openlayers.rap.widget.controls.OverviewMapControl;

/**
 * Overview map support for a {@link MapEditor}. The MapEditor calls the methods
 * of theis class as delegates of its own methods.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class MapEditorOverview {

    static Log log = LogFactory.getLog( MapEditorOverview.class );

    private MapEditor               mapEditor;
    
    private OverviewMapControl      overview;

    
    public MapEditorOverview( MapEditor mapEditor ) {
        this.mapEditor = mapEditor;
    }


    public void dispose() {
        if (overview != null) {
            overview.dispose();
            overview = null;
        }
    }


    public void addLayer( RenderLayerDescriptor descriptor ) {
//        log.debug( "addLayer: layer=" + descriptor );
//        Layer olayer = mapEditor.layers.get( descriptor );
//        
//        if (olayer != null) {
//            if (overview != null) {
//                overview.destroy();
//                overview.dispose();
//            }
//            overview = new OverviewMapControl( mapEditor.olwidget.getMap(), olayer);
//            mapEditor.addControl( overview );
//        }
    }

    
    public void removeLayer( RenderLayerDescriptor descriptor ) {
//        Layer olayer = mapEditor.layers.get( descriptor );
//        if (olayer != null) {
//            overview.removeLayer( olayer );
//        }
    }

}
