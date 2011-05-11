/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.operation;

import java.util.concurrent.atomic.AtomicInteger;

import net.refractions.udig.core.internal.CorePlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * This wizard can be used by operations to gather information from the user and/or
 * display results. The execute() method should look like:
 * <pre>
 *       OperationWizard wizard = new OperationWizard( this, info, monitor ) {
 *           public boolean doPerformFinish() throws Exception {
 *               // do the operation work
 *            }
 *       };
 *       wizard.addPage( ... );
 *       return OperationWizard.openDialog( wizard );
 * </pre>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class OperationWizard
        extends Wizard
        implements IWizard, IPageChangeProvider {

    private static Log log = LogFactory.getLog( OperationWizard.class );


    /**
     * Creastes and opens a new dialog for the given wizard.
     * <p/>
     * This method blocks until the wizard dialogs OK button is pressed ot dialog
     * is canceled.
     *
     * @param wizard
     * @return True if the OK button of the wizard was pressed.
     */
    public static boolean openDialog( final OperationWizard wizard ) {
        final AtomicInteger returnCode = new AtomicInteger( -1 );

        wizard.getDisplay().syncExec( new Runnable() {

            public void run() {
                Shell shell = PolymapWorkbench.getShellToParentOn();
                OperationWizardDialog dialog = new OperationWizardDialog( shell, wizard );
                dialog.setBlockOnOpen( true );
                returnCode.set( dialog.open() );
            }
        });
        return returnCode.get() == Window.OK;
    }


    // instance *******************************************

    private IUndoableOperation      operation;

    private IAdaptable              operationInfo;

    private IProgressMonitor        monitor;


    protected OperationWizard( IUndoableOperation operation, IAdaptable operationInfo, IProgressMonitor monitor ) {
        super();
        this.operation = operation;
        this.operationInfo = operationInfo;
        this.monitor = monitor;
    }

    public IUndoableOperation getOperation() {
        return operation;
    }

    public IAdaptable getOperationInfo() {
        return operationInfo;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public Display getDisplay() {
        return (Display)operationInfo.getAdapter( Display.class );
    }

    public IStatusLineManager getStatusLine() {
        return ((OperationWizardDialog)getContainer()).getStatusLine();
    }

    public void addPageChangedListener( IPageChangedListener listener ) {
        ((OperationWizardDialog)getContainer()).addPageChangedListener( listener );
    }

    public void removePageChangedListener( IPageChangedListener listener ) {
        ((OperationWizardDialog)getContainer()).removePageChangedListener( listener );
    }

    public Object getSelectedPage() {
        return ((OperationWizardDialog)getContainer()).getSelectedPage();
    }

    public boolean performFinish() {
        try {
            return doPerformFinish();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( CorePlugin.ID, this, "Error while executing this operation.", e );
            return false;
        }
    }

    protected abstract boolean doPerformFinish()
    throws Exception;


    /**
     *
     */
    class ResultPage
            extends WizardPage {

        public static final String ID = "OperationWizard.ResultPage";


        protected ResultPage() {
            super( ID );
        }

        public void createControl( Composite parent ) {
        }

    }

}
