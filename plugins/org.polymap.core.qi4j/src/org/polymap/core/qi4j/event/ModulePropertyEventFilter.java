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
package org.polymap.core.qi4j.event;

import java.util.EventObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.QiModule;

/**
 * Filter events that are fired by entities of the given module.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class ModulePropertyEventFilter
        implements IEventFilter {

    private static Log log = LogFactory.getLog( ModulePropertyEventFilter.class );

    private QiModule            module;


    public ModulePropertyEventFilter( QiModule module ) {
        this.module = module;
    }

    public boolean accept( EventObject ev ) {
        QiEntity entity = (QiEntity)ev.getSource();
        try {
            module.findEntity( entity.getCompositeType(), entity.id() );
            return true;
        }
        catch (NoSuchEntityException e) {
            return false;
        }
    }
    
}
