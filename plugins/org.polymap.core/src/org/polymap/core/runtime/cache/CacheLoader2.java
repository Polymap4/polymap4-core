/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.cache;

/**
 * A loader that does not throw a checked exception. This allows to use a lambda
 * expression for loaders that does not throw a checked exception.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface CacheLoader2<K,V>
        extends CacheLoader<K,V,RuntimeException> {

}
