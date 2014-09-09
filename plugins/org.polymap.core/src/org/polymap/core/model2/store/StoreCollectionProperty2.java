/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.store;

import java.util.Collection;

/**
 * <p/>
 * @deprecated Not yet supported.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface StoreCollectionProperty2<T>
        extends StoreCollectionProperty<T> {

    public boolean addAll( Collection<T> c );

    public boolean removeAll( Collection<T> c );

    public boolean retainAll( Collection<T> c );

}
