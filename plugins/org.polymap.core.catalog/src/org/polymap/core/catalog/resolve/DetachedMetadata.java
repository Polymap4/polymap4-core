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
package org.polymap.core.catalog.resolve;

import java.util.Map;
import java.util.Set;

import org.polymap.core.catalog.IMetadata;

/**
 * Provides a 'detached' metadata that is used to resolve any connections params.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DetachedMetadata
        implements IMetadata {

    private Map<String,String>      connectionParams;
    
    
    public DetachedMetadata( Map<String,String> connectionParams ) {
        this.connectionParams = connectionParams;
    }

    @Override
    public String getIdentifier() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public String getTitle() {
        return "Detached";
    }

    @Override
    public String getDescription() {
        return "Detached metadata without a catalog.";
    }

    @Override
    public Set<String> getKeywords() {
        return null;
    }

    @Override
    public Map<String,String> getConnectionParams() {
        return connectionParams;
    }
    
}
