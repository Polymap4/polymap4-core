/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam. All rights reserved.
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

import java.util.List;
import java.util.Properties;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.data.Messages;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.buffer.FeatureBufferProcessor;
import org.polymap.core.data.feature.typeeditor.AttributeMapping;
import org.polymap.core.data.feature.typeeditor.FeatureTypeEditorProcessor;
import org.polymap.core.data.feature.typeeditor.FeatureTypeEditorProcessorConfig;
import org.polymap.core.data.feature.typeeditor.FeatureTypeMapping;
import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operations.ChooseLayerPage;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.ui.featuretypeeditor.FeatureTypeEditor;
import org.polymap.core.data.ui.featuretypeeditor.ValueViewerColumn;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.data.util.ProgressListenerAdaptor;
import org.polymap.core.data.util.RetypingFeatureCollection;
import org.polymap.core.operation.OperationWizard;
import org.polymap.core.operation.OperationWizardPage;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.WeakListener;

/**
 * Copy features into another layer. The type of the features and the CRS might be
 * modified while copying.
 * 
 * @see NewFeatureOperation
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class CopyFeaturesOperation2
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( CopyFeaturesOperation2.class );

    private static IMessages            i18n = Messages.forClass( CopyFeaturesOperation2.class );
    
    private PipelineFeatureSource       source;

    private ILayer                      dest;

    private FeatureCollection           features;

    private PipelineFeatureSource       destFs;

    private List<FeatureId>             createdFeatureIds;


    /** The copied features. */    
    public FeatureCollection getFeatures() {
        return features;
    }
    
    public PipelineFeatureSource getDestFs() {
        return destFs;
    }
    
    public List<FeatureId> getCreatedFeatureIds() {
        return createdFeatureIds;
    }

    
    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), 10 );

        source = (PipelineFeatureSource)context.featureSource();
        
        if (!(source.getSchema() instanceof SimpleFeatureType)) {
            throw new Exception( i18n.get( "notSimpleType" ) );
        }

        // open wizard dialog
        monitor.subTask( "Eingaben vom Nutzer..." );
        IUndoableOperation op = context.adapt( IUndoableOperation.class );
        final OperationWizard wizard = new OperationWizard( op, context, monitor ) {
            public boolean doPerformFinish() throws Exception {
                ((FeatureEditorPage2)getPage( FeatureEditorPage2.ID )).performFinish();
                return true;
            }
        };
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                wizard.getShell().setMinimumSize( 600, 600 );
                wizard.getShell().layout( true );
            }
        });

        final ChooseLayerPage chooseLayerPage = new ChooseLayerPage(
                i18n.get( "ChooseLayerPage_title" ),
                i18n.get( "ChooseLayerPage_description" ),
                true );
        ILayer layer = context.adapt( ILayer.class );
        chooseLayerPage.preset( layer );
        wizard.addPage( chooseLayerPage );
        final FeatureEditorPage2 featureEditorPage = new FeatureEditorPage2();
        wizard.addPage( featureEditorPage );
        DirectCopyPage directCopyPage = new DirectCopyPage();
        wizard.addPage( directCopyPage );

        // get/set chosen layer
        wizard.addPageChangedListener( new IPageChangedListener() {
            public void pageChanged( PageChangedEvent ev ) {
                log.info( "Page: " + ev.getSelectedPage() );
                if (featureEditorPage == ev.getSelectedPage()) {
                    dest = chooseLayerPage.getResult();
                }
            }
        });
        monitor.worked( 1 );

        // copy features
        if (OperationWizard.openDialog( wizard )) {
            final IProgressMonitor copyMonitor = new SubProgressMonitor( monitor, 8,
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );

            destFs = PipelineFeatureSource.forLayer( dest, true );
            features = context.features();
            int featuresSize = features.size();

            copyMonitor.beginTask( i18n.get( "taskTitle", featuresSize ), featuresSize );

            // direct copy?
            if (directCopyPage.isDirectCopy()) {
                Pipeline pipe = destFs.getPipeline();
                Iterables.removeIf( pipe, Predicates.instanceOf( FeatureBufferProcessor.class ) );
            }

            // tranform schema
            features = featureEditorPage.retyped( features );            
            
            // transform CRS
            SimpleFeatureType destSchema = destFs.getSchema();
            final CoordinateReferenceSystem destCrs = destSchema.getCoordinateReferenceSystem();
            SimpleFeatureType sourceSchema = (SimpleFeatureType)features.getSchema();
            // XXX do not use sourceSchema here as featureEditorPage ff. has set CRS to destFs schema CRS
            CoordinateReferenceSystem sourceCrs = sourceSchema.getCoordinateReferenceSystem();
            
            if (destCrs != null && !destCrs.equals( sourceCrs )) {
               // features = new ReprojectingFeatureCollection( features, destCrs );
                
                final MathTransform transform = Geometries.transform( sourceCrs, destCrs );
                final String geomName = sourceSchema.getGeometryDescriptor().getLocalName();
                
                // actually copy features; the above just sets attribute which is not supported by caching data sources
                final SimpleFeatureType retypedSchema = SimpleFeatureTypeBuilder.retype( sourceSchema, destCrs );
                features = new RetypingFeatureCollection( features, retypedSchema ) {
                    protected Feature retype( Feature feature ) {
                        try {
                            SimpleFeatureBuilder fb = new SimpleFeatureBuilder( retypedSchema );
                            fb.init( (SimpleFeature)feature );
                            Geometry geom = (Geometry)feature.getProperty( geomName ).getValue();
                            fb.set( geomName, JTS.transform( geom, transform ) );
                            return fb.buildFeature( feature.getIdentifier().getID() );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                };
            }
            
            // transform geometry types
            SimpleFeatureType featuresSchema = (SimpleFeatureType)features.getSchema();
            final GeometryDescriptor featureGeom = featuresSchema.getGeometryDescriptor();
            final GeometryDescriptor destGeom = destSchema.getGeometryDescriptor();
            if (featureGeom != null && destGeom != null
                    && !featureGeom.getType().getBinding().equals( destGeom.getType().getBinding() )) {
             
                SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                ftb.init( featuresSchema );
                ftb.remove( featureGeom.getLocalName() );
                ftb.add( destGeom.getLocalName(), destGeom.getType().getBinding(), destGeom.getCoordinateReferenceSystem() );
                final SimpleFeatureType retypedSchema = ftb.buildFeatureType();
                
                features = new RetypingFeatureCollection( features, retypedSchema ) {
                    protected Feature retype( Feature feature ) {
                        try {
                            SimpleFeatureBuilder fb = new SimpleFeatureBuilder( retypedSchema );
                            fb.init( (SimpleFeature)feature );
                            Geometry geom = (Geometry)feature.getProperty( featureGeom.getLocalName() ).getValue();

                            // Point -> MultiPolygon
                            if (destGeom.getType().getBinding().equals( MultiPolygon.class )
                                    || destGeom.getType().getBinding().equals( Polygon.class )) {
                                geom = Geometries.transform( geom, destCrs, Geometries.crs( "EPSG:3857" ) );
                                geom = geom.buffer( 10, 3 );
                                geom = Geometries.transform( geom, Geometries.crs( "EPSG:3857" ), destCrs );
                                
                                fb.set( destGeom.getLocalName(), geom );
                                return fb.buildFeature( feature.getIdentifier().getID() );
                            }
                            else {
                                throw new UnsupportedOperationException( "Unsupported geometry transformation: " + destGeom.getType().getBinding().getSimpleName() );
                            }
                        }
                        catch (RuntimeException e) {
                            throw e;
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                };
            }
            
            createdFeatureIds = destFs.addFeatures( features, new ProgressListenerAdaptor( copyMonitor ) );

            monitor.done();
            return Status.OK;
        }
        return Status.Cancel;
    }


    /**
     *
     */
    class FeatureEditorPage2
            extends OperationWizardPage
            implements IWizardPage, IPageChangedListener {

        public static final String          ID = "FeatureEditorPage2";
        
        private Composite                   content;

        private Properties                  configPageProps = new Properties();

        private FeatureTypeEditorProcessorConfig configPage;
        

        protected FeatureEditorPage2() {
            super( ID );
            setTitle( i18n.get( "FeatureEditorPage_title" ) );
            setDescription( i18n.get( "FeatureEditorPage_description" ) );
        }

        public void createControl( Composite parent ) {
            this.content = new Composite( parent, SWT.NONE );
            FormLayout layout = new FormLayout();
            layout.spacing = 5;
            content.setLayout( layout );
            setControl( content );
            getWizard().addPageChangedListener( WeakListener.forListener( this ) );
        }

        public void pageChanged( PageChangedEvent ev ) {
            log.info( "pageChanged(): ev= " + ev.getSelectedPage() );
            if (ev.getSelectedPage() == this /*&& editor == null*/) {
                pageEntered();
            }
        }

        protected void pageEntered() {
            getContainer().getShell().setMinimumSize( 600, 600 );
            getContainer().getShell().layout( true );

            setErrorMessage( null );

            // create default mapping
            FeatureTypeMapping mappings = new FeatureTypeMapping();
            SimpleFeatureType schema = null;
            try {
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( dest, true );
                schema = fs.getSchema();
            }
            catch (Exception e) {
                throw new RuntimeException( i18n.get( "FeatureEditorPage_errorLayerSchema" ), e );
            }
            
            SimpleFeatureType sourceSchema = source.getSchema();

            // check CRSs
            if (!CRS.equalsIgnoreMetadata( schema.getCoordinateReferenceSystem(),
                    sourceSchema.getCoordinateReferenceSystem() )) {
                setMessage( i18n.get( "FeatureEditorPage_errorCrs",
                        CRS.toSRS( schema.getCoordinateReferenceSystem() ),
                        CRS.toSRS( sourceSchema.getCoordinateReferenceSystem() ) ), DialogPage.INFORMATION );
            }

            // geometry attribute
            GeometryDescriptor destGeomAttr = schema.getGeometryDescriptor();
            GeometryDescriptor sourceGeomAttr = sourceSchema.getGeometryDescriptor();
            if (destGeomAttr != null && sourceGeomAttr != null) {
                
                mappings.put( new AttributeMapping( destGeomAttr.getLocalName(),
                        // binding and CRS are tranformed in #execute()
                        sourceGeomAttr.getType().getBinding(),
                        sourceGeomAttr.getCoordinateReferenceSystem(),
                        sourceGeomAttr.getLocalName(), null ) );

                // check geometry type
                Class destGeomType = destGeomAttr.getType().getBinding();
                Class sourceGeomType = sourceGeomAttr.getType().getBinding();
                if (!destGeomType.isAssignableFrom( sourceGeomType )) {
                    setMessage( i18n.get( "FeatureEditorPage_errorGeom",
                            destGeomType.getSimpleName(), sourceGeomType.getSimpleName() ),
                            DialogPage.WARNING );
                }
            }

            for (AttributeDescriptor destAttr : schema.getAttributeDescriptors()) {
                if (destAttr == destGeomAttr) {
                    continue;
                }

                // find best matching attribut
                log.debug( "Find mapping for: " + destAttr.getLocalName() );
                AttributeDescriptor matchingAttr = null;
                int score = Integer.MAX_VALUE;

                for (AttributeDescriptor sourceAttr : sourceSchema.getAttributeDescriptors()) {

                    if (destAttr.getType().getBinding().isAssignableFrom(
                            sourceAttr.getType().getBinding() )) {

                        int s = StringUtils.getLevenshteinDistance(
                            sourceAttr.getLocalName().toLowerCase(), destAttr.getLocalName().toLowerCase() );

                        log.debug( "    check: " + sourceAttr.getLocalName() + ", score:" + s );
                        if (s < score) {
                            score = s;
                            matchingAttr = sourceAttr;
                            log.debug( "    match: " + matchingAttr.getLocalName() + " (" + score + ")" );
                        }
                    }
                }
                if (matchingAttr != null) {
                    mappings.put( new AttributeMapping( destAttr.getLocalName(), destAttr.getType().getBinding(), null,
                            matchingAttr.getLocalName(), null ) );
                }
            }
            configPageProps.put( "mappings", mappings.serialize() );

            if (configPage != null) {
                configPage.getControl().dispose();                
            }
            configPage = new FeatureTypeEditorProcessorConfig();
            configPage.init( dest, configPageProps );
            configPage.setSourceFeatureType( sourceSchema );

            configPage.createControl( content );
            configPage.getControl().setLayoutData( new SimpleFormData().fill().create() );
            content.layout( true );

            getContainer().updateButtons();
        }

        public RetypingFeatureCollection retyped( FeatureCollection src ) {
            final FeatureTypeEditorProcessor processor = new FeatureTypeEditorProcessor();
            processor.init( configPageProps );
            final SimpleFeatureBuilder builder = new SimpleFeatureBuilder( (SimpleFeatureType)processor.getFeatureType() );

            return new RetypingFeatureCollection( src, null ) {
                @Override
                public FeatureType getSchema() {
                    return processor.getFeatureType();
                }
                @Override
                protected Feature retype( Feature feature ) {
                    try {
                        // make new FID to avoid problem when copying features of the same layer
                        return processor.transformFeature( (SimpleFeature)feature, builder, null );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            };
        }
        
        public void performFinish() {
            log.info( "performFinish(): ..." );

            configPage.performOk();
//
//            PipelineProcessor proc = source.getPipeline().get( 0 );
//            if (!(proc instanceof FeatureTypeEditorProcessor)) {
//                proc = new FeatureTypeEditorProcessor();
//                source.getPipeline().addFirst( proc );
//            }
//            ((FeatureTypeEditorProcessor)proc).init( configPageProps );
//
//            // give FeatureTypeEditorProcessor the upstream FeatureType
//            SimpleFeatureType schema = source.getSchema();
//            log.debug( "Schema: " + schema );
        }

        public boolean isPageComplete() {
            if (getErrorMessage() != null) {
                return false;
            }
            return configPage != null ? configPage.okToLeave() : false;
        }

    }


//    /**
//     * Adapt geometry types:
//     * <ul>
//     * <li>Any -> Point: </li>
//     * </ul> 
//     */
//    class GeometryAdaptingFeatureCollection
//            extends RetypingFeatureCollection<SimpleFeatureType,SimpleFeature> {
//
//        Class<? extends Geometry>       targetType;
//        
//
//        public GeometryAdaptingFeatureCollection( FeatureCollection delegate, SimpleFeatureType targetSchema ) {
//            super( delegate, targetSchema );
//            GeometryDescriptor geomDescr = targetSchema.getGeometryDescriptor();
//            targetType = (Class<? extends Geometry>)(geomDescr != null ? geomDescr.getType().getBinding() : null);
//            
//            SimpleFeatureTypeBuilder ftp = new SimpleFeatureTypeBuilder();
//            ftp.init( targetSchema );
//            
//            JTS.
//        }
//
//
//
//        protected SimpleFeature retype( SimpleFeature input ) {
//            GeometryAttribute prop = input.getDefaultGeometryProperty();
//            if (targetType != null && prop != null) {
//                // same type?
//                if (prop.getType().getBinding() == targetType) {
//                    return input;
//                }
//                // different types
//                else {
//                    new
//                }
//            }
//            else {
//                return input;
//            }
//        }
//        
//    }
    
    
    /**
     * @deprecated
     */
    class FeatureEditorPage
            extends OperationWizardPage
            implements IWizardPage, IPageChangedListener {

        public static final String          ID = "FeatureEditorPage";

        private FeatureTypeEditor           editor;

        private Composite                   contents;


        protected FeatureEditorPage() {
            super( ID );
            setTitle( i18n.get( "FeatureEditorPage_title" ) );
            setDescription( i18n.get( "FeatureEditorPage_description" ) );
        }

        public void createControl( Composite parent ) {
            this.contents = new Composite( parent, SWT.BORDER );
            FormLayout layout = new FormLayout();
            layout.spacing = 5;
            contents.setLayout( layout );
            setControl( contents );
            getWizard().addPageChangedListener( WeakListener.forListener( this ) );
        }

        public void pageChanged( PageChangedEvent ev ) {
            log.info( "pageChanged(): ev= " + ev.getSelectedPage() );
            if (ev.getSelectedPage() == this && editor == null) {
                getContainer().getShell().setMinimumSize( SWT.DEFAULT, 600 );
                getContainer().getShell().layout( true );

                editor = new FeatureTypeEditor();

                FeatureTypeMapping mapping = new FeatureTypeMapping();
                editor.addViewerColumn( new ValueViewerColumn( mapping, source.getSchema() ) );

                try {
                    PipelineFeatureSource fs = PipelineFeatureSource.forLayer( dest, true );
                    editor.createTable( contents, new SimpleFormData().fill().create(), fs.getSchema(), true );
                    contents.layout( true );
                }
                catch (Exception e) {
                    throw new RuntimeException( i18n.get( "FeatureEditorPage_errorLayerSchema" ), e );
                }
            }
        }

        public boolean isPageComplete() {
            return true;
        }

    }

    class DirectCopyPage
            extends OperationWizardPage
            implements IWizardPage, IPageChangedListener {

        public static final String      ID = "DirectCopyPage";

        private Composite               contents;

        private boolean                 isDirectCopy;


        protected DirectCopyPage() {
            super( ID );
            setTitle( i18n.get( "DirectCopyPage_title" ) );
            setDescription( i18n.get( "DirectCopyPage_description" ) );
        }
        
        public boolean isDirectCopy() {
            return isDirectCopy;
        }

        public void createControl( Composite parent ) {
            this.contents = new Composite( parent, SWT.NONE );
            FormLayout layout = new FormLayout();
            layout.spacing = 5;
            layout.marginWidth = layout.marginHeight = 20;
            contents.setLayout( layout );
            setControl( contents );
            getWizard().addPageChangedListener( WeakListener.forListener( this ) );
            
            final Button check = new Button( contents, SWT.CHECK );
            check.setText( i18n.get( "DirectCopyPage_text" ) );
            check.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent e ) {
                    isDirectCopy = check.getSelection();
                }
            });
        }

        @Override
        public void pageChanged( PageChangedEvent ev ) {
            log.info( "ev: " + ev );
        }
        
    }
    
}
