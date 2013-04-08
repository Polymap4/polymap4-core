/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui.properties;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;

import org.eclipse.core.runtime.IAdapterFactory;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

/**
 * An adapter factory that provides {@link IPropertySourceProvider} for {@link IMap}
 * and {@link ILayer}. Regsitered via <code>org.eclipse.core.runtime.adpaters</code>
 * extension point.
 * <p/>
 * Basically property source providers are responsible of creating a
 * hierarchy of {@link IPropertySource}s for the different element types. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyProviderAdapterFactory
        implements IAdapterFactory {

    private static Log log = LogFactory.getLog( PropertyProviderAdapterFactory.class );
    

    public Object getAdapter( Object adaptable, Class adapter ) {
        if (!IPropertySourceProvider.class.isAssignableFrom( adapter )) {
            return null;
        }
        // IMap
        else if (adaptable instanceof IMap) {
            return new MapPropertySourceProvider();
        }
        // ILayer
        else if (adaptable instanceof ILayer) {
            return new LayerPropertySourceProvider();
        }
        return null;
    }


    public Class[] getAdapterList() {
        return new Class[] {IPropertySourceProvider.class};
    }

    
    /**
     * 
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public static class MapPropertySourceProvider
            implements IPropertySourceProvider {
        
        public IPropertySource getPropertySource( Object obj ) {
            log.trace( "getPropertySource(): " + obj );
            if (obj instanceof IMap) {
                return new MapPropertySource( (IMap)obj );
            }
            else if (obj instanceof ReferencedEnvelope) {
                return new EnvelopPropertySource( (ReferencedEnvelope)obj ); //.setEditable( true );
            }
            else if (obj instanceof IPropertySource) {
                return (IPropertySource)obj;
            }
            return null;
        }
    };


    /**
     * 
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public static class LayerPropertySourceProvider
            implements IPropertySourceProvider {
        
        public IPropertySource getPropertySource( Object obj ) {
            log.trace( "getPropertySource(): " + obj.getClass().getName() );
            if (obj instanceof ILayer) {
                return new LayerPropertySource( (ILayer)obj );
            }
            else if (obj instanceof ReferencedEnvelope) {
                return new EnvelopPropertySource( (ReferencedEnvelope)obj ); //.setEditable( true );
            }
            return null;
        }
    };

}
