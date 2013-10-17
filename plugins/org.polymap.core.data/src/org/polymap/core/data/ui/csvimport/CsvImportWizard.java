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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.catalog.actions.ResetServiceAction;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.UIJob;

import static org.polymap.core.data.ui.csvimport.Messages.i18n;

/**
 * @author Andrea Antonello - www.hydrologis.com
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class CsvImportWizard extends Wizard implements INewWizard {

    private CsvImportWizardPage                     page1;

    private CsvImportWizardPage2                    page2;

    private final Map<String, String>               params    = new HashMap<String, String>();

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> csvFeatureCollection;


    public CsvImportWizard() {
        super();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle( i18n( "CsvImportWizard.csvimport" ) );
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                DataPlugin.PLUGIN_ID, "icons/workset_wiz.png" ) );
        setNeedsProgressMonitor(true);
        page1 = new CsvImportWizardPage( i18n( "CsvImportWizard.csvimport" ), params );
        page2 = new CsvImportWizardPage2( i18n( "CsvImportWizard.csvimport" ), params );
    }

    public void addPages() {
        super.addPages();
        addPage( page1 );
        addPage( page2 );
    }

    public String getCsvFilename() {
        return page1.getCsvFilename();
    }
    
    void createCsvFeatureCollection() {
        final String name = page2.getShpName();
        final CoordinateReferenceSystem crs = page1.getCrs();
        final CsvImporter csvImporter = page1.getCsvImporter();
        final LinkedHashMap<String, Integer> fieldsAndTypesIndex = page1.getFieldsAndTypesIndex();

        UIJob job = new UIJob( "Reading CSV Data" ) {
            protected void runWithException( IProgressMonitor monitor )
            throws Exception {
                try {
                    csvFeatureCollection = csvImporter.createFeatureCollection( name, 
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
        
        UIJob job = new UIJob( i18n( "CsvImportWizard.tasktitle" ) ) {
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                monitor.beginTask( i18n( "CsvImportWizard.tasktitle" ), IProgressMonitor.UNKNOWN );

                IResolve service = page2.getTarget();
                SimpleFeatureType featureType = csvFeatureCollection.getSchema();
                CoordinateReferenceSystem crs = page1.getCrs();

                DataAccess ds = page2.getTarget().resolve( DataAccess.class, new SubProgressMonitor( monitor, 1 ) );
                ds.createSchema( featureType );

                // write the features
                FeatureStore<SimpleFeatureType, SimpleFeature> fs =
                        (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource( featureType.getName() );

                // no transaction: save memory                        
                fs.addFeatures( csvFeatureCollection );

                // reset service in catalog
                Thread.sleep( 1000 );

                if (service instanceof IService) {
                    ResetServiceAction.reset( Collections.singletonList( (IService)service ), new SubProgressMonitor( monitor, 1 ) );
                }
                else if (service instanceof IResolveFolder) {
                    ResetServiceAction.reset( 
                            Collections.singletonList( ((IResolveFolder)service).getService( monitor ) ),
                            new SubProgressMonitor( monitor, 1 ) );                    
                }
                monitor.done();
            }
        };
        job.setUser( true );  //ShowProgressDialog( i18n( "CsvImportWizard.tasktitle" ), false );
        job.schedule();
        return true;
    }

}
