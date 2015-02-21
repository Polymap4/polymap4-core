/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;

import org.polymap.core.runtime.IMessages;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.internal.Messages;

import org.polymap.rap.openlayers.base_types.Bounds;
import org.polymap.rap.openlayers.base_types.OpenLayersMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class HomeMapAction
        extends ContributionItem {

    private static Log log = LogFactory.getLog( HomeMapAction.class );

    public static final IMessages   i18n = Messages.forPrefix( "KarteHome" ); //$NON-NLS-1$

    private IPanelSite          site;
    
    private OpenLayersMap       map;

    private MapViewer           viewer;
    
    
    public HomeMapAction( MapViewer viewer ) {
        this.viewer = viewer;
        this.site = viewer.getPanelSite();
        this.map = viewer.getMap();
    }

    
    @Override
    public void fill( Composite parent ) {
        Button btn = site.toolkit().createButton( parent, null, SWT.PUSH );
        btn.setToolTipText( i18n.get( "buttonTip" ) );
        btn.setImage( BatikPlugin.instance().imageForName( "resources/icons/expand.png" ) );
        btn.setEnabled( true );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                Bounds maxExtent = map.getMaxExtent();
                map.zoomToExtent( maxExtent, true );
                map.zoomTo( 2 );
            }
        });
    }
    
}
