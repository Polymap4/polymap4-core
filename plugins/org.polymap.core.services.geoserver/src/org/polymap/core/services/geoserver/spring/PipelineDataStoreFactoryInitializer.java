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
package org.polymap.core.services.geoserver.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.factory.FactoryIteratorProvider;
import org.geotools.factory.GeoTools;

import org.geoserver.data.DataStoreFactoryInitializer;
import org.geoserver.platform.GeoServerResourceLoader;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class PipelineDataStoreFactoryInitializer
        extends DataStoreFactoryInitializer<PipelineDataStoreFactory> {
    
    private static final Log log = LogFactory.getLog( PipelineDataStoreFactoryInitializer.class );

    GeoServerResourceLoader resourceLoader;


    public PipelineDataStoreFactoryInitializer() {
        super( PipelineDataStoreFactory.class );
        log.debug( "..." );
        GeoTools.addFactoryIteratorProvider( new FactoryIteratorProvider() {
            public <T> Iterator<T> iterator( Class<T> category ) {
                if (DataStoreFactorySpi.class.isAssignableFrom( category )) {
                    log.debug( "##### iterator(): category=" + category );
                    List<DataStoreFactorySpi> list = new ArrayList();
                    list.add( new PipelineDataStoreFactory() );
                    return (Iterator<T>)list.iterator();
                }
                else {
                    return null;
                }
            }
        });
    }

    public void setResourceLoader( GeoServerResourceLoader resourceLoader ) {
        this.resourceLoader = resourceLoader;
    }

    public void initialize( PipelineDataStoreFactory factory ) {
        log.debug( "initialize(): factory= " + factory );
    }

}
