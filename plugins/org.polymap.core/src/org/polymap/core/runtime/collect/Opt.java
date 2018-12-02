/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.runtime.collect;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * An {@link Optional} implemantation that allows checked exceptions in
 * {@link #ifPresent(Consumer)}.
 *
 * @author Falko Bräutigam
 */
public class Opt<T> {

    private static final Opt<?>     ABSENT = new Opt<>(null);
    
    public static <U> Opt<U> of( U value ) {
        return new Opt( Objects.requireNonNull( value ) );
    }
    
    public static <U> Opt<U> ofNullable( U value ) {
        return value != null ? new Opt( value ) : absent();
    }
    
    /**
     * Returns an empty instance.  No value is present for this
     * Optional.
     *
     * @apiNote Though it may be tempting to do so, avoid testing if an object
     * is empty by comparing with {@code ==} against instances returned by
     * {@code Option.empty()}. There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code Optional}
     */
    public static <U> Opt<U> absent() {
        Opt<U> result = (Opt<U>)ABSENT;
        return result;
    }

    public static <U> Opt<U> missing() {
        return absent();
    }
    
    // instance *******************************************
    
    private final T         value;
    
    
    protected Opt( T value ) {
        this.value = value;
    }

    public <E extends Exception> void ifPresent( Consumer<T,E> consumer ) throws E {
        if (isPresent()) {
            consumer.accept( value );
        }
    }

    /**
     * If a value is present in this {@code Optional}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value held by this {@code Optional}
     * @throws NoSuchElementException if there is no value present
     *
     * @see Optional#isPresent()
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }


}
