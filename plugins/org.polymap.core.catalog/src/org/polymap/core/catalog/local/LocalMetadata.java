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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableSet;
import org.polymap.core.catalog.IUpdateableMetadata;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Defaults;
import org.polymap.model2.Entity;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalMetadata
        extends Entity
        implements IUpdateableMetadata {

    private static Log log = LogFactory.getLog( LocalMetadata.class );
    
    public static LocalMetadata         TYPE;
    
    @Queryable
    protected Property<String>          identifier;
    
    @Queryable
    protected Property<String>          title;
    
    @Queryable
    @Nullable
    protected Property<String>          description;

    @Queryable
    @Defaults
    protected CollectionProperty<String> keywords;

    @Queryable
    @Nullable
    protected Property<Date>            modified;

    @Queryable
    @Nullable
    protected Property<Date>            created;

    @Defaults
    public CollectionProperty<KeyValue> connectionParams;

    @Queryable
    @Defaults
    protected CollectionProperty<KeyValue> descriptions;
    
    @Queryable
    @Nullable
    protected Property<String>          type;
    
    @Queryable
    @Defaults
    protected CollectionProperty<String> formats;

    @Queryable
    @Defaults
    protected CollectionProperty<String> languages;

    /**
     * 
     */
    public static class KeyValue
            extends Composite {

        @Queryable
        protected Property<String>          key;

        @Queryable
        protected Property<String>          value;

        @Override
        public String toString() {
            return "KeyValue[" + key.get() + ", " + value.get() + "]";
        }
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
    public Optional<String> getDescription() {
        return Optional.ofNullable( description.get() );
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
    public Optional<Date> getModified() {
        return Optional.ofNullable( modified.get() );
    }

    @Override
    public Optional<Date> getCreated() {
        return Optional.ofNullable( created.get() );
    }

    @Override
    public Date[] getAvailable() {
        return new Date[] {};
    }

    @Override
    public Optional<String> getType() {
        return Optional.ofNullable( type.get() );
    }

    @Override
    public IUpdateableMetadata setType( String type ) {
        this.type.set( type );
        return this;
    }

    @Override
    public Set<String> getFormats() {
        return ImmutableSet.copyOf( formats );
    }

    @Override
    public IUpdateableMetadata setFormats( Set<String> formats ) {
        this.formats.clear();
        this.formats.addAll( formats );
        return this;
    }

    @Override
    public Set<String> getLanguages() {
        return ImmutableSet.copyOf( languages );
    }

    @Override
    public IUpdateableMetadata setLanguages( Set<String> langs ) {
        this.languages.clear();
        this.languages.addAll( langs );
        return this;
    }

    @Override
    public Optional<String> getDescription( Field field ) {
        return descriptions.stream()
                .filter( kv -> kv.key.get().equals( field.name() ) )
                .map( kv -> kv.value.get() )
                .findAny();
    }

    @Override
    public IUpdateableMetadata setDescription( Field field, String description ) {
        descriptions.stream()
                .filter( kv -> kv.key.get().equals( field.name() ) ).findAny()
                .orElseGet( () -> descriptions.createElement( proto -> { 
                    proto.key.set( field.name() ); 
                    return proto; } ) )
                .value.set( description );
        return this;
    }

    @Override
    public Map<String,String> getConnectionParams() {
        Map result = new HashMap();
        connectionParams.stream().forEach( kv -> result.put( kv.key.get(), kv.value.get() ) );
        return result;
    }

    @Override
    public IUpdateableMetadata setConnectionParams( Map<String,String> params ) {
        //this.connectionParams.clear();
        assert connectionParams.isEmpty();
        
        params.entrySet().stream().forEach( entry -> 
                connectionParams.createElement( (KeyValue kv) -> {
                        kv.key.set( entry.getKey() );
                        kv.value.set( entry.getValue() );
                        return kv;
                }));
        return this;
    }
    
}
