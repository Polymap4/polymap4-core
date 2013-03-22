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

import org.eclipse.core.runtime.IAdaptable;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureOperationFactory {

    private static Log log = LogFactory.getLog( FeatureOperationFactory.class );

    /**
     * 
     */
    public interface IContextProvider {

        /**
         * Creates a new context in the given environment and for the
         * {@link #currentSelection()}.
         * 
         * @return A new context, or null it there is nothing to contribute.
         */
        public DefaultOperationContext newContext();

    }

    
    public static FeatureOperationFactory forContext( IContextProvider provider ) {
        return new FeatureOperationFactory( provider );    
    }


    // instance *******************************************
    
    private IContextProvider provider;


    public FeatureOperationFactory( IContextProvider provider ) {
        this.provider = provider;
    }


    public List<FeatureOperationAction> actions() {
        List<FeatureOperationAction> result = new ArrayList();
        for (IFeatureOperation op : operations()) {
            FeatureOperationAction action = new FeatureOperationAction( op );
            result.add( action );
        }
        return result;
    }
    
    
    public List<IFeatureOperation> operations() {
        List<IFeatureOperation> result = new ArrayList();
        for (final FeatureOperationExtension ext : FeatureOperationExtension.all()) {
            
            IFeatureOperation op = ext.newOperation();
            DefaultOperationContext context = provider.newContext();
            
            if (context != null && op.init( context )) {
                result.add( op );
                
                // adapt to FeatureOperationExtension
                context.addAdapter( new IAdaptable() {
                    public Object getAdapter( Class adapter ) {
                        if (adapter.equals( FeatureOperationExtension.class )) {
                            return ext;
                        }
                        return null;
                    }
                });
            }
        }
        return result;
    }

}
