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

import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * Information about a resource in the context of an {@link IServiceInfo}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IResourceInfo
        extends IResolvableInfo {

    /**
     * Returns the name of this resource within the context of its service.
     * <p/>
     * Known mappings:
     * <ul>
     * <li>WFS typeName
     * <li>Database table name
     * <li>WMS layer name
     * <li>level of a grid coverage
     * </ul>
     * 
     * The name should be unique within the context of a single Service.
     * 
     * @return name of this resource
     */
    public String getName();

    public ReferencedEnvelope getBounds();


//    /**
//     * A namespace, in the form of a {@code URI}, used to identify the resource type.
//     * <p/>
//     * Known Mappings:
//     * <ul>
//     * <li>Dublin Code Format element
//     * <li>WFS DescribeFeatureType URL
//     * <li>file.toURI()
//     * <li>XML namespace
//     * <li>URL
//     * </ul>
//     * 
//     * @return namespace, used with getName() to identify resource type
//     */
//    public URI getSchema();

}
