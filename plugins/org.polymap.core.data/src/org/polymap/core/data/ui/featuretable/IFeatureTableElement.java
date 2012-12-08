/*
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.ui.featuretable;

import org.opengis.feature.Feature;

/**
 * This is the content element of a {@link FeatureTableViewer}.
 * {@link IFeatureContentProvider} provides elements of this type.
 * <p/>
 * The content element are not plain {@link Feature} instances in order to let the
 * feature table handle any combinations of attributes, including complex attributes
 * or even multiple features in one table.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IFeatureTableElement {

    public String fid();
    
    public Object getValue( String name );

    /**
     * Implement to support table in-place editing
     *
     * @param name
     * @param value
     */
    public void setValue( String name, Object value );

}
