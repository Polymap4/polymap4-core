/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ops;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Creates a new {@link ILayer} inside the given {@link #uow UnitOfWork}.
 * <p/>
 * This might open dialogs and must not be executed with progress dialog.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewLayerOperation
        extends TwoPhaseCommitOperation {

    /**
     * Inbound: The UnitOfWork to work with.
     */
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,UnitOfWork>    uow;
    
    /** Inbound: the {@link IMap} to create new layer for. */
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,IMap>          map;
    
    /** Inbound:  */
    public Config2<NewLayerOperation,ValueInitializer<ILayer>>  initializer;
    
    /** Outbound: Newly created layer. */
    @Immutable
    public Config2<NewLayerOperation,ILayer>        layer;


    public NewLayerOperation() {
        super( "New layer" );
        
        // initialize with empty initializer so that sub-classes and concers can chain away
        initializer.set( (ILayer proto) -> proto );
    }


    @Override
    public IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        assert map.get().belongsTo( uow.get() );
        
        monitor.beginTask( getLabel(), 5 );
        register( uow.get() );

        // create entity
        ILayer newLayer = uow.get().createEntity( ILayer.class, null, (ILayer proto) -> {
            initializer.get().initialize( proto );
            
            assert proto.label.get() != null;
            assert proto.resourceIdentifier.get() != null;

            proto.parentMap.set( map.get() );
            proto.orderKey.set( proto.maxOrderKey() + 1  );
            return proto;
        });
        layer.set( newLayer );
        
        assert map.get().layers.contains( newLayer );
//        map.get().layers.add( layer.get() );
//        // force commit (https://github.com/Polymap4/polymap4-model/issues/6)
//        map.get().label.set( map.get().label.get() );

        return Status.OK_STATUS;
    }

}
