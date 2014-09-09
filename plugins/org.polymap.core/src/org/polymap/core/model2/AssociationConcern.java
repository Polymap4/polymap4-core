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
package org.polymap.core.model2;

/**
 * A concern intercepting a {@link Association} value.
 * <p/>
 * Implementations should be thread save. Instances might be instantiated
 * on-demand, so instances cannot hold an internal state.
 * <p/>
 * Impl. Note: This interface does not extend {@link PropertyConcernBase} in order to
 * be able to implemented both, {@link Property} and {@link CollectionProperty}. 
 *
 * @see PropertyConcernBase
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface AssociationConcern<T extends Entity>
        extends Association<T> {

}
