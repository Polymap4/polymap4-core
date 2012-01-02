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
package org.polymap.core.project;

import org.polymap.core.project.model.MapState;

/**
 * Tagging interface for temporary layers.
 * <p/>
 * XXX The current implementation handles {@link ITempLayer} explicitly in
 * {@link MapState}. Check if a more general mechanism could be introduced to handle
 * temporar/non-persistent parts of the model.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ITempLayer
        extends ILayer {

}
