/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.catalog.local;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.polymap.core.catalog.IUpdateableMetadata;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Defaults;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.runtime.config.Mandatory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalMetadata
        extends Entity
        implements IUpdateableMetadata {

    public static LocalMetadata         TYPE;
    
    @Mandatory
    @Queryable
    protected Property<String>          identifier;
    
    @Queryable
    protected Property<String>          title;
    
    @Queryable
    @Defaults
    protected Property<String>          description;

    @Queryable
    @Defaults
    protected CollectionProperty<String> keywords;

    @Defaults
    public CollectionProperty<KeyValue> connectionParams;

    /**
     * 
     */
    public static class KeyValue
            extends Composite {

        @Queryable
        protected Property<String>          key;

        @Queryable
        protected Property<String>          value;
    }
    
    @Override
    public String getIdentifier() {
        return identifier.get();
    }

    @Override
    public IUpdateableMetadata setIdentifier( String identifier ) {
        this.identifier.set( identifier );
        return this;
    }

    @Override
    public String getTitle() {
        return title.get();
    }

    @Override
    public IUpdateableMetadata setTitle( String title ) {
        this.title.set( title );
        return this;
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public IUpdateableMetadata setDescription( String description ) {
        this.description.set( description );
        return this;
    }

    @Override
    public Set<String> getKeywords() {
        return keywords.stream().collect( Collectors.toSet() );
    }
    
    @Override
    public IUpdateableMetadata setKeywords( Set<String> keywords ) {
        this.keywords.clear();
        this.keywords.addAll( keywords );
        return this;
    }

    @Override
    public Map<String,String> getConnectionParams() {
        return connectionParams.stream().collect( Collectors.toMap( kv -> kv.key.get(), kv -> kv.value.get() ) );
    }

    @Override
    public IUpdateableMetadata setConnectionParams( Map<String,String> params ) {
        this.connectionParams.clear();
        params.entrySet().stream().forEach( entry -> 
                connectionParams.createElement( (KeyValue kv) -> {
                        kv.key.set( entry.getKey() );
                        kv.value.set( entry.getValue() );
                        return kv;
                }));
        return this;
    }
    
}
