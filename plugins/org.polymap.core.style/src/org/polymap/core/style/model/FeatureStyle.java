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
package org.polymap.core.style.model;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ModelRuntimeException;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Describes the visual representation (aka the style) of a layer and its data. This
 * is <b>not tied</b> to any particular backend (SLD, OpenLayers). It rather models
 * the "user experience" we want to achieve.
 * <p/>
 * Every instance is created/loaded inside its own {@link UnitOfWork}. See also
 * {@link StyleRepository}.
 * <p/>
 * The name is misleading and will be changed in future versions. It should be
 * MapStyle.
 *
 * @author Falko Bräutigam
 */
public class FeatureStyle
        extends Entity {

    public static FeatureStyle          TYPE;
    

    /**
     * Initializes a newly created instance with default values.
     */
    static ValueInitializer<FeatureStyle> defaults( StyleRepository repo, UnitOfWork uow ) {
        return (FeatureStyle proto) -> {
            assert uow != null;
            proto.uow = uow;
            proto.repo = repo;
            proto.styles.createValue( StyleGroup.defaults );
            return proto;
        };
    }

    
    // instance *******************************************
    
    /**
     * The root group of styles.
     */
    public Property<StyleGroup>         styles;

    protected StyleRepository           repo;
    
    protected UnitOfWork                uow;
    
    
    @Override
    public String id() {
        return (String)super.id();
    }

    @Override
    protected void finalize() throws Throwable {
        if (uow != null) {
            uow.close();
            uow = null;
        }
    }

    /**
     * 
     *
     * @throws ModelRuntimeException
     * @throws {@link ConcurrentEntityModificationException} If another party (thread
     *         or session) has store another version.
     */
    public void store() throws ModelRuntimeException {
        uow.commit();
        repo.updated( this );
        EventManager.instance().publish( new FeatureStyleCommitedEvent( this ) );
    }

    /**
     * Shortcut to <code>styles.get().members</code>.
     */
    public CollectionProperty<Style> members() {
        return styles.get().members;
    }
    
}
