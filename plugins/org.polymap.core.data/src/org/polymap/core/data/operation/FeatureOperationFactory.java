/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FeatureOperationFactory {

    private static Log log = LogFactory.getLog( FeatureOperationFactory.class );
    
    private static final FeatureOperationFactory instance = new FeatureOperationFactory();
    
    public static FeatureOperationFactory instance() {
        return instance;    
    }
    
    
    // instance *******************************************
    
    public List<FeatureOperationAction> actionsFor( IFeatureOperationContext context ) {
        List<FeatureOperationAction> result = new ArrayList();
        for (IFeatureOperation op : operationsFor( context )) {
            FeatureOperationAction action = new FeatureOperationAction( op, context );
            result.add( action );
        }
        return result;
    }
    
    
    public List<IFeatureOperation> operationsFor( IFeatureOperationContext context ) {
        List<IFeatureOperation> result = new ArrayList();
        for (FeatureOperationExtension ext : FeatureOperationExtension.all()) {
            IFeatureOperation op = ext.newOperation();
            if (op.init( context )) {
                result.add( op );
            }
        }
        return result;
    }

}
