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

import java.util.Properties;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import org.geotools.data.Query;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.data.Messages;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.typeeditor.AttributeMapping;
import org.polymap.core.data.feature.typeeditor.FeatureTypeEditorProcessor;
import org.polymap.core.data.feature.typeeditor.FeatureTypeEditorProcessorConfig;
import org.polymap.core.data.feature.typeeditor.FeatureTypeMapping;
import org.polymap.core.data.operations.ChooseLayerPage;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.ui.featuretypeeditor.FeatureTypeEditor;
import org.polymap.core.data.ui.featuretypeeditor.ValueViewerColumn;
import org.polymap.core.data.util.ProgressListenerAdaptor;
import org.polymap.core.operation.OperationWizard;
import org.polymap.core.operation.OperationWizardPage;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.WeakListener;

/**
 * Modifies features. This operation is triggered by
 * {@link ModifyFeatureEditorAction} but might be used by other classes as well.
 * <p>
 * ...
 * @deprecated As of {@link CopyFeaturesOperation2}
 * @see NewFeatureOperation
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class CopyFeaturesOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( CopyFeaturesOperation.class );

    private IGeoResource            sourceGeores;

    private Query                   sourceQuery;

    private PipelineFeatureSource   source;

    ILayer                  dest;


    /**
     * Creates a new operation without the destination layer set. This will open a dialog
     * when executing, which allows to choose the destination layer.
     *
     * @param source
     * @param query The query to be used to fetch the features, or null if all features
     *        should be fetched.
     */
    public CopyFeaturesOperation( PipelineFeatureSource source, Query query ) {
        super( Messages.get( "CopyFeaturesOperation_label" ) );
        this.source = source;
        this.sourceQuery = query;
    }


    public CopyFeaturesOperation( IGeoResource geores ) {
        super( Messages.get( "CopyFeaturesOperation_label" ) );
        this.sourceGeores = geores;
    }


    public PipelineFeatureSource getSource() {
        return source;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        monitor.beginTask( getLabel(), 10 );

        // narrow source
        monitor.subTask( "Datenstruktur der Quelle ermitteln..." );
        if (sourceGeores != null) {
            try {
                IService service = sourceGeores.service(
                        new SubProgressMonitor( monitor, 0 ) );

                //Pipeline pipeline = new DefaultPipelineIncubator().newPipeline( LayerUseCase.FEATURES, null, null, service );
                Pipeline pipeline = new Pipeline( null, null, service );
                DataSourceProcessor dataSource = new DataSourceProcessor();
                dataSource.setGeores( sourceGeores );
                pipeline.addLast( dataSource );

                source = new PipelineFeatureSource( pipeline );
                log.info( "Source created from GeoRes: " + source );
            }
            catch (Exception e) {
                throw new ExecutionException( "", e );
            }
        }
        monitor.worked( 1 );

        // open wizard dialog
        monitor.subTask( "Eingaben vom Nutzer..." );
        OperationWizard wizard = new OperationWizard( this, info, monitor ) {
            public boolean doPerformFinish()
            throws Exception {
                ((FeatureEditorPage2)getPage( FeatureEditorPage2.ID )).performFinish();
                return true;
            }
        };
        final ChooseLayerPage chooseLayerPage = new ChooseLayerPage(
                Messages.get( "CopyFeaturesOperation_ChooseLayerPage_title" ),
                Messages.get( "CopyFeaturesOperation_ChooseLayerPage_description" ),
                true );
        wizard.addPage( chooseLayerPage );
        final FeatureEditorPage2 featureEditorPage = new FeatureEditorPage2();
        wizard.addPage( featureEditorPage );

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
            try {
                final IProgressMonitor copyMonitor = new SubProgressMonitor( monitor, 8,
                        SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );
                
                final PipelineFeatureSource destFs = PipelineFeatureSource.forLayer( dest, true );
                FeatureCollection features = sourceQuery != null
                        ? source.getFeatures( sourceQuery )
                        : source.getFeatures();

                copyMonitor.beginTask( "Objekte kopieren", features.size() );
                
                CoordinateReferenceSystem destCrs = destFs.getSchema().getCoordinateReferenceSystem();
//                final MathTransform transform = CRS.findMathTransform( 
//                        source.getSchema().getCoordinateReferenceSystem(), destCrs );

//                // all features
//                int c = 0;
////                List<Feature> chunk = new ArrayList( DataSourceProcessor.DEFAULT_CHUNK_SIZE );
//                features.accepts( new FeatureVisitor() {
//                    public void visit( Feature feature ) {
//                        try {
//                            GeometryAttribute geomAttr = feature.getDefaultGeometryProperty();
//                            Geometry geom = (Geometry)geomAttr.getValue();
//                            Geometry transformed = JTS.transform( geom, transform );
//                            geomAttr.setValue( transformed );
//                            
//                            FeatureCollection coll = FeatureCollections.newCollection();
//                            coll.add( feature );
//                            
//                            destFs.addFeatures( coll );
//                            copyMonitor.worked( 1 );
//                        }
//                        catch (Exception e) {
//                            throw new RuntimeException( e );
//                        }
//                    }
//                }, null );
                
                ReprojectingFeatureCollection reprojected = new ReprojectingFeatureCollection( features, destCrs );
                destFs.addFeatures( reprojected, new ProgressListenerAdaptor( copyMonitor ) );
                
                monitor.done();
                return Status.OK_STATUS;
            }
            catch (Exception e) {
                throw new ExecutionException( Messages.get( "CopyFeaturesOperation_executeError" ), e );
            }
        }
        return Status.CANCEL_STATUS;
    }


    public boolean canUndo() {
        return false;
    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean canRedo() {
        return false;
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     *
     */
    class FeatureEditorPage2
            extends OperationWizardPage
            implements IWizardPage, IPageChangedListener {

        public static final String          ID = "FeatureEditorPage2";

        private FeatureTypeEditor           editor;

        private Composite                   content;

        private Properties                  configPageProps = new Properties();

        private FeatureTypeEditorProcessorConfig configPage;

        protected FeatureEditorPage2() {
            super( ID );
            setTitle( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_title" ) );
            setDescription( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_description" ) );
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
            getContainer().getShell().setMinimumSize( 700, 500 );
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
                throw new RuntimeException( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_errorLayerSchema" ), e );
            }
            SimpleFeatureType sourceSchema = source.getSchema();

            // check CRSs
            if (!CRS.equalsIgnoreMetadata( schema.getCoordinateReferenceSystem(),
                    sourceSchema.getCoordinateReferenceSystem() )) {
                setMessage( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_errorCrs",
                        CRS.toSRS( schema.getCoordinateReferenceSystem() ),
                        CRS.toSRS( sourceSchema.getCoordinateReferenceSystem() ) ), DialogPage.WARNING );
            }

            // geometry attribute
            GeometryDescriptor destGeomAttr = schema.getGeometryDescriptor();
            GeometryDescriptor sourceGeomAttr = sourceSchema.getGeometryDescriptor();
            mappings.put( new AttributeMapping( destGeomAttr.getLocalName(),
                    destGeomAttr.getType().getBinding(),
                    destGeomAttr.getCoordinateReferenceSystem(),
                    sourceGeomAttr.getLocalName(), null ) );

            // check geometry type
            Class destGeomType = destGeomAttr.getType().getBinding();
            Class sourceGeomType = sourceGeomAttr.getType().getBinding();
            if (!destGeomType.isAssignableFrom( sourceGeomType )) {
                setMessage( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_errorGeom",
                        destGeomType.getSimpleName(), sourceGeomType.getSimpleName() ),
                        DialogPage.WARNING );
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

            configPage = new FeatureTypeEditorProcessorConfig();
            configPage.init( dest, configPageProps );
            configPage.setSourceFeatureType( sourceSchema );

            configPage.createControl( content );
            configPage.getControl().setLayoutData( new SimpleFormData().fill().create() );
            content.layout( true );

            getContainer().updateButtons();
        }

        public void performFinish() {
            log.info( "performFinish(): ..." );

            configPage.performOk();

            PipelineProcessor proc = source.getPipeline().get( 0 );
            if (!(proc instanceof FeatureTypeEditorProcessor)) {
                proc = new FeatureTypeEditorProcessor();
                source.getPipeline().addFirst( proc );
            }
            ((FeatureTypeEditorProcessor)proc).init( configPageProps );

            // give FeatureTypeEditorProcessor the upstream FeatureType
            SimpleFeatureType schema = source.getSchema();
            log.debug( "Schema: " + schema );
        }

        public boolean isPageComplete() {
            if (getErrorMessage() != null) {
                return false;
            }
            return configPage != null ? configPage.okToLeave() : false;
        }

    }


    /**
     *
     */
    class FeatureEditorPage
            extends OperationWizardPage
            implements IWizardPage, IPageChangedListener {

        public static final String          ID = "FeatureEditorPage";

        private FeatureTypeEditor           editor;

        private Composite                   contents;


        protected FeatureEditorPage() {
            super( ID );
            setTitle( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_title" ) );
            setDescription( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_description" ) );
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
                    throw new RuntimeException( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_errorLayerSchema" ), e );
                }
            }
        }

        public boolean isPageComplete() {
            return true;
        }

    }

}
