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
package org.polymap.rhei.data.entityfeature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.value.ValueComposite;

import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.data.ui.featuretable.IFeatureContentProvider;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.Composite;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

/**
 * Used to display {@link Entity} collections as result of a {@link Query}, or the
 * contents of a collection property that contains {@link Composite} instances (
 * {@link ValueComposite}).
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class CompositesFeatureContentProvider
        implements IFeatureContentProvider {

    private static Log log = LogFactory.getLog( CompositesFeatureContentProvider.class );

    private Iterable<? extends Composite>   composites;

    private EntityType                      compositeType;


    public CompositesFeatureContentProvider() {
    }


    public CompositesFeatureContentProvider(
            Iterable<? extends Composite> composites,
            EntityType<? extends Composite> compositeType) {
        this.composites = composites;
        this.compositeType = compositeType;
    }


    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        this.composites = (Iterable<? extends Composite>)newInput;
    }


    public Object[] getElements( Object input ) {
        log.debug( "getElements(): input=" + input.getClass().getName() );
        List<IFeatureTableElement> result = new ArrayList();
        for (final Composite composite : composites) {
            result.add( new FeatureTableElement( composite ) );
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

        private Composite       composite;


        protected FeatureTableElement( Composite composite ) {
            this.composite = composite;
        }
        
        public Composite getComposite() {
            return composite;
        }

        public Object getValue( String name ) {
            try {
                return compositeType.getProperty( name ).getValue( composite );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        public void setValue( String name, Object value ) {
            try {
                compositeType.getProperty( name ).setValue( composite, value );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        public String fid() {
            if (composite instanceof Entity) {
                return ((Entity)composite).id();
            }
            else {
                throw new RuntimeException( "Don't know how to build fid out of: " + composite );
            }
        }

    }

}
