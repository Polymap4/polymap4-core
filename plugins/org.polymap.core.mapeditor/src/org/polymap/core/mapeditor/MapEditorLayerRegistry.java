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

import java.util.HashSet;
import java.util.Set;

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.project.ILayer;

/**
 * Holds runtime information about the layers and their state of an
 * {@link MapEditor}.
 * 
 * @deprecated Not yet used. currently the state of a layer is signaled by it
 *             properties. 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class MapEditorLayerRegistry {

    public static final String      PROP_LAYER_EDIT = "_edit_";
    public static final String      PROP_LAYER_SELECT = "_select_";
    
    private ListenerList            listeners = new ListenerList( ListenerList.IDENTITY );
    
    private Set<ILayer>             visible = new HashSet();
    
    private ILayer                  edit;
    
    private ILayer                  select;
    
    
    public boolean isVisible( ILayer layer ) {
        return visible.contains( layer );
    }

    public void setVisible( ILayer layer, boolean value ) {
        if (value) {
            visible.add( layer );
        }
        else {
            visible.remove( layer );
        }
    }

    
    public boolean isEdit( ILayer layer ) {
        return layer.equals( edit );
    }

    public void setEdit( ILayer layer, boolean value ) {
        if (value) {
            assert edit == null;
            edit = layer;
        }
        else {
            assert edit.equals( layer );
            edit = null;
        }
        firePropChange( layer, PROP_LAYER_EDIT, value );
    }
    
    
    // events *********************************************
    
    private void firePropChange( ILayer layer, String prop, boolean newValue ) {
        
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        listeners.add( listener );
    }

    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        listeners.remove( listener );
    }
    
}
