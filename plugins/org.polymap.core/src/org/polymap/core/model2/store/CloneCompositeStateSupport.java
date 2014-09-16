/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.store;

import org.polymap.core.model2.engine.UnitOfWorkNested;

/**
 * Implement this interface to support nested {@link UnitOfWorkNested} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface CloneCompositeStateSupport
        extends StoreUnitOfWork {

    /**
     *
     */
    public CompositeState cloneEntityState( CompositeState state );

    /**
     *
     */
    public void reincorparateEntityState( CompositeState state, CompositeState clonedState );

}
