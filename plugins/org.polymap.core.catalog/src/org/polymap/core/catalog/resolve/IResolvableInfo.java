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

import java.util.Set;

/**
 * Basic information about any resource type that can be resolved by an {@link IMetadataResourceResolver}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IResolvableInfo {

    /**
     * The root {@link IServiceInfo} of this resource.
     */
    public IServiceInfo getServiceInfo();
    
    /**
     * Human readable title representing the service or resource.
     * <p>
     * The title is used to represent the service in the context of a user interface
     * and should make use of the current Locale if possible.
     * </p>
     * 
     * @return title, null if unsupported.
     */
    public String getTitle();

    /**
     * Keywords associated with this service or resource.
     * <p>
     * Maps to the Dublin Core Subject element.
     * 
     * @return keywords associated with this service.
     */
    public Set<String> getKeywords();

    /**
     * Human readable description of this service or resource.
     * <p>
     * This use is understood to be in agreement with "dublin-core", implementors may
     * use either abstract or description as needed.
     * <p>
     * <ul>
     * <li>Dublin Core: <quote> A textual description of the content of the resource,
     * including abstracts in the case of document-like objects or content
     * descriptions in the case of visual resources. </quote> When providing actual
     * dublin-core metadata you can gather up all the description information into a
     * single string for searching.</li>
     * <li>WMS: abstract</li>
     * <li>WFS: abstract</li>
     * <li>shapefile shp.xml information</li>
     * </ul>
     *
     * @return Human readable description, may be null.
     */
    public String getDescription();

}
