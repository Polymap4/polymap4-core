/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime;

/**
 * One way to implement a lazilly initialized (cache) variable.
 * <p/>
 * Problem is that each access needs a newly created instance of the Loader. While
 * the code looks good it might be not that efficient.
 *
 * @see LazyInit
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Atomically {

    public static <T,E extends Throwable> T getOrInit( T value, Loader<T,E> loader )
    throws E {
        if (value == null) {
            synchronized (value) {
                if (value == null) {
                    value = loader.load();
                }
            }
        }
        return value;
    }
    
    public static interface Loader<T,E extends Throwable> {
        public T load() throws E;
    }
    
}
