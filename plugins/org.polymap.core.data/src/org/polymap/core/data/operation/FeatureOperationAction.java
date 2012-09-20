/* 
 * polymap.org
 * Copyright 2011-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.core.commands.ExecutionException;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FeatureOperationAction
        extends Action {

    private static Log log = LogFactory.getLog( FeatureOperationAction.class );

    private IFeatureOperation           op;

    private IFeatureOperationContext    context;
    
    
    public FeatureOperationAction( IFeatureOperation op ) {
        super();
        
        this.op = op;
        this.context = op.getContext();
        
        FeatureOperationExtension ext = context.adapt( FeatureOperationExtension.class );
        setText( ext.getLabel() );
        setToolTipText( ext.getTooltip() );
        ImageDescriptor icon = ext.getIcon();
        if (icon != null) {
            setImageDescriptor( icon );
        }
    }


    public void run() {
        try {
            FeatureOperationContainer container = new FeatureOperationContainer( op, getText() );
            OperationSupport.instance().execute( container, true, true );
        }
        catch (ExecutionException e) {
            log.warn( "", e );
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Operation konnte nicht beendet werden.", e );
        }
    }
    
}
