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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.lang.ref.SoftReference;

import javax.measure.unit.Unit;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.data.Messages;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.operation.IFeatureOperation.Status;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.operation.OperationWizard;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.CachedLazyInit;

/**
 * Buffer the geometries of the features.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BufferFeaturesOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( BufferFeaturesOperation.class );

    private static final FilterFactory2     ff = CommonFactoryFinder.getFilterFactory2( null );

    private ILayer                          layer;
    
    private SoftReference<Map<FeatureId,Geometry>> undo;

    
    @Override
    public boolean init( IFeatureOperationContext _context ) {
        super.init( _context );
        
        layer = (ILayer)context.getAdapter( ILayer.class );
        return layer != null;
    }

    
    protected String i18n( String key, Object... args ) {
        return Messages.get( "BufferFeaturesOperation_" + key, args );
    }
    

    /**
     * 
     */
    @Override
    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), 100 );

        final PipelineFeatureSource fs = (PipelineFeatureSource)context.featureSource();
        
        // wizard dialog
        monitor.subTask( i18n( "userInput" ) );
        IUndoableOperation op = context.adapt( IUndoableOperation.class );
        OperationWizard wizard = new OperationWizard( op, context, monitor ) {
            public boolean doPerformFinish() throws Exception {
                return true;
            }
        };
        final BufferInputPage bufferInputPage = new BufferInputPage();
        wizard.addPage( bufferInputPage );
        
        monitor.worked( 10 );

        // open wizard dialog
        if (OperationWizard.openDialog( wizard )) {
            monitor.worked( 10 );
            final IProgressMonitor submon = new SubProgressMonitor( monitor, 80,
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );

            FeatureCollection features = context.features();
            final CoordinateReferenceSystem crs = context.featureSource().getSchema().getCoordinateReferenceSystem();
            final Unit<?> uom = crs.getCoordinateSystem().getAxis(0).getUnit();

            int featuresSize = features.size();
            submon.beginTask( i18n( "submonTitle", featuresSize ), featuresSize );

            final HashMap<FeatureId,Geometry> undoMap = new HashMap<FeatureId,Geometry>();
            
            // buffer geometries
            features.accepts( new FeatureVisitor() {
                int count = 0;
                public void visit( Feature feature ) {
                    if (!submon.isCanceled()) {
                        if (++count % 100 == 0) {
                            submon.worked( 100 );
                            submon.subTask( String.valueOf( count ) );
                        }
                        try {
                            Geometry geom = (Geometry)feature.getDefaultGeometryProperty().getValue();
                            GeometryDescriptor geomDesc = feature.getDefaultGeometryProperty().getDescriptor();
                            
                            undoMap.put( feature.getIdentifier(), geom );
                            
                            if (uom.toString().equalsIgnoreCase( "m" )) {
                                geom = geom.buffer( bufferInputPage.distance, 3 );
                            }
                            else {
                                geom = Geometries.transform( geom, crs, Geometries.crs( "EPSG:3857" ) );
                                geom = geom.buffer( bufferInputPage.distance, bufferInputPage.segments );
                                geom = Geometries.transform( geom, Geometries.crs( "EPSG:3857" ), crs );
                            }
                            fs.modifyFeatures( geomDesc, geom, 
                                    ff.id( Collections.singleton( feature.getIdentifier() ) ) );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                }
            }, new NullProgressListener() );

            undo = new SoftReference( undoMap );
            monitor.done();
            return Status.OK;
        }
        return Status.Cancel;
    }
    
    
    @Override
    public boolean canUndo() {
        return undo != null && undo.get() != null;
    }

    @Override
    public Status undo( IProgressMonitor monitor ) throws Exception {
        // XXX there is a race cond. between canUndo() and here
        Map<FeatureId,Geometry> undoMap = undo.get();
        
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), undoMap.size() );

        FeatureStore fs = (FeatureStore)context.featureSource();
        GeometryDescriptor geomDesc = fs.getSchema().getGeometryDescriptor();

        Transaction tx = new DefaultTransaction( "BufferFeaturesOperation_undo" );
        fs.setTransaction( tx );
        try {
            for (Map.Entry<FeatureId,Geometry> entry : undoMap.entrySet()) {
                if (monitor.isCanceled()) {
                    return Status.Cancel;
                }
                fs.modifyFeatures( geomDesc, entry.getValue(), 
                        ff.id( Collections.singleton( entry.getKey() ) ) );
                monitor.worked( 1 );
            }
            return Status.OK;
        }
        catch (Exception e) {
            tx.rollback();
            throw e;
        }
        finally {
            tx.close();
        }
    }

    @Override
    public boolean canRedo() {
        return true;
    }

    @Override
    public Status redo( IProgressMonitor monitor ) throws Exception {
        return execute( monitor );
    }


    /**
     * A wizard page that gets buffer distance.
     */
    public class BufferInputPage
            extends WizardPage {

        public static final String          ID = "BufferInputPage";

        private boolean                     mandatory = false;
        
        private double                      distance = 10;
        
        private int                         segments = 3;                
        
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

            // buffer distance
            Composite dgroup = contents; //new Group( contents, SWT.NONE );
//            dgroup.setText( i18n( "distanceGroupTitle" ) );
//            dgroup.setLayoutData( SimpleFormData.filled().create() );
//            layout = new FormLayout();
//            layout.spacing = 10;
//            layout.marginWidth = 8;
//            dgroup.setLayout( layout );
            
            Label dl = new Label( dgroup, SWT.WRAP );
            dl.setText( i18n( "distanceDescription" ) );
            dl.setForeground( Graphics.getColor( 0x90, 0x90, 0x90 ) );
            dl.setLayoutData( SimpleFormData.filled().width( 200 ).bottom( -1 ).create() );
            
            // distance
            Label dtl = new Label( dgroup, SWT.NONE );
            dtl.setText( i18n( "distanceLabel" ) );
            dtl.setLayoutData( SimpleFormData.filled().top( dl, 2 ).bottom( -1 ).right( 30 ).create() );
            
            final Spinner dspinner = new Spinner( dgroup, SWT.BORDER );
            dspinner.setMinimum( -10000 );
            dspinner.setMaximum( 10000 );
            dspinner.setSelection( (int)distance );
            dspinner.setLayoutData( SimpleFormData.filled().top( dl ).bottom( -1 ).left( dtl, 8 ).width( 100 ).create() );
            dspinner.addModifyListener( new ModifyListener() {
                public void modifyText( ModifyEvent ev ) {
                    distance = dspinner.getSelection();
                }
            });
            
            // segments per quadrant
            Label ql = new Label( dgroup, SWT.NONE );
            ql.setText( i18n( "quadrantLabel" ) );
            ql.setLayoutData( SimpleFormData.filled().top( dspinner, 2 ).bottom( -1 ).right( 30 ).width( 100 ).create() );
            
            final Spinner qspinner = new Spinner( dgroup, SWT.BORDER );
            qspinner.setMinimum( 0 );
            qspinner.setMaximum( 100 );
            qspinner.setSelection( segments );
            qspinner.setLayoutData( SimpleFormData.filled().top( dspinner ).bottom( -1 ).left( ql, 8 ).create() );
            qspinner.addModifyListener( new ModifyListener() {
                public void modifyText( ModifyEvent ev ) {
                    segments = qspinner.getSelection();
                }
            });

        }

        public boolean isPageComplete() {
            return !mandatory;
        }

        protected String i18n( String key, Object... args ) {
            return Messages.get( "BufferFeaturesOperation_BufferInputPage_" + key, args );
        }

    }
    
}
