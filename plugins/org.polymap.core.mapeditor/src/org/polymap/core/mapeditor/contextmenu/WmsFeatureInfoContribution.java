/* 
 * polymap.org
 * Copyright (C) 2012-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.contextmenu;

import java.util.Collections;

import java.io.InputStream;
import java.io.InputStreamReader;

import net.refractions.udig.catalog.IGeoResource;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetFeatureInfoResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.BlockingReference2;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WmsFeatureInfoContribution
        extends ContributionItem
        implements IContextMenuContribution {

    private static Log log = LogFactory.getLog( WmsFeatureInfoContribution.class );
    
    private ContextMenuSite             site;

    private BlockingReference2<Menu>    menuRef = new BlockingReference2();

    private int                         menuIndex;


    @Override
    public IContextMenuContribution init( ContextMenuSite _site ) {
        this.site = _site;

        setVisible( false );
        
        for (final ILayer layer : site.getMap().getLayers()) {
            if (layer.isVisible()
                    && layer.getGeoResource().canResolve( WebMapServer.class )) {
                
                setVisible( true );

                UIJob job = new UIJob( "GetFeatureInfo: " + layer.getLabel() ) {
                    protected void runWithException( IProgressMonitor monitor ) throws Exception {
                        try {
                            IGeoResource geores = layer.getGeoResource();
                            WebMapServer wms = geores.resolve( WebMapServer.class, null );

                            WMSCapabilities caps = wms.getCapabilities();
                            
                            // GetFeatureInfo supported?
                            if (caps.getRequest().getGetFeatureInfo() != null) {
                                log.info( "Possible formats: " + layer.getLabel() );
                                log.info( "    " + Iterables.toString( caps.getRequest().getGetFeatureInfo().getFormats() ) );
                              
                                // rough check if any feature is covered
                                String plain = issueRequest( geores, "text/plain", false );
                                log.info( "Plain: " + plain );
                                if (plain.length() > 50 ) {
                                    awaitAndFillMenuEntry( layer, geores );
                                }
                            }
                        }
                        catch (Throwable e) {
                            log.warn( "Unable to GetFeatureInfo of: " + layer.getLabel() );
                            log.debug( "", e );
                        }
                    }
                };
                job.schedule();
            }
        }
        return this;
    }


    public String getMenuGroup() {
        return GROUP_HIGH;
    }


    @Override
    public void fill( Menu parent, int index ) {
        this.menuRef.set( parent );
        this.menuIndex = index;
    }

    
    protected void awaitAndFillMenuEntry( ILayer layer, final IGeoResource geores ) {
        final String label = Messages.get( "WmsFeatureInfoContribution_label", layer.getLabel() );
        final Action action = new Action( label ) {
            public void run() {
                String content = issueRequest( geores, "text/html", true );
                openHelpWindow( label, content );
            }            
        };
        action.setImageDescriptor( MapEditorPlugin.imageDescriptor( "icons/etool16/discovery.gif" ) );
        
        // await and actually fill menu
        final Menu menu = menuRef.waitAndGet();
        menu.getDisplay().asyncExec( new Runnable() {
            public void run() {
                if (!menu.isDisposed()) {
                    new ActionContributionItem( action ).fill( menu, menuIndex );
                }
            }
        });
    }
    
    
    protected String issueRequest( IGeoResource geores, String format, boolean handleError ) {
        try {
            WebMapServer wms = geores.resolve( WebMapServer.class, null );
            GetMapRequest mapRequest = wms.createGetMapRequest();
            Layer wmsLayer = geores.resolve( Layer.class, null );
            mapRequest.addLayer( wmsLayer );
            mapRequest.setBBox( site.getMapExtent() );
            mapRequest.setSRS( site.getMap().getCRSCode() );
            mapRequest.setDimensions( site.getMapSize().x, site.getMapSize().y );

            GetFeatureInfoRequest featureInfoRequest = wms.createGetFeatureInfoRequest( mapRequest );
            featureInfoRequest.setFeatureCount( 100 );
            featureInfoRequest.setInfoFormat( format );
            featureInfoRequest.setQueryLayers( Collections.singleton( wmsLayer ) );
            featureInfoRequest.setQueryPoint( site.widgetMousePosition().x, site.widgetMousePosition().y );
            
            log.debug( "URL: " + featureInfoRequest.getFinalURL() );
            
            GetFeatureInfoResponse response = wms.issueRequest( featureInfoRequest );
            InputStream in = response.getInputStream();
            try {
                //log.info( "   contentType:" + response.getContentType() );
                // XXX charset from contentType
                InputStreamReader reader = new InputStreamReader( in, "UTF-8" );
                
                StringBuilder content = new StringBuilder( 4096 );
                for (int c=reader.read(); c!=-1; c=reader.read()) {
                    content.append( (char)c );
                }
                log.debug( content.toString() );
                return content.toString();
            }
            finally {
                IOUtils.closeQuietly( in );
            }
        }
        catch (Exception e) {
            log.warn( "Unable to GetFeatureInfo: " + e.getLocalizedMessage() );
            log.debug( "", e );
            if (handleError) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
            }
            return "";
        }
    }

    protected void openHelpWindow( String title, String html ) {
        Shell parentShell = PolymapWorkbench.getShellToParentOn();
        Shell window = new Shell( parentShell, SWT.CLOSE | SWT.TITLE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL );
        window.setText( title );
        GridLayout layout = new GridLayout( 1, false );
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        window.setLayout( layout );
        window.setSize( 800, 300 );
        //window.setLocation( 100, 50 );
        
        Rectangle bounds = Polymap.getSessionDisplay().getBounds();
        Rectangle rect = window.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        window.setLocation( x, y );
        
        Browser browser = new Browser( window, SWT.NONE );
        browser.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        browser.setText( html );
        window.open();
    }

}
