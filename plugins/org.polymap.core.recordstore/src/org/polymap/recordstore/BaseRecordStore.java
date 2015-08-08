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
package org.polymap.recordstore;

/**
 * Provides common base methods.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BaseRecordStore
        implements IRecordStore {

    private IRecordFieldSelector        indexFieldSelector = IRecordFieldSelector.ALL;

    
    public IRecordFieldSelector getIndexFieldSelector() {
        return indexFieldSelector;
    }

    public void setIndexFieldSelector( IRecordFieldSelector indexFieldSelector ) {
        this.indexFieldSelector = indexFieldSelector;
    }
    
}
