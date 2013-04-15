/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.workbench;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.rap.ui.branding.IExitConfirmation;

import org.polymap.core.Messages;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.SessionSingleton;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BrowserExitConfirmation
        implements IExitConfirmation {

    private static final Log log = LogFactory.getLog( BrowserExitConfirmation.class );

    
    /**
     * Calling undoHistorySize() right in
     * {@link BrowserExitConfirmation#showExitConfirmation()} causes race cond and
     * concurrent modification exception in undo history. The fix this a operation
     * history listener is installed for every session.
     */
    static class Session
            extends SessionSingleton
            implements IOperationHistoryListener {

        boolean             dirty;
        
        public Session() {
            OperationSupport.instance().addOperationHistoryListener( this );
        }

        @Override
        public void historyNotification( OperationHistoryEvent ev ) {
            dirty = OperationSupport.instance().undoHistorySize() > 0;
        }

    }
    
    public BrowserExitConfirmation() {
    }

    @Override
    public String getExitConfirmationText() {
        return Messages.get( "PolymapWorkbench_exitConfirmation" );
    }

    @Override
    public boolean showExitConfirmation() {
        return Session.instance( Session.class ).dirty;
    }
    
}
