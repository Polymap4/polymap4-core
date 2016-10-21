/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.data.wms.catalog;

import java.util.Collections;
import java.util.Set;

import org.geotools.data.ResourceInfo;
import org.geotools.data.ows.Layer;

import com.google.common.collect.Sets;

import org.polymap.core.catalog.resolve.DefaultResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;

/**
 * 
 * 
 *
 * @author Falko Bräutigam
 */
public class WmsResourceInfo
        extends DefaultResourceInfo {

    private Layer       layer;
    
    
    public WmsResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate, Layer layer ) {
        super( serviceInfo, delegate );
        this.layer = layer;
    }
        
    /**
     * {@inheritDoc}
     * <p/>
     * <b>XXX</b> For layers without a name this method returns {@link #getTitle()}.
     * This is not conform to the WMS spec (?) but some stupid WMS seems to expect
     * this behaviour.
     */
    @Override
    public String getName() {
        return super.getName(); //!isBlank( super.getName() ) ? super.getName() : super.getTitle();
    }

    @Override
    public Set<String> getKeywords() {
        // avoid all the additional stuff from delegate
        return layer.getKeywords() != null
                ? Sets.newHashSet( layer.getKeywords() )
                : Collections.EMPTY_SET;
    }
    
}