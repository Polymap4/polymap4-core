/*
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
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
package org.polymap.rhei.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Provides means to handle a set of constant values and synonyms that might be
 * associated with them. This can be used to model a property an entity, where
 * the data store may contain several values to describe one constant.
 * <p>
 * <b>Example implementation:</b>
 * <pre>
 * public class Kennzeichen
 *       extends ConstantWithSynonyms<String> {
 *   public static final Type<String> all = new Type<String>();
 *
 *   public static final Kennzeichen Gebaude = new Kennzeichen( "Gebäude", "gebaude" );
 *   public static final Kennzeichen Digital = new Kennzeichen( "Digital" );
 *
 *   private Kennzeichen( String label, String... synonyms ) {
 *       super( new Random().nextInt(), label, synonyms );
 *       all.add( this );
 *   }
 *
 *   protected String normalizeValue( String value ) {
 *       return value.trim().toLowerCase();
 *   }
 * }
 * </pre>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class ConstantWithSynonyms<L> {

    private static Log log = LogFactory.getLog( ConstantWithSynonyms.class );


    /**
     * One instance of this class is used in subclasses to hold the elements of
     * the set of constants.
     */
    public static class Type<T extends ConstantWithSynonyms,L>
            implements Iterable<T> {

        private List<T>             constants = new ArrayList();

        public void add( T constant ) {
            constants.add( constant );
        }


        /**
         * The constant for the given label or synonym, or null if no such
         * constant exists.
         *
         * @return The constant or null.
         */
        public T forLabelOrSynonym( L labelOrSynonym ) {
            for (ConstantWithSynonyms elm : constants) {
                if (elm.equals( labelOrSynonym )) {
                    return (T)elm;
                }
            }
            return null;
            //throw new NoSuchElementException( "Unknown " + getClass().getSimpleName() + ": " + labelOrSynonym );
        }

        /**
         * The constant for the given label or synonym.
         */
        public T forId( int id ) {
            for (ConstantWithSynonyms elm : constants) {
                if (elm.id == id) {
                    return (T)elm;
                }
            }
            return null;
            //throw new NoSuchElementException( "Unknown " + getClass().getSimpleName() + " id: " + id );
        }

        /**
         *
         */
        public Iterable<L> labels() {
            return new Iterable<L>() {
                public Iterator<L> iterator() {
                    return new Iterator<L>() {
                        Iterator<T> it = constants.iterator();

                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        public L next() {
                            return (L)it.next().label;
                        }

                        public void remove() {
                            throw new UnsupportedOperationException( "plöde, oder was?!");
                        }
                    };
                }
            };

//            ArrayList<T> result = new ArrayList<T>();
//            for (ConstantWithSynonyms elm : constants) {
//                result.add( (T)elm.label );
//            }
//            return result;
        }

        public Iterator<T> iterator() {
            return constants.iterator();
        }
    }


    // instance *******************************************

    public final int    id;

    public L            label;

    public List<L>      synonyms;

    public ConstantWithSynonyms( int id, L label, L... synonyms) {
        this.id = id;
        this.label = label;

        this.synonyms = new ArrayList( synonyms.length + 1 );
        this.synonyms.add( label );
        this.synonyms.addAll( Arrays.asList( synonyms ) );
    }

    public String toString() {
        return label.toString();
    }

    public boolean equals( Object o ) {
        if (o instanceof ConstantWithSynonyms) {
            return this == o || id == ((ConstantWithSynonyms)o).id;
        }
//        if (o instanceof Property) {
//            Property prop = (Property)o;
//            if (prop.t
//        }
        else if (o != null && o.getClass().equals( label.getClass() ) ) {
            L rhs = normalizeValue( (L)o );
            for (L synonym : synonyms) {
                if (normalizeValue( synonym ).equals( rhs )) {
                    return true;
                }
            }
        }
        return false;
    }

    public Filter createFilter( String propName, FilterFactory ff ) {
        List<Filter> synonymFilters = new ArrayList();
        for (L synonym : synonyms) {
            if (synonym instanceof String) {
                synonymFilters.add( ff.like( ff.property( propName ), (String)synonym ) );
            }
            else {
                synonymFilters.add( ff.equals( ff.property( propName ), ff.literal( synonym ) ) );
            }
        }
        return ff.or( synonymFilters );
    }


    /**
     * Subclasses may overwrite in order to normalize values from the data
     * store. For example for Strings <code>trim().toLowerCase()</code> might be
     * performed. The default implementation does nothing.
     * <p>
     * Implementors should check and maybe overwrite
     * {@link #createFilter(String, FilterFactory2)} as well so that both work
     * together.
     */
    protected L normalizeValue( L value ) {
        return value;
    }

}
