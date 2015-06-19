/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.config;

/**
 * A configuration property. Another name for the same thing: {@link Property2}. This
 * helps to distinguish between model properties and config or Batik context
 * properties.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Config<H,V>
        extends Property2<H,V> {

}
