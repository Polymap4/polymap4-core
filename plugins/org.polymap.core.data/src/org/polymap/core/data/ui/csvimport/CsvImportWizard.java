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
import java.lang.reflect.InvocationTargetException;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
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
import net.refractions.udig.ui.PlatformJobs;
import org.eclipse.jface.operation.IRunnableWithProgress;
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

/**
 * @author Andrea Antonello - www.hydrologis.com
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
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
        setWindowTitle(Messages.getString("CsvImportWizard.fileimport")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                DataPlugin.PLUGIN_ID, "icons/workset_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
        page1 = new CsvImportWizardPage(Messages.getString("CsvImportWizard.csvimport"), params); //$NON-NLS-1$
        page2 = new CsvImportWizardPage2("Results", params); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        addPage(page1);
        addPage(page2);
    }

    void createCsvFeatureCollection() {
        final CoordinateReferenceSystem crs = page1.getCrs();
        final CsvImporter csvImporter = page1.getCsvImporter();
        final LinkedHashMap<String, Integer> fieldsAndTypesIndex = page1.getFieldsAndTypesIndex();

        IRunnableWithProgress operation = new IRunnableWithProgress(){
            public void run( IProgressMonitor monitor ) throws InvocationTargetException,
                    InterruptedException {
//                if (!csvFile.exists()) {
//                    ProblemDialogs.errorDialog( null,
//                            Messages.getString("CsvImportWizard.inputnotexist") + csvFile.getAbsolutePath(), true); //$NON-NLS-1$ 
//                    return;
//                }
                try {
                    csvFeatureCollection = csvImporter.createFeatureCollection( 
                            crs, fieldsAndTypesIndex, new EclipseProgressMonitorAdapter(monitor));
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    String message = Messages.getString("CsvImportWizard.error");
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            DataPlugin.PLUGIN_ID, e);
                }
            }
        };
        try {
            PlatformJobs.runSync(operation, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        //FIXME
        //page2.setFeatureCollection( csvFeatureCollection );
    }
    
    /**
     * 
     */
    public boolean performFinish() {
        final AtomicBoolean ok_flag = new AtomicBoolean( false );
        
        IRunnableWithProgress operation = new IRunnableWithProgress(){
            public void run( IProgressMonitor pm ) throws InvocationTargetException,
                    InterruptedException {

                try {
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

                        Map<String, Serializable> params = new HashMap<String, Serializable>();
                        params.put("url", newFile.toURI().toURL());
                        params.put("create spatial index", Boolean.TRUE);

                        ShapefileDataStore newDataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
                        newDataStore.createSchema( featureType );
                        newDataStore.forceSchemaCRS( crs );
                        //newDataStore.setStringCharset( Charset.forName( "ISO-8859-1" ) );

                        // write the features to shape
                        Transaction transaction = new DefaultTransaction( "create" );
                        String typeName = newDataStore.getTypeNames()[0];
                        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore =
                                (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource( typeName );
                        featureStore.setTransaction( transaction );
                        try {
                            featureStore.addFeatures(csvFeatureCollection);
                            transaction.commit();
                        } 
                        catch (Exception ee) {
                            transaction.rollback();
                            throw ee;
                        } 
                        finally {
                            transaction.close();
                        }

                        // adding service to catalog
                        ShpServiceExtension creator = new ShpServiceExtension();
                        params = creator.createParams( newFile.toURI().toURL() );
                        IService service = creator.createService( null, params );
                        IServiceInfo info = service.getInfo( new NullProgressMonitor() ); // load

                        CatalogPlugin.getDefault().getLocalCatalog().add( service );
                        ok_flag.set( true );
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    String message = Messages.getString("CsvImportWizard.error");
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            DataPlugin.PLUGIN_ID, e);
                }

            }

        };

        PlatformJobs.runInProgressDialog( "Importing data", true, operation, false );
        System.out.println( "after operation..." );
        
//        try {
//            PlatformJobs.runSync( operation, null );
//            //MessageDialog.openInformation( getShell(), "Info", "Shapefile created." );
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

        return ok_flag.get();
    }

    public boolean canFinish() {
        return canFinish;
    }

}
