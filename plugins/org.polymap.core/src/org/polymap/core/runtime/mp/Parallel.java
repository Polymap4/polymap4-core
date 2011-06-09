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
package org.polymap.core.runtime.mp;

/**
 * A Parallel processor can process several chunks of elements at a given time.
 * Implementations should be stateles or must be thread-safe and synchronize
 * access to members.
 * <p/>
 * Note that the actual mode of execution depends on the {@link ForEachExecutor} that
 * is used.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Parallel<T,S>
        extends Processor<T,S> {
}