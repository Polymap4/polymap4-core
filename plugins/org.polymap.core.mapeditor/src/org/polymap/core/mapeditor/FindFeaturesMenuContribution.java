/* 
 * polymap.org
 * Copyright (C) 2012-2014, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor;

import java.util.List;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Envelope;

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.runtime.BlockingReference2;
import org.polymap.core.runtime.UIJob;

import org.polymap.rap.openlayers.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FindFeaturesMenuContribution
        extends ContributionItem
        implements IContextMenuContribution {

    private static Log log = LogFactory.getLog( FindFeaturesMenuContribution.class );
    
    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );
    
    private ContextMenuSite             site;

    private BlockingReference2<Menu>    menuRef = new BlockingReference2();

    private int                         menuIndex;
    
    
    @Override
    public boolean init( ContextMenuSite _site ) {
        this.site = _site;
        setVisible( false );
        
        List<WMSLayer> mapLayers = site.getMapViewer().getLayers();
        for (WMSLayer mapLayer : mapLayers) {
            if (site.getMapViewer().isVisible( mapLayer )) {
                ILayer layer = ProjectRepository.instance().visit( new LayerFinder( mapLayer.getName(), mapLayer.getWmsLayers() ) );
                if (layer != null) {
                    setVisible( true );
                    new FindFeaturesJob( "Find features: " + layer.getLabel(), site.boundingBox(), layer ).schedule();                    
                }
            }
        }
        return true;
    }

    
    @Override
    public String getMenuGroup() {
        return GROUP_HIGH;
    }


    @Override
    public void fill( final Menu parent, final int index ) {
        menuRef.set( parent );
        menuIndex = index;
    }

    
    protected void awaitAndFillMenu( final ILayer layer, final PipelineFeatureSource fs, final Feature feature ) {
        log.info( "Feature: " + feature );
        final Action action = new Action( createLabel( feature, layer ) ) {
            public void run() {
                onMenuOpen( fs, feature, layer );
            }            
        };
        //action.setImageDescriptor( RheiFormPlugin.getDefault().imageDescriptor( "icons/etool16/open_form_editor.gif" ) );
        
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

    
    protected abstract void onMenuOpen( FeatureStore fs, Feature feature, ILayer layer );
    

    /**
     * Creates the menu label for the given feature. Override this method in order to
     * adjust behaviour.
     * 
     * @param feature
     * @param layer
     * @return
     */
    protected String createLabel( Feature feature, ILayer layer ) {
        // last resort: fid
        String featureLabel = feature.getIdentifier().getID();
        
        for (Property prop : feature.getProperties()) {
            String propName = prop.getName().getLocalPart();
            
            if (propName.equalsIgnoreCase( "name" )
                    && prop.getValue() != null) {
                featureLabel = prop.getValue().toString();
                break;
            }
            else if ((propName.contains( "name" ) || propName.contains( "Name" ))
                    && prop.getValue() != null) {
                featureLabel = prop.getValue().toString();
                break;
            }
            else if ((propName.equalsIgnoreCase( "number" ) || propName.equalsIgnoreCase( "nummer" ))
                    && prop.getValue() != null) {
                featureLabel = prop.getValue().toString();
            }
        }
        return Messages.get( "OpenFormMapContextMenu_label", 
                StringUtils.abbreviate( featureLabel, 0, 35 ), 
                layer.getLabel() );
    }


    /**
     * 
     */
    class FindFeaturesJob
            extends UIJob {
        
        private PipelineFeatureSource   fs;
        
        private ILayer                  layer;
    
        private ReferencedEnvelope      bbox;
        
        
        public FindFeaturesJob( String name, ReferencedEnvelope bbox, PipelineFeatureSource fs ) {
            super( name );
            this.fs = fs;
            this.bbox = bbox;
        }
    
    
        public FindFeaturesJob( String name, ReferencedEnvelope bbox, ILayer layer ) {
            super( name );
            this.layer = layer;
            this.bbox = bbox;
        }
    
    
        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            FeatureIterator it = null;
            try {
                if (fs == null) {
                    fs = PipelineFeatureSource.forLayer( layer, false );
                }
                if (fs != null) {
                    CoordinateReferenceSystem crs = layer.getGeoResource().getInfo( monitor ).getCRS();
                    ReferencedEnvelope transformed = bbox.transform( crs, true );
                    String propname = "";
                    Filter filter = ff.intersects( 
                            ff.property( propname ),
                            ff.literal( JTS.toGeometry( (Envelope)transformed ) ) );
    
                    FeatureCollection features = fs.getFeatures( new DefaultQuery( 
                            fs.getSchema().getTypeName(), filter, 10, null, null ) );
    
                    for (it = features.features(); it.hasNext(); ) {
                        awaitAndFillMenu( layer, fs, it.next() );
                    }
                }
            }
            catch (Exception e) {
                log.warn( "Filtering covered features failed: ", e );
            }
            finally {
                if (it != null) {it.close();}
            }
        }
    }

}
