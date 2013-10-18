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

import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureCollectionContentProvider
        implements IFeatureContentProvider {

    private static Log log = LogFactory.getLog( FeatureCollectionContentProvider.class );

    private FeatureCollection           coll;


    public FeatureCollectionContentProvider( FeatureCollection delegate ) {
        this.coll = delegate;
    }


    @Override
    public Object[] getElements( Object input ) {
        try {
            final Object[] result = new Object[ coll.size() ];
            coll.accepts( new FeatureVisitor() {
                int i = 0;
                public void visit( Feature feature ) {
                    result[i++] = new FeatureTableElement( feature );
                }
            }, new NullProgressListener() );
            return result;
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
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
