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
package org.polymap.core.runtime.cache;

/**
 * Values stored in a {@link Cache} can implement this interface in order to listen
 * to eviction events and perform some cleanup operations.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface EvictionAware {

    /**
     * 
     * <p/>
     * The returned object <b>must not<b/> reference the value in any way. So it cannot
     * be an inner class of the value! The listener should just reference the resources
     * that needs to be cleaned up.
     *
     * @return Newly created listener for this element.
     */
    public EvictionListener newListener();

}
