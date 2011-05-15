/*
 * polymap.org
 * Copyright 2009-2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.core.model;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public interface Entity
        extends Composite {

    public String id();

    /**
     * Last commit timestamp of the entity.
     */
    public long lastModified();

    /**
     * The user who last commited changes.
     */
    public String lastModifiedBy();

    public EntityType getEntityType();

}
