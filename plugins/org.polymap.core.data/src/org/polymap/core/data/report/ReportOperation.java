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

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.data.Messages;
import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.operation.OperationWizard;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ReportOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( ReportOperation.class );
    
    private ChooseReportWizardPage      chooseReportPage;

    private IReport                     selectedReport;
    

    @Override
    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), 10 );
        
        // choose report from wizard
        monitor.subTask( "Eingaben vom Nutzer..." );
        IUndoableOperation op = context.adapt( IUndoableOperation.class );
        OperationWizard wizard = new OperationWizard( op, context, monitor ) {
            public boolean doPerformFinish() throws Exception {
                selectedReport = chooseReportPage.getSelectedReport();
                return true;
            }
        };
        
        chooseReportPage = new ChooseReportWizardPage( context, new ReportSite() );
        wizard.addPage( chooseReportPage );
        
        monitor.worked( 1 );

        // execute selected report
        if (OperationWizard.openDialog( wizard )) {
            monitor.worked( 1 );

            IProgressMonitor submon = new SubProgressMonitor( monitor, 8,
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );

            selectedReport.execute( submon );            
            submon.done();
        }
        return Status.OK;
    }

    
    protected String i18n( String key, Object... args ) {
        return Messages.get( "ReportOperation_" + key, args );
    }

    
    /**
     * 
     */
    class ReportSite
            implements IReportSite {
     
        @Override
        public FeatureSource getFeatureSource() throws Exception {
            return context.featureSource();
        }
        
        @Override
        public FeatureCollection getFeatures() throws Exception {
            return context.features();
        }
    }

    
    // ****************************************************
    
    public Status undo( IProgressMonitor monitor ) throws Exception {
        return Status.OK;
    }

    public Status redo( IProgressMonitor monitor ) throws Exception {
        return Status.OK;
    }

    public boolean canExecute() {
        return true;
    }

    public boolean canRedo() {
        return true;
    }

    public boolean canUndo() {
        return true;
    }
}
