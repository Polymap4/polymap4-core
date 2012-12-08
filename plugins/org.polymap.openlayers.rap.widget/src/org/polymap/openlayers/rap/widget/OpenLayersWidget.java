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
 */
package org.polymap.openlayers.rap.widget;

import org.eclipse.rwt.lifecycle.IWidgetLifeCycleAdapter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.polymap.openlayers.rap.widget.base.OpenLayersSessionHandler;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.internal.openlayerswidgetkit.OpenLayersWidgetLCA;

/**
 * 
 * Composite part for the OpenLayers RAP Widget
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OpenLayersWidget extends Composite {

	public boolean lib_init_done=false;
	
	/** reference to the map object - every widged has exactly one Map **/
	private OpenLayersMap map;

	/** default external openlayers lib location **/
	public String js_location = "http://www.openlayers.org/api/OpenLayers.js";

	public Object getAdapter( Class adapter ) {
	    Object result;
	    if( adapter == IWidgetLifeCycleAdapter.class ) {
	        result = new OpenLayersWidgetLCA();
	    } else {
	        result = super.getAdapter( adapter );
	    }
	    return result;
	}
	
	public OpenLayersWidget(Composite parent, int style) {
		super(parent, style);
		prepare();
		//hookContextMenu();
	}

	public OpenLayersWidget(Composite parent, int style, String lib_location) {
		this(parent, style);
		js_location = lib_location;
	}

//	protected void hookContextMenu() {
//        final MenuManager contextMenu = new MenuManager();
//        contextMenu.setRemoveAllWhenShown( true );
//        contextMenu.addMenuListener( new IMenuListener() {
//            public void menuAboutToShow( IMenuManager manager ) {
//                contextMenu.add( new Action( "Text" ) {
//                    public void run() {
//                    }
//                });
//            }
//        } );
//        Menu menu = contextMenu.createContextMenu( this );
//        setMenu( menu );
//	}
	
	public OpenLayersMap getMap() {
	    if (map == null) {
	       map = new OpenLayersMap(this);    
	    }
		return map;
	}

	/** 
	 * create the Map with non default Projection
	 * createMapmust be called before getMap()
	 * 
	 **/
	public void createMap(Projection projection,Projection display_projection,String units,Bounds maxExtent,float maxResolution) {
	    map = new OpenLayersMap(this,projection,display_projection,units, maxExtent,maxResolution);    
	}
	
	public void prepare() {
		OpenLayersSessionHandler.getInstance().setWidget(this);
	
	}

	public String getJSLocation() {
		return js_location;
	}

	// no layout
	public void setLayout(final Layout layout) {
	}

	
}
