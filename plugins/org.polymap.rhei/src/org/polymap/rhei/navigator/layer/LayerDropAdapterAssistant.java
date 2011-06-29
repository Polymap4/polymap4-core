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
package org.polymap.rhei.navigator.layer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerDropAdapterAssistant
        extends CommonDropAdapterAssistant {

    private static Log log = LogFactory.getLog( LayerDropAdapterAssistant.class );


    public LayerDropAdapterAssistant() {
    }


    public IStatus handleDrop( CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent,
            Object target ) {
        log.info( "handleDrop(): target=" + target );
        return Status.CANCEL_STATUS;
    }


    public IStatus validateDrop( Object target, int operation, TransferData transferType ) {
        log.info( "validateDrop(): target=" + target );
        return Status.CANCEL_STATUS;
    }
    
}
