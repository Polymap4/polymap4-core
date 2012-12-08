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

import org.eclipse.rap.ui.branding.IExitConfirmation;

import org.polymap.core.Messages;
import org.polymap.core.operation.OperationSupport;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BrowserExitConfirmation
        implements IExitConfirmation {

    public BrowserExitConfirmation() {
    }

    public String getExitConfirmationText() {
        return Messages.get( "PolymapWorkbench_exitConfirmation" );
    }

    public boolean showExitConfirmation() {
        return OperationSupport.instance().undoHistorySize() > 0;
    }
    
}
