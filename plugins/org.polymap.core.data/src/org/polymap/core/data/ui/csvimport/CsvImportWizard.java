/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * Copyright 2011, Polymap GmbH. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.polymap.core.data.ui.csvimport;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import java.io.File;
import java.io.Serializable;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.internal.shp.ShpServiceExtension;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.UIJob;

import static org.polymap.core.data.ui.csvimport.Messages.i18n;

/**
 * @author Andrea Antonello - www.hydrologis.com
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
@SuppressWarnings("restriction")
public class CsvImportWizard extends Wizard implements INewWizard {

    private CsvImportWizardPage                     page1;

    private CsvImportWizardPage2                    page2;

    private final Map<String, String>               params    = new HashMap<String, String>();

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> csvFeatureCollection;

    protected boolean                               canFinish = false;


    public CsvImportWizard() {
        super();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle( i18n( "CsvImportWizard.csvimport" ) );
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                DataPlugin.PLUGIN_ID, "icons/workset_wiz.png" ) );
        setNeedsProgressMonitor(true);
        page1 = new CsvImportWizardPage( i18n( "CsvImportWizard.csvimport" ), params);
        page2 = new CsvImportWizardPage2( i18n( "CsvImportWizard.csvimport" ), params );
    }

    public void addPages() {
        super.addPages();
        addPage( page1 );
        addPage( page2 );
    }

    void createCsvFeatureCollection() {
        final CoordinateReferenceSystem crs = page1.getCrs();
        final CsvImporter csvImporter = page1.getCsvImporter();
        final LinkedHashMap<String, Integer> fieldsAndTypesIndex = page1.getFieldsAndTypesIndex();

        UIJob job = new UIJob( "Reading CSV Data" ) {
            protected void runWithException( IProgressMonitor monitor )
            throws Exception {
                try {
                    csvFeatureCollection = csvImporter.createFeatureCollection( 
                            crs, fieldsAndTypesIndex, new EclipseProgressMonitorAdapter( monitor ) );
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    ExceptionDetailsDialog.openError( null, i18n( "CsvImportWizard.error" ), 
                            IStatus.ERROR, DataPlugin.PLUGIN_ID, e);
                }
            }            
        };
        job.setShowProgressDialog( job.getName(), false );
        job.schedule();
        
        if (! job.joinAndDispatch( 180000 )) {
            job.cancelAndInterrupt();
            MessageDialog.openInformation( getShell(), "Info", i18n( "CsvImportWizard.timeout" ) );
        }
        
        //FIXME
        //page2.setFeatureCollection( csvFeatureCollection );
    }
    
    /**
     * 
     */
    public boolean performFinish() {
        final AtomicBoolean ok_flag = new AtomicBoolean( false );
        
        UIJob job = new UIJob( "CSV Import" ) {
            protected void runWithException( IProgressMonitor monitor )
            throws Exception {
                try {
                    monitor.beginTask( i18n( "CsvImportWizard.tasktitle" ), IProgressMonitor.UNKNOWN );
                    
                    SimpleFeatureType featureType = csvFeatureCollection.getSchema();
                    CoordinateReferenceSystem crs = page1.getCrs();

                    // memory store
                    if (page2.getImportTarget() == 1) {
                        throw new RuntimeException( "Memory store is not supported" );
//                        JGrassCatalogUtilities.removeMemoryServiceByTypeName(featureType.getTypeName());
//                        IGeoResource resource = CatalogPlugin.getDefault().getLocalCatalog()
//                                .createTemporaryResource(featureType);
//                        resource.resolve(FeatureStore.class, pm).addFeatures(csvFeatureCollection);
                    }

                    // create shape dataStore
                    else if (page2.getImportTarget() == 2) {
                        IWorkspace workspace = ResourcesPlugin.getWorkspace();
                        IWorkspaceRoot root = workspace.getRoot();
                        IPath path = root.getLocation();
                        System.out.println( "Workspace root: " + path.toString() );

                        ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

//                        Date now = new Date();
//                        String suffix = "_" + now.getDate() + "_" + now.getTime();
                        File newFile = new File( path.toFile(), page2.getShpName() /*+ suffix*/ + ".shp" );
                        DataStoreFactorySpi dataStoreFactory = new ShapefileDataStoreFactory();

                        Map<String, Serializable> shapeParams = new HashMap<String, Serializable>();
                        shapeParams.put("url", newFile.toURI().toURL());
                        shapeParams.put("create spatial index", Boolean.TRUE);

                        ShapefileDataStore newDataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(shapeParams);
                        newDataStore.createSchema( featureType );
                        newDataStore.forceSchemaCRS( crs );
                        //newDataStore.setStringCharset( Charset.forName( "ISO-8859-1" ) );

                        // write the features to shape
                        String typeName = newDataStore.getTypeNames()[0];
                        FeatureStore<SimpleFeatureType, SimpleFeature> fs =
                                (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource( typeName );
                        
                        // no transaction: save memory                        
                        fs.addFeatures( csvFeatureCollection );

                        // adding service to catalog
                        ShpServiceExtension creator = new ShpServiceExtension();
                        shapeParams = creator.createParams( newFile.toURI().toURL() );
                        IService service = creator.createService( null, shapeParams );
                        IServiceInfo info = service.getInfo( new NullProgressMonitor() ); // load

                        CatalogPlugin.getDefault().getLocalCatalog().add( service );
                        ok_flag.set( true );
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    ExceptionDetailsDialog.openError( null, i18n( "CsvImportWizard.error" ), 
                            IStatus.ERROR, DataPlugin.PLUGIN_ID, e);
                }

            }
        };
        job.setShowProgressDialog( "CSV Import", false );
        job.schedule();

        if (! job.joinAndDispatch( 180000 )) {
            job.cancelAndInterrupt();
            MessageDialog.openInformation( getShell(), "Info", i18n( "CsvImportWizard.timeout" ) );
        }

        return ok_flag.get();
    }

    public boolean canFinish() {
        return canFinish;
    }

}
