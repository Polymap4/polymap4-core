/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.operations.feature;

import java.util.HashSet;
import java.util.Set;

import javax.measure.unit.Unit;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.data.Messages;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.operations.ChooseLayerPage;
import org.polymap.core.data.ui.featureselection.FeatureSelectionView;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.geohub.LayerFeatureSelectionOperation;
import org.polymap.core.operation.OperationWizard;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.util.SimpleFormData;

/**
 * Intersect the geometries of the features with features of another layer. The
 * result is send to the {@link LayerFeatureSelectionManager} of the layer.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class IntersectFeaturesOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( IntersectFeaturesOperation.class );

    private static final FilterFactory2     ff = CommonFactoryFinder.getFilterFactory2( null );

    private ILayer                          layer;
    
    private LayerFeatureSelectionOperation  delegate;
    
    
    @Override
    public boolean init( IFeatureOperationContext _context ) {
        super.init( _context );
        
        layer = (ILayer)context.getAdapter( ILayer.class );
        return layer != null;
    }


    /**
     * 
     */
    @Override
    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), 100 );

        PipelineFeatureSource fs = (PipelineFeatureSource)context.featureSource();
        
        // create wizard
        monitor.subTask( i18n( "userInput" ) );
        IUndoableOperation op = context.adapt( IUndoableOperation.class );
        OperationWizard wizard = new OperationWizard( op, context, monitor ) {
            public boolean doPerformFinish() throws Exception {
                return true;
            }
        };
        // ChooseLayerPage
        ChooseLayerPage chooseLayerPage = new ChooseLayerPage(
                i18n( "ChooseLayerPage_title" ), i18n( "ChooseLayerPage_description" ), true ) {
            /** Check if result layer has features. */
            public boolean isPageComplete() {
                if (super.isPageComplete()) {
                    try {
                        PipelineFeatureSource rfs = PipelineFeatureSource.forLayer( getResult(), false );
                        return rfs != null && rfs.getPipeline().length() > 0;
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }            
        };
        wizard.addPage( chooseLayerPage );
        // BufferInputPage
        BufferInputPage bufferInputPage = new BufferInputPage();
        wizard.addPage( bufferInputPage );
        
        monitor.worked( 10 );

        // open wizard dialog
        if (OperationWizard.openDialog( wizard )) {
            monitor.worked( 10 );
            final IProgressMonitor submon = new SubProgressMonitor( monitor, 80,
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );

            FeatureCollection features = context.features();
            CoordinateReferenceSystem crs = context.featureSource().getSchema().getCoordinateReferenceSystem();

            int featuresSize = features.size();
            submon.beginTask( i18n( "submonTitle", featuresSize ), featuresSize );

            // query layer
            ILayer queryLayer = chooseLayerPage.getResult();
            final PipelineFeatureSource queryFs = PipelineFeatureSource.forLayer( queryLayer, true );
            if (queryFs == null) {
                throw new Exception( "Not a feature layer: " + queryLayer.getLabel() );
            }
            // intersection mode
            final IntersectionMode mode = bufferInputPage.mode;
            
            // intersect geometries
            final Set<FeatureId> fids = new HashSet();
            features.accepts( new FeatureVisitor() {
                int count = 0;
                public void visit( Feature feature ) {
                    if (!submon.isCanceled()) {
                        if (++count % 100 == 0) {
                            submon.worked( 100 );
                            submon.subTask( String.valueOf( count ) );
                        }
                        try {
                            FeatureId fid = mode.perform( feature, queryFs );
                            if (fid != null) {
                                fids.add( fid );
                            }
                        }
                        catch (Exception e) {
                            log.warn( "", e );
                            throw new RuntimeException( e );
                        }
                    }
                }
            }, new NullProgressListener() );

            // change selection
            if (!submon.isCanceled()) {
                // XXX find indirect way to open view
                FeatureSelectionView.open( layer );
                
                delegate = new LayerFeatureSelectionOperation();
                delegate.init( layer, ff.id( fids ), null, null );
                delegate.execute( monitor, context );
            }
            monitor.done();
            return Status.OK;
        }
        return Status.Cancel;
    }
    
    
    @Override
    public boolean canUndo() {
        return delegate != null && delegate.canUndo();
    }


    @Override
    public Status undo( IProgressMonitor monitor )
    throws Exception {
        IStatus result = delegate.undo( monitor, context );
        return result == org.eclipse.core.runtime.Status.OK_STATUS ? Status.OK : Status.Error;
    }


    @Override
    public boolean canRedo() {
        return delegate != null && delegate.canRedo();
    }


    @Override
    public Status redo( IProgressMonitor monitor )
    throws Exception {
        IStatus result = delegate.redo( monitor, context );
        return result == org.eclipse.core.runtime.Status.OK_STATUS ? Status.OK : Status.Error;
    }


    protected String i18n( String key, Object... args ) {
        return Messages.get( "IntersectFeaturesOperation_" + key, args );
    }
    

    /**
     * 
     */
    protected abstract class IntersectionMode {
        
        /** The buffer distance in meters. */
        protected double                    distance;
        
        private Unit<?>                     uom;
        
        /** The CRS of the paretion features. */
        private CoordinateReferenceSystem   crs;

        private CoordinateReferenceSystem   queryCrs;

        protected Name                      queryGeomName;
        
        
        public IntersectionMode( double distance ) {
            this.distance = distance;
        }

        public abstract FeatureId perform( Feature feature, FeatureSource queryFs ) 
        throws Exception;

        protected Geometry transformed( Feature feature, FeatureSource queryFs )
        throws Exception {
            if (crs == null) {
                crs = feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();
                queryCrs = queryFs.getSchema().getCoordinateReferenceSystem();
                queryGeomName = queryFs.getSchema().getGeometryDescriptor().getName();
            }

            Geometry geom = (Geometry)feature.getDefaultGeometryProperty().getValue();
            
            if (distance != 0) {
                if (uom == null) {
                    uom = crs.getCoordinateSystem().getAxis(0).getUnit();
                    log.info( "UOM: " + uom.toString() );
                }

                if (uom.toString().equalsIgnoreCase( "m" )) {
                    geom = geom.buffer( distance, 3 );
                    geom = Geometries.transform( geom, crs, queryCrs );
                }
                else {
                    geom = Geometries.transform( geom, crs, Geometries.crs( "EPSG:3857" ) );
                    geom = geom.buffer( distance, 3 );
                    geom = Geometries.transform( geom, Geometries.crs( "EPSG:3857" ), queryCrs );
                }                        
            }
            else {
                geom = Geometries.transform( geom, crs, queryCrs );
            }
            return geom;
        }
    }

    
    /**
     * Implements 'intersects' mode based on <code>ff.intersects()</code>.
     */
    protected class Intersects
            extends IntersectionMode {

        public Intersects( double distance ) {
            super( distance );
        }

        public FeatureId perform( Feature feature, FeatureSource queryFs ) 
        throws Exception {
            Geometry geom = transformed( feature, queryFs );
            
            ReferencedEnvelope bbox = JTS.toEnvelope( geom );
            FeatureCollection intersected = queryFs.getFeatures( ff.and(
                    ff.bbox( ff.property( queryGeomName ), bbox ),
                    ff.intersects( ff.property( queryGeomName ), ff.literal( geom ) ) ) );
            
            return !intersected.isEmpty() ? feature.getIdentifier() : null;
        }
    }

    /**
     * Implements <code>ff.crosses()</code> mode.
     */
    protected class Crosses
            extends IntersectionMode {

        public Crosses( double distance ) {
            super( distance );
        }

        public FeatureId perform( Feature feature, FeatureSource queryFs ) 
        throws Exception {
            Geometry geom = transformed( feature, queryFs );
            
            FeatureCollection intersected = queryFs.getFeatures(
                    ff.crosses( ff.property( queryGeomName ), ff.literal( geom ) ) );
            
            return !intersected.isEmpty() ? feature.getIdentifier() : null;
        }
    }

    /**
     * Implements 'intersects' mode based on <code>ff.intersects()</code>.
     */
    protected class DWithin
            extends IntersectionMode {

        public DWithin( double distance ) {
            super( distance );
        }

        public FeatureId perform( Feature feature, FeatureSource queryFs ) 
        throws Exception {
            Geometry geom = transformed( feature, queryFs );
            
            // get the unit of measurement
            CoordinateReferenceSystem crs = queryFs.getSchema().getCoordinateReferenceSystem();
            Unit<?> uom = crs.getCoordinateSystem().getAxis(0).getUnit();
            log.info( "UOM: " + uom.toString() );
            
            FeatureCollection intersected = queryFs.getFeatures(
                    ff.dwithin( ff.property( queryGeomName ), ff.literal( geom ), distance, "m" /*uom.toString()*/ ) );
            
            return !intersected.isEmpty() ? feature.getIdentifier() : null;
        }
    }

    /**
     * Implements <code>ff.crosses()</code> mode.
     */
    protected class Touches
            extends IntersectionMode {

        public Touches( double distance ) {
            super( distance );
        }

        public FeatureId perform( Feature feature, FeatureSource queryFs ) 
        throws Exception {
            Geometry geom = transformed( feature, queryFs );
            
            FeatureCollection intersected = queryFs.getFeatures(
                    ff.touches( ff.property( queryGeomName ), ff.literal( geom ) ) );
            
            return !intersected.isEmpty() ? feature.getIdentifier() : null;
        }
    }

    /**
     * Implements <code>ff.overlaps()</code> mode.
     */
    protected class Overlaps
            extends IntersectionMode {

        public Overlaps( double distance ) {
            super( distance );
        }

        public FeatureId perform( Feature feature, FeatureSource queryFs ) 
        throws Exception {
            Geometry geom = transformed( feature, queryFs );
            
            FeatureCollection intersected = queryFs.getFeatures(
                    ff.overlaps( ff.property( queryGeomName ), ff.literal( geom ) ) );
            
            return !intersected.isEmpty() ? feature.getIdentifier() : null;
        }
    }

    /**
     * Implements <code>ff.contains()</code> mode.
     */
    protected class Contains
            extends IntersectionMode {

        public Contains( double distance ) {
            super( distance );
        }

        public FeatureId perform( Feature feature, FeatureSource queryFs ) 
        throws Exception {
            Geometry geom = transformed( feature, queryFs );
            
            FeatureCollection intersected = queryFs.getFeatures(
                    ff.contains( ff.property( queryGeomName ), ff.literal( geom ) ) );
            
            return !intersected.isEmpty() ? feature.getIdentifier() : null;
        }
    }




    /**
     * A wizard page that gets buffer distance and intersection mode from the user.
     */
    public class BufferInputPage
            extends WizardPage {

        public static final String          ID = "BufferInputPage";

        private boolean                     mandatory = false;
        
        protected IntersectionMode          mode = new Intersects( 0 );

        
        public BufferInputPage() {
            super( ID );
            setTitle( i18n( "title" ) );
            setDescription( i18n( "description" ) );
        }


        public void createControl( Composite parent ) {
            Composite contents = new Composite( parent, SWT.NONE );
            FormLayout layout = new FormLayout();
            layout.spacing = 10;
            layout.marginWidth = 8;
            contents.setLayout( layout );
            setControl( contents );

            // intersection mode
            Group igroup = new Group( contents, SWT.NONE );
            igroup.setText( i18n( "modeGroupTitle" ) );
            igroup.setLayoutData( SimpleFormData.filled().bottom( 55 ).create() );
            layout = new FormLayout();
            layout.spacing = 0;
            layout.marginWidth = 8;
            igroup.setLayout( layout );
            
            Button intersectsBtn = new Button( igroup, SWT.RADIO );
            intersectsBtn.setText( i18n( "intersects" ) );
            intersectsBtn.setLayoutData( SimpleFormData.filled().bottom( -1 ).create() );
            intersectsBtn.setSelection( true );
            intersectsBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    mode = new Intersects( mode.distance );
                }
            });

//            Button dwithinBtn = new Button( igroup, SWT.RADIO );
//            dwithinBtn.setText( i18n( "dwithin" ) );
//            dwithinBtn.setLayoutData( SimpleFormData.filled().top( intersectsBtn ).bottom( -1 ).create() );
//            dwithinBtn.addSelectionListener( new SelectionAdapter() {
//                public void widgetSelected( SelectionEvent ev ) {
//                    mode = new DWithin( mode.distance );
//                }
//            });

            Button touchesBtn = new Button( igroup, SWT.RADIO );
            touchesBtn.setText( i18n( "touches" ) );
            touchesBtn.setLayoutData( SimpleFormData.filled().top( intersectsBtn ).bottom( -1 ).create() );
            touchesBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    mode = new Touches( mode.distance );
                }
            });

            Button overlapsBtn = new Button( igroup, SWT.RADIO );
            overlapsBtn.setText( i18n( "overlaps" ) );
            overlapsBtn.setLayoutData( SimpleFormData.filled().top( touchesBtn ).bottom( -1 ).create() );
            overlapsBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    mode = new Overlaps( mode.distance );
                }
            });

            Button containsBtn = new Button( igroup, SWT.RADIO );
            containsBtn.setText( i18n( "contains" ) );
            containsBtn.setLayoutData( SimpleFormData.filled().top( overlapsBtn ).bottom( -1 ).create() );
            containsBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    mode = new Contains( mode.distance );
                }
            });

            Button crossesBtn = new Button( igroup, SWT.RADIO );
            crossesBtn.setText( i18n( "crosses" ) );
            crossesBtn.setLayoutData( SimpleFormData.filled().top( containsBtn ).bottom( -1 ).create() );
            crossesBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    mode = new Crosses( mode.distance );
                }
            });

            // buffer distance
            Group dgroup = new Group( contents, SWT.NONE );
            dgroup.setText( i18n( "distanceGroupTitle" ) );
            dgroup.setLayoutData( SimpleFormData.filled().top( igroup ).create() );
            layout = new FormLayout();
            layout.spacing = 10;
            layout.marginWidth = 8;
            dgroup.setLayout( layout );
            
            Label dl = new Label( dgroup, SWT.WRAP );
            dl.setText( i18n( "distanceDescription" ) );
            dl.setForeground( Graphics.getColor( 0x90, 0x90, 0x90 ) );
            dl.setLayoutData( SimpleFormData.filled().width( 300 ).bottom( -1 ).create() );
            
            Label dtl = new Label( dgroup, SWT.NONE );
            dtl.setText( i18n( "distanceLabel" ) );
            dtl.setLayoutData( SimpleFormData.filled().top( dl, 2 ).bottom( -1 ).right( -1 ).create() );
            
            final Spinner dspinner = new Spinner( dgroup, SWT.BORDER );
            dspinner.setMaximum( 10000 );
            dspinner.setMinimum( -10000 );
            dspinner.setSelection( (int)mode.distance );
            dspinner.setLayoutData( SimpleFormData.filled().top( dl ).bottom( -1 ).left( dtl, 8 ).create() );
            dspinner.addModifyListener( new ModifyListener() {
                public void modifyText( ModifyEvent ev ) {
                    mode.distance = dspinner.getSelection();
                }
            });
        }

        public boolean isPageComplete() {
            return !mandatory;
        }

        protected String i18n( String key, Object... args ) {
            return Messages.get( "IntersectFeaturesOperation_BufferInputPage_" + key, args );
        }

    }

}
