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

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;

/**
 * Creates an {@link IServiceInfo} service out of connection params provided by an
 * {@link IMetadata} instance. It is the link between the matedata and the data.
 * <p/>
 * Resolving a backend service instance from a layer contains the following step.
 * <ul>
 * <li>ILayer provides UID of the {@link IMetadata} entry in the catalog</li>
 * <li>{@link IMetadata} provides connection params</li>
 * <li>{@link IMetadataResourceResolver}s are checked if one can resolve the
 * connection params</li>
 * <li>one {@link IMetadataResourceResolvers} build {@link IServiceInfo} out of
 * connection params</li>
 * <li>{@link IServiceInfo#createService()} creates the actual backend service
 * instance</li>
 * <li>ILayer also provides resource name which allows to scan all resources of the
 * service and find the right one for that layer</li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMetadataResourceResolver {

    /**
     * The param that identifies the type of the service coupled with this metadata.
     */
    public static final String      CONNECTION_PARAM_TYPE = "_type_";
    public static final String      CONNECTION_PARAM_URL = "_url_";

    /**
     * 
     *
     * @param params
     */
    public boolean canResolve( IMetadata metadata );

    /**
     * 
     * <p/>
     * This usually <b>blocks</b> execution until backend service is available and/or connected.
     * 
     * @param params
     * @param monitor
     */
    public IResolvableInfo resolve( IMetadata metadata, IProgressMonitor monitor ) throws Exception;
    
    /**
     * 
     *
     * @param service
     */
    public Map<String,String> createParams( Object service );
    
}
