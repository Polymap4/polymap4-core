/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.query;

import com.google.common.collect.Lists;

import org.polymap.core.model2.Association;
import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.engine.TemplateProperty;
import org.polymap.core.model2.engine.TemplateInstanceBuilder;
import org.polymap.core.model2.query.grammar.AssociationEquals;
import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.model2.query.grammar.ComparisonPredicate;
import org.polymap.core.model2.query.grammar.Conjunction;
import org.polymap.core.model2.query.grammar.Disjunction;
import org.polymap.core.model2.query.grammar.IdPredicate;
import org.polymap.core.model2.query.grammar.Negation;
import org.polymap.core.model2.query.grammar.PropertyEquals;
import org.polymap.core.model2.query.grammar.PropertyMatches;
import org.polymap.core.model2.query.grammar.PropertyNotEquals;
import org.polymap.core.model2.runtime.EntityRepository;

/**
 * Static factory methods to create query expressions. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Expressions {

    public static Conjunction and( BooleanExpression first, BooleanExpression second, BooleanExpression... more ) {
        return new Conjunction( Lists.asList( first, second, more ).toArray( new BooleanExpression[2+more.length] ) );                                                                               
    }
    
    public static Disjunction or( BooleanExpression first, BooleanExpression second, BooleanExpression... more) {
        return new Disjunction( Lists.asList( first, second, more ).toArray( new BooleanExpression[2+more.length] ) );                                                                               
    }
    
    public static <T> PropertyEquals<T> eq( Property<T> prop, T value ) {
        return new PropertyEquals( (TemplateProperty)prop, value );
    }

    public static <T> PropertyNotEquals<T> notEq( Property<T> prop, T value ) {
        return new PropertyNotEquals( (TemplateProperty)prop, value );
    }
    
    public static <T> Negation not( BooleanExpression expression ) {
        return new Negation( expression );
    }
    
    /**
     * Checks the value to see if it matches the specified wildcard matcher, always
     * testing case-sensitive.
     * <p/>
     * The wildcard matcher uses the characters '?' and '*' to represent a single or
     * multiple wildcard characters. This is the same as often found on Dos/Unix
     * command lines. The check is case-sensitive always.
     */
    public static <T> PropertyMatches<T> matches( Property<T> prop, T value ) {
        return new PropertyMatches( (TemplateProperty)prop, value );
    }
    
    /**
     * True if there is at least one element in the collection that fullfils the
     * given sub expression.
     */
    public static <T> void any( CollectionProperty<T> prop, ComparisonPredicate<T> sub ) {
        throw new RuntimeException( "not yet implemented" );
    }
    
    /**
     * True if all elements of the collection fulfill the given sub expression.
     */
    public static <T> void all( CollectionProperty<T> prop, ComparisonPredicate<T> sub ) {
        throw new RuntimeException( "not yet implemented" );
    }
    
    public static <T extends Composite> void any( CollectionProperty<T> prop, BooleanExpression sub ) {
        throw new RuntimeException( "not yet implemented" );
    }
    
    public static <T extends Composite> void all( CollectionProperty<T> prop, BooleanExpression sub ) {
        throw new RuntimeException( "not yet implemented" );
    }
    
    public static <T extends Entity> AssociationEquals<T> is( Association<T> assoc, T entity ) {
        return new AssociationEquals( (TemplateProperty)assoc, id( entity ) );
    }

    public static <T extends Entity> AssociationEquals<T> is( Association<T> assoc, BooleanExpression sub ) {
        return new AssociationEquals( (TemplateProperty)assoc, sub );
    }

    public static <T extends Entity> IdPredicate<T> id( Object id ) {
        return new IdPredicate( id );
    }

    public static <T extends Entity> IdPredicate<T> id( T entity ) {
        return new IdPredicate( entity.id() );
    }

    public static <T extends Composite> T template( Class<T> type, EntityRepository repo ) {
        return new TemplateInstanceBuilder( repo ).newComposite( type );
    }
    
    public static <T extends Composite> T template( CollectionProperty<T> prop, EntityRepository repo ) {
        Class<T> type = prop.getInfo().getType();
        return new TemplateInstanceBuilder( repo ).newComposite( type );
    }
    
}
