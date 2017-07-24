/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process;

import java.util.List;
import java.util.Optional;

/**
 * Allows to provide multiple processing modules to the system.
 *
 * @author <a href="http://mapzone.io">Falko Bräutigam</a>
 */
public interface ModuleProvider {

    public List<ModuleInfo> createModuleInfos();
    
    public Optional<ModuleInfo> findModuleInfo( Object module );
}
