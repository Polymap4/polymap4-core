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
package org.polymap.core.data.ui.featureTable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionView
        extends ViewPart 
        implements PropertyChangeListener {

    private static Log log = LogFactory.getLog( FeatureSelectionView.class );
    
    /* Bad but effective way to pass the layer to the view. */
    private static final ThreadLocal<ILayer>    initLayer = new ThreadLocal();
    
    
    /**
     * Makes sure that the view for the layer is open. If the view is already
     * open, then it is activated.
     *
     * @param layer
     * @return The view for the given layer.
     */
    public static FeatureSelectionView open( final ILayer layer ) {
        final FeatureSelectionView[] result = new FeatureSelectionView[1];

        Polymap.getSessionDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//                    ensureMaxViews( page );

                    initLayer.set( layer );
                    result[0] = (FeatureSelectionView)page.showView(
                            FeatureSelectionView.ID, layer.id(), IWorkbenchPage.VIEW_ACTIVATE );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, null, e.getMessage(), e );
                }
                finally {
                    initLayer.remove();
                }
            }
        });
        return result[0];
    }

    public static void close( final ILayer layer ) {
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                    FeatureSelectionView view = (FeatureSelectionView)page.findView( FeatureSelectionView.ID );
                    if (view != null) {
                        page.hideView( view );
                        view.dispose();
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
                }
            }
        });
    }


    // instance *******************************************

    public static final String      ID = "org.polymap.core.data.FeatureSelectionView";

    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

    private ILayer                  layer;
    
    private PipelineFeatureSource   fs;

    private Filter                  filter;

    private FeatureTableViewer      viewer;

    private String                  basePartName;

    private Composite               parent;


    protected void init( ILayer _layer ) {
        try {
            this.layer = _layer;
            this.basePartName = layer.getLabel(); 
            setPartName( basePartName );

            LayerFeatureSelectionManager.forLayer( layer ).addChangeListener( this );
            
            // FIXME check blocking
            this.fs = PipelineFeatureSource.forLayer( layer, false );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "", e );
        }
    }
    
    
    public void dispose() {
        LayerFeatureSelectionManager.forLayer( layer ).removeChangeListener( this );
        
        if (viewer != null) {
            viewer.dispose();
        }
        layer = null;
        fs = null;
        super.dispose();
    }

    
    public ILayer getLayer() {
        return layer;
    }

    
    public PipelineFeatureSource getFeatureStore() {
        return fs;
    }
    
    
    public Filter getFilter() {
        return filter; 
    }

    
    public IFeatureTableElement[] getTableElements() {
        return viewer.getTableElements();
    }
    
    
    public void createPartControl( @SuppressWarnings("hiding") Composite parent ) {
        init( initLayer.get() );
        
        this.parent = parent;
        this.parent.setLayout( new FormLayout() );

        viewer = new FeatureTableViewer( parent, SWT.NONE );
        viewer.getTable().setLayoutData( new SimpleFormData().fill().create() );

        viewer.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                if (ev.getPropertyName().equals( FeatureTableViewer.PROP_CONTENT_SIZE )) {
                    Integer count = (Integer)ev.getNewValue();
                    setPartName( basePartName + " (" + count + ")" );
                }
            }
        });
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                IStructuredSelection sel = (IStructuredSelection)ev.getSelection();
                IFeatureTableElement elm = (IFeatureTableElement)sel.getFirstElement();
                
                LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
                fsm.setHovered( elm.fid() );
            }            
        });
        
        // columns
        assert fs != null : "fs not set. Call init() first.";
        SimpleFeatureType schema = fs.getSchema();
        for (PropertyDescriptor prop : schema.getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                viewer.addColumn( new DefaultFeatureTableColumn( prop ) );
            }
        }

        viewer.getTable().pack( true );

        getSite().setSelectionProvider( viewer );
    }


    protected void loadTable( @SuppressWarnings("hiding") Filter filter ) {
        this.filter = filter;
        viewer.setContent( fs, filter );
    }

    
    /*
     * Feature selection has changed.
     */
    public void propertyChange( final PropertyChangeEvent ev ) {
        LayerFeatureSelectionManager fsm = (LayerFeatureSelectionManager)ev.getSource();
        
        // select
        if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_FILTER )) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    loadTable( (Filter)ev.getNewValue() );
                }
            });
        }
        // hover
        if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_HOVER )) {
//            viewer.removeSelectionChangedListener( tableSelectionListener );
//            viewer.setSelection( new StructuredSelection( fsm.getHovered() ) );
//            viewer.addSelectionChangedListener( tableSelectionListener );
        }

    }

    
    public void setFocus() {
    }
    
}
