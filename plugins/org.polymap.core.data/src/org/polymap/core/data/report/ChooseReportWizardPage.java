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
package org.polymap.core.data.report;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;

import org.polymap.core.data.Messages;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.report.ReportOperation.ReportSite;
import org.polymap.core.operation.OperationWizardPage;
import org.polymap.core.runtime.WeakListener;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ChooseReportWizardPage
    extends OperationWizardPage
    implements IWizardPage, IPageChangedListener {
        
    private static Log log = LogFactory.getLog( ChooseReportWizardPage.class );

    public static final String          ID = "ChooseReportWizardPage";

    private IFeatureOperationContext    context;

    private ReportSite                  site;

    private Composite                   content;
    
    private java.util.List<IReport>     reports = new ArrayList();
    
    private IReport                     selectedReport;
    
    private List                        reportsList;

    private Label                       desciptionLabel;


    protected ChooseReportWizardPage( IFeatureOperationContext context, ReportSite site ) {
        super( ID );
        this.context = context;
        this.site = site;
        
        setTitle( i18n( "title" ) );
        setDescription( i18n( "description" ) );
        
        reports = ReportFactoryExtension.reportsFor( site );
    }

    
    protected IReport getSelectedReport() {
        return selectedReport;
    }


    public void createControl( Composite parent ) {
        this.content = new Composite( parent, SWT.NONE );
        FillLayout layout = new FillLayout( SWT.VERTICAL );
        layout.spacing = 10;
        content.setLayout( layout );
        setControl( content );
        
        getWizard().addPageChangedListener( WeakListener.forListener( this ) );
        
        // reportsList
        reportsList = new List( content, SWT.BORDER );
        for (IReport report : reports) {
            reportsList.add( report.getLabel() );
        }
        reportsList.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                // selectedReport
                selectedReport = reports.get( reportsList.getSelectionIndex() );
                getContainer().updateButtons();
                
                String description = selectedReport.getDescription();
                desciptionLabel.setText( description != null ? description : "No description." );
                
                // add report pages
                java.util.List<IWizardPage> reportPages = selectedReport.getWizardPages();
                getWizard().setForcePreviousAndNextButtons( reportPages.size() > 0 );
            }
            public void widgetDefaultSelected( SelectionEvent ev ) {
                widgetSelected( ev );
                getWizard().performFinish();
            }
        });
        
        // descriptionLabel
        desciptionLabel = new Label( content, SWT.WRAP /*| SWT.BORDER*/ );
    }

    
    @Override
    public boolean isPageComplete() {
        return selectedReport != null;
    }


    public void pageChanged( PageChangedEvent ev ) {
        log.info( "pageChanged(): ev= " + ev.getSelectedPage() );
        if (ev.getSelectedPage() == this /*&& editor == null*/) {
            pageEntered();
        }
    }

    
    protected void pageEntered() {
       // getContainer().getShell().setMinimumSize( 700, 500 );
        getContainer().getShell().layout( true );

        setErrorMessage( null );
    }
    
    
    protected String i18n( String key, Object... args ) {
        return Messages.get( "ReportOperation_ChooseReportWizardPage_" + key, args );
    }
    
}

