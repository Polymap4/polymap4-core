/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
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

import java.util.Map;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import net.refractions.udig.ui.FeatureTableControl;
import net.refractions.udig.ui.PlatformJobs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class CsvImportWizardPage2 extends WizardPage {

    private static Log log = LogFactory.getLog( CsvImportWizardPage2.class );

    public static final String        ID = "CsvImportWizardPage2"; //$NON-NLS-1$
    
    private FeatureTableControl       tableViewer;
    
    private FeatureCollection<SimpleFeatureType, SimpleFeature> features;

    private Composite                 fileSelectionArea;

    private Button                    memBtn, shpBtn;
    
    private int                       importTarget;
    
    private Text                      shpNameText;

    private String                    shpName = "csvimport";



    public CsvImportWizardPage2( String pageName, Map<String, String> params ) {
        super( ID );
        setTitle( pageName );
        setDescription( Messages.getString( "CsvImportWizardPage.importasshape" ) ); //$NON-NLS-1$
    }

    
    public void createControl( Composite parent ) {
        fileSelectionArea = new Composite( parent, SWT.NONE );
        fileSelectionArea.setLayout( new GridLayout() );

        Group inputGroup = new Group( fileSelectionArea, SWT.None );
        inputGroup.setText( "Import into" );
        inputGroup.setLayout( new GridLayout( 3, false ) );
        inputGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );

        // target selection
        memBtn = new Button( inputGroup, SWT.RADIO );
        memBtn.setText( "Memory Store" );
        memBtn.setToolTipText( "Import into temporary memory store" );
        memBtn.addListener( SWT.Selection, new Listener() {
            public void handleEvent( Event ev ) {
                importTarget = memBtn.getSelection() ? 1 : importTarget;
                shpNameText.setEnabled( false );
            }
        } );

        shpBtn = new Button( inputGroup, SWT.RADIO );
        shpBtn.setSelection( true );
        importTarget = 2;
        shpBtn.setText( "Shapefile" );
        shpBtn.setToolTipText( "Import into shapefile" );
        shpBtn.addListener( SWT.Selection, new Listener() {
            public void handleEvent( Event ev ) {
                log.debug( "stateMask= " + ev.stateMask );
                importTarget = shpBtn.getSelection() ? 2 : importTarget;
                shpNameText.setEnabled( true );
            }
        } );
        //radios[0].setBounds(10, 5, 75, 30);

        shpNameText = new Text( inputGroup, SWT.BORDER );
        shpNameText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );
        shpNameText.setText( shpName );
        shpNameText.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                shpName = shpNameText.getText();
                log.info( "shpName= " + shpName );
            }
        });
//        shpNameText.addKeyListener( new KeyListener() {
//            public void keyReleased( KeyEvent e ) {
//                shpName = shpNameText.getText();
//                log.info( "shpName= " + shpName );
//            }
//            public void keyPressed( KeyEvent e ) {
//                shpName = shpNameText.getText();
//                log.info( "shpName= " + shpName );
//            }
//        });
        
        
        checkFinish();
        setControl(fileSelectionArea);
    }

    public int getImportTarget() {
        return importTarget;
    }
    
    public String getShpName() {
        return shpName;
    }
    
    public void setVisible( boolean visible ) {
        super.setVisible( visible );

        if (visible = true) {
            log.info( "setVisible() = " + visible );

            // complete -> job: create fc
            if (isPageComplete()) {
                final CsvImportWizard csvWizard = (CsvImportWizard)getWizard();

                PlatformJobs.runInProgressDialog( "Reading data", false, new IRunnableWithProgress() {
                    public void run( IProgressMonitor monitor )
                    throws InvocationTargetException, InterruptedException {
                        csvWizard.createCsvFeatureCollection();
                    }
                }, false );

                setFeatureCollection( csvWizard.csvFeatureCollection );
            }
        }
    }


    public void setFeatureCollection(
            FeatureCollection<SimpleFeatureType, SimpleFeature> csvFeatureCollection ) {
        this.features = csvFeatureCollection;
        
        try {
            log.debug( "Features size: " + features.size() );
            if (tableViewer == null) {
                GridData gridData1 = new GridData();
                gridData1.horizontalSpan = 2;
                gridData1.horizontalAlignment = GridData.FILL;
                gridData1.grabExcessHorizontalSpace = true;
                gridData1.grabExcessVerticalSpace = true;
                gridData1.verticalAlignment = GridData.FILL;
                Composite comp = new Composite(fileSelectionArea, SWT.NONE);
                comp.setLayout(new FillLayout());
                comp.setLayoutData(gridData1);
                
                tableViewer = new FeatureTableControl( comp, features );
            }
            else {
                tableViewer.setFeatures( features );
            }
        }
        catch (Exception e) {
            log.warn( "unhandled: ", e );
        }
    }


    private void checkFinish() {
        ((CsvImportWizard)getWizard()).canFinish = true;
    }

}
