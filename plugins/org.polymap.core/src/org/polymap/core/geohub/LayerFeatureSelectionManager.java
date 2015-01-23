/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.geohub;

import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.rap.rwt.SessionSingletonBase;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

/**
 * Provides a central manager to hold and manage the feature selected in a
 * layer.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerFeatureSelectionManager {

    private static Log log = LogFactory.getLog( LayerFeatureSelectionManager.class );
    
    private static final FilterFactory  ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

    public static final String          PROP_FILTER = "filter";
    
    public static final String          PROP_HOVER = "hover";
    
    public static final String          PROP_MODE = "mode";
    
    /**
     * The selection modes
     */
    public enum MODE { REPLACE, ADD, DIFFERENCE, INTERSECT };

    
    private static FeatureCollectionFactory    fcf;
    
    public static void setFeatureCollectionFactory( FeatureCollectionFactory _fcf ) {
        assert fcf == null;
        fcf = _fcf;
    }
    
    
    private static class SessionRegistry
            extends SessionSingletonBase {
        
        private Map<Object,LayerFeatureSelectionManager>   managers = new HashMap();
        
    }


    /**
     * Returns the one and only selection manager in the current session for the
     * given layer.
     */
    public static LayerFeatureSelectionManager forLayer( Object layer ) {
        SessionRegistry registry = (SessionRegistry)SessionRegistry.getInstance( SessionRegistry.class );
        LayerFeatureSelectionManager result = registry.managers.get( layer );
        if (result == null) {
            result = new LayerFeatureSelectionManager( layer );
            registry.managers.put( layer, result );
        }
        return result;
    }

    
    // instance *******************************************

    private Object                  layer;
    
    private MODE                    mode = MODE.REPLACE;
    
    private Filter                  filter;
    
    private FeatureCollection       fc;
    
    private String                  hovered;
    
    
    protected LayerFeatureSelectionManager( Object layer ) {
        this.layer = layer;
    }

    
    public void dispose() {
    }
    
    public MODE getMode() {
        return mode;
    }

    public void setMode( MODE mode ) {
        MODE old = this.mode;
        this.mode = mode;
        fireEvent( PROP_MODE, old, mode, null );
    }
    
    
    public String getHovered() {
        return hovered;
    }
    
    public void setHovered( String fid ) {
        String old = this.hovered;
        this.hovered = fid;
        fireEvent( PROP_HOVER, old, fid, null );
    }


    public Filter getFilter() {
        return filter != null ? filter : Filter.EXCLUDE;
    }
    
    public void changeSelection( Filter newFilter ) {
        changeSelection( newFilter, mode, null );
    }
    
    /**
     *
     * @param newFilter
     * @param modeHint Null signals that the current mode is to be used.
     * @param ommit The listner to ommit, or null.
     */
    public void changeSelection( Filter newFilter, MODE modeHint, PropertyChangeListener ommit ) {
        assert newFilter != null;
        Filter old = filter;

        modeHint = modeHint != null ? modeHint : mode;
        switch (modeHint) {
            case REPLACE : 
                filter = newFilter;
                break;
            case ADD :
                filter = filter != null ? ff.or( filter, newFilter ) : newFilter;
                break;
            case INTERSECT :
                filter = filter != null ? ff.and( filter, newFilter ) : newFilter;
                break;
            case DIFFERENCE :
                filter = filter != null ? ff.and( filter, ff.not( newFilter ) ) : newFilter;
                break;
            default:
                throw new UnsupportedOperationException( "Operation not supported yet: " + modeHint );
        }
        fc = null;
        fireEvent( PROP_FILTER, old, getFilter(), ommit );
    }

    
    public void clearSelection() {
        Filter old = filter;
        filter = null;
        fireEvent( PROP_FILTER, old, getFilter(), null );
    }


    /**
     * Returns the {@link FeatureCollection} for the current selection filter. The
     * same instance is returned by subsequent calls if filter has not changed
     * meanwhile.
     */
    public FeatureCollection getFeatureCollection() {
        assert fcf != null;
        if (fc == null) {
            fc = fcf.newFeatureCollection( layer, getFilter() );
        }
        return fc;
    }
    
    // events
    
    protected void fireEvent( String prop, Object oldValue, Object newValue, PropertyChangeListener ommit ) {
        PropertyChangeEvent ev = new FeatureSelectionEvent( this, prop, oldValue, newValue );
        EventManager.instance().publish( ev, ommit );
    }
    
    public boolean addSelectionChangeListener( final PropertyChangeListener l ) {
        EventManager.instance().subscribe( l, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent ev ) {
                return ev instanceof FeatureSelectionEvent 
                        && ev.getSource() == LayerFeatureSelectionManager.this;
            }
        });
        return true;
    }

    public boolean removeSelectionChangeListener( PropertyChangeListener l ) {
        return EventManager.instance().unsubscribe( l );
    }

}
