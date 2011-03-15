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

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import org.polymap.core.project.IMap;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MapEditorInput
        implements IEditorInput {

    private static Log log = LogFactory.getLog( MapEditorInput.class );

    private IMap                map;
    
    
    public MapEditorInput( IMap map ) {
        super();
        if (map == null) {
            throw new IllegalArgumentException( "map is null!" );
        }
        this.map = map;
    }


    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof MapEditorInput) {
            return ((MapEditorInput)obj).map.equals( map );
        }
        else {
            return false;
        }
    }


    public int hashCode() {
        return map.hashCode();
    }


    public IMap getMap() {
        return map;
    }


    public String getEditorId() {
        return "org.polymap.core.mapeditor.MapEditor";
    }


    public boolean exists() {
        return true;
    }


    public ImageDescriptor getImageDescriptor() {
        return null;
    }


    public String getName() {
        return "MapEditorInput";
    }


    public IPersistableElement getPersistable() {
        return null;
    }


    public String getToolTipText() {
        return "tooltip";
    }


    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom( map.getClass() )) {
            return map;
        }
        return null;
    }

}
