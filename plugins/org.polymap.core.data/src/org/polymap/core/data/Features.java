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
package org.polymap.core.data;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.io.Closeable;
import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static helpers to work with {@link Feature} and the glorios
 * {@link FeatureCollection}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Features {

    private static Log log = LogFactory.getLog( Features.class );

    
    /**
     * A real {@link Iterable} of {@link Feature}s with a {@link Closeable} interface.
     */
    public interface ClosableIterable<F extends Feature>
            extends Iterable<F>, Closeable {
    }
    
    
    /**
     * Create a real {@link Iterable} from the result of
     * {@link FeatureCollection#features()}. Use this in a try-with statement in
     * order to make sure that the underlying {@link FeatureIterator} gets closed
     * properly.
     */
    public static <T extends FeatureType, F extends Feature> ClosableIterable<F> iterable( FeatureCollection<T,F> fc ) {
        return new ClosableIterable() {
            
            private FeatureIterator<F>      it = fc.features();
            
            @Override
            public Iterator<F> iterator() {
                return new Iterator<F>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public F next() {
                        return it.next();
                    }
                };
            }

            @Override
            public void close() throws IOException {
                if (it != null) {
                    it.close();
                    it = null;
                }
            }

            @Override
            protected void finalize() throws Throwable {
                close();
            }
        };
    }
    

    /**
     * Experimental: the underlying {@link FeatureIterator} is closed when the STream
     * is finalised. There is no way to it from client code.
     */
    public static <T extends FeatureType, F extends Feature> Stream<F> stream( FeatureCollection<T,F> fc ) {
        return StreamSupport.stream( iterable( fc ).spliterator(), false );
    }
    
}
