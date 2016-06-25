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
package org.polymap.core.catalog;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.polymap.core.catalog.resolve.IMetadataResourceResolver;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMetadata {

    public String getIdentifier();
    
    public String getTitle();
    
    public String getDescription();
    
    public Date getModified();
    
    public Set<String> getKeywords();

    /**
     * 
     * @see IMetadataResourceResolver#CONNECTION_PARAM_TYPE
     * @see IMetadataResourceResolver#CONNECTION_PARAM_URL
     */
    public Map<String,String> getConnectionParams();
    
}
