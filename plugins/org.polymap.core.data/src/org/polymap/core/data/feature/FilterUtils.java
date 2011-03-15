/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data.feature;

import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BBOX;

import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FilterUtils {

    private static final FilterFactory  filterFactory = CommonFactoryFinder.getFilterFactory( null );
    

    /**
     * Creates the bounding box filters (one for each geometric attribute)
     * needed to query a <code>MapLayer</code>'s feature source to return just
     * the features for the target rendering extent
     * 
     * @param schema the layer's feature source schema
     * @param attributes set of needed attributes or null.
     * @param bbox the expression holding the target rendering bounding box
     * @return an or'ed list of bbox filters, one for each geometric attribute
     *         in <code>attributes</code>. If there are just one geometric
     *         attribute, just returns its corresponding
     *         <code>GeometryFilter</code>.
     * @throws IllegalFilterException if something goes wrong creating the
     *         filter
     */
    public static Filter createBBoxFilters( SimpleFeatureType schema, 
            String[] attributes, Envelope bbox )
            throws IllegalFilterException {

        if (attributes == null) {
            List<AttributeDescriptor> ats = schema.getAttributeDescriptors();
            int length = ats.size();
            attributes = new String[length];
            for (int t=0; t<length; t++) {
                attributes[t] = ats.get(t).getLocalName();
            }
        }
        
        Filter filter = Filter.INCLUDE;

        for (int j=0; j<attributes.length; j++) {
            AttributeDescriptor attType = schema.getDescriptor( attributes[j] );

            if (attType == null) {
                throw new IllegalFilterException( new StringBuffer( "Could not find '" )
                        .append( attributes[j] + "' in the FeatureType (" )
                        .append( schema.getTypeName() ).append( ")" ).toString() );
            }

            if (attType instanceof GeometryDescriptor) {
                BBOX gfilter = filterFactory.bbox( attType.getLocalName(), 
                        bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), null );

                if (filter == Filter.INCLUDE) {
                    filter = gfilter;
                }
                else {
                    filter = filterFactory.or( filter, gfilter );
                }
            }
        }

        return filter;
    }

}
