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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class OperationWizardDialog
        extends WizardDialog {

    private static Log log = LogFactory.getLog( OperationWizardDialog.class );
    
    private StatusLine              statusLine;
    
    
    public OperationWizardDialog( Shell parentShell, IWizard newWizard ) {
        super( parentShell, newWizard );
        statusLine = new StatusLine();
    }

    
    public IStatusLineManager getStatusLine() {
        return statusLine;    
    }

    
    /**
     * 
     */
    class StatusLine
            extends StatusLineManager
            implements IStatusLineManager {
        
    }
    
}
