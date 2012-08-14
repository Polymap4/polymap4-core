/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
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

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;

/**
 * Provides the Features of an {@link Iterable}.
 * <p/>
 * Unfortunatelly {@link FeatureCollection} is not an {@link Iterable} so we need 2
 * separate implementations to support CollectionIterable and FeatureCollection.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CollectionContentProvider
        implements IFeatureContentProvider {

    private static Log log = LogFactory.getLog( CollectionContentProvider.class );

    private Iterable<? extends Feature>     features;

    private FeatureType                     schema;


    public CollectionContentProvider() {
    }


    public CollectionContentProvider( Iterable<? extends Feature> features, FeatureType schema ) {
        this.features = features;
        this.schema = schema;
    }


    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        this.features = (Iterable<? extends Feature>)newInput;
    }


    public Object[] getElements( Object input ) {
        log.debug( "getElements(): input=" + input.getClass().getName() );
        List<IFeatureTableElement> result = new ArrayList();
        for (final Feature feature : features) {
            result.add( new FeatureTableElement( feature ) );
        }
        return result.toArray();
    }

    
    public void dispose() {
    }


    /**
     *
     */
    public class FeatureTableElement
            implements IFeatureTableElement {

        private Feature         feature;


        protected FeatureTableElement( Feature feature ) {
            this.feature = feature;
        }
        
        public Feature getFeature() {
            return feature;
        }

        public Object getValue( String name ) {
            return feature.getProperty( name ).getValue();
        }

        public void setValue( String name, Object value ) {
            feature.getProperty( name ).setValue( value );
        }

        public String fid() {
            return feature.getIdentifier().getID();
        }

    }

}
