/*
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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
 * $Id: $
 */

package org.polymap.core.model;

import java.util.Collection;

import org.qi4j.api.value.ValueComposite;

/**
 * Provides information about and access to properties and associations of an
 * {@link Entity} type.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public interface EntityType<T extends Composite> {

    public String getName();

    public Class<T> getType();

    public Collection<Property> getProperties();

    public Property getProperty( String name );


    /**
     * Information about a property of an {@link EntityType}.
     */
    public interface Property {

        public String getName();

        public Class getType();

        public Object getValue( Composite entity )
        throws Exception;

        public void setValue( Composite entity, Object value )
        throws Exception;

    }

    /**
     * Information about a {@link ValueComposite} property of an {@link EntityType}.
     */
    public interface CompositeProperty
            extends Property {

        public EntityType getCompositeType();

    }

    /**
     * Information about a {@link Collection} property of an {@link EntityType}.
     */
    public interface CollectionProperty
            extends Property {

        /**
         * The complex type of the elements of this collection.
         *
         * @throws IllegalStateException If the colelction has non-complex element type.
         */
        public EntityType getComplexType();


        /**
         * The type of the elements of this collection.
         */
        public Class getType();

    }

    /**
     * Information about an association of an {@link EntityType}.
     */
    public interface Association
            extends Property {

    }

    /**
     * Information about a many-to-one association of an {@link EntityType}.
     */
    public interface ManyAssociation
            extends Property {

    }

}
