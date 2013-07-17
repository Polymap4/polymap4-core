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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.data.DataAccess;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.ui.CatalogTreeViewer;
import net.refractions.udig.ui.FeatureTableControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;

import org.polymap.core.project.ui.util.SelectionAdapter;
import org.polymap.core.project.ui.util.SimpleFormData;

import static org.polymap.core.data.ui.csvimport.Messages.i18n;

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
    
    private Text                      shpNameText;

    private String                    shpName = "csvimport";
    
    private IResolve                  target;


    public CsvImportWizardPage2( String pageName, Map<String, String> params ) {
        super( ID );
        setTitle( pageName );
        setDescription( i18n( "CsvImportWizardPage.importasshape" ) );        
    }

    @Override
    public CsvImportWizard getWizard() {
        return (CsvImportWizard)super.getWizard();
    }

    public void createControl( Composite parent ) {
        getWizard().getShell().setSize( 520, 650 );
        
        fileSelectionArea = new Composite( parent, SWT.NONE );
        fileSelectionArea.setLayout( new GridLayout() );

        Group inputGroup = new Group( fileSelectionArea, SWT.None );
        inputGroup.setText( i18n( "CsvImportWizardPage.importinto" ) );
        inputGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );
        inputGroup.setLayout( new FormLayout() );

        Label l = new Label( inputGroup, SWT.NONE );
        l.setText( i18n( "CsvImportWizardPage.typename" ) );
        l.setLayoutData( SimpleFormData.offset( 5 ).left( 0 ).top( 0 ).create() );
        
//        shpName = getWizard().getCsvFilename();
        shpNameText = new Text( inputGroup, SWT.BORDER );
        shpNameText.setLayoutData( SimpleFormData.offset( 5 ).left( l ).right( 100 ).create() );
        shpNameText.setText( shpName );
        shpNameText.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                shpName = shpNameText.getText();
                log.info( "shpName= " + shpName );
                getWizard().createCsvFeatureCollection();
            }
        });
        
        CatalogTreeViewer catalogViewer = new CatalogTreeViewer( inputGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, true );
        catalogViewer.getControl().setLayoutData( SimpleFormData.offset( 5 ).left( 0 ).right( 100 ).top( shpNameText ).height( 150 ).bottom( 100 ).create() );
        catalogViewer.setInput( CatalogPlugin.getDefault().getLocalCatalog() );
        catalogViewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                target = new SelectionAdapter( ev.getSelection() ).first( IResolve.class );
                target = target != null && target.canResolve( DataAccess.class ) ? target : null;
                checkFinish();
            }
        });
        
        setControl( fileSelectionArea );
        
        checkFinish();
    }

//    public int getImportTarget() {
//        return importTarget;
//    }
    
    public String getShpName() {
        return shpName;
    }
    
    public IResolve getTarget() {
        return target;
    }

    public void setVisible( boolean visible ) {
        super.setVisible( visible );

        if (visible) {
            getWizard().createCsvFeatureCollection();
            setFeatureCollection( getWizard().csvFeatureCollection );
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
                fileSelectionArea.layout( true );
            }
            else {
                tableViewer.setFeatures( features );
                fileSelectionArea.layout( true );
            }
        }
        catch (Exception e) {
            log.warn( "unhandled: ", e );
        }
    }

    protected void checkFinish() {
        setMessage( null );
        setPageComplete( true );
        
        if (target == null) {
            setMessage( i18n( "CsvImportWizardPage.noservice" ), WARNING );
            setPageComplete( false );
        }
    }
    
}
