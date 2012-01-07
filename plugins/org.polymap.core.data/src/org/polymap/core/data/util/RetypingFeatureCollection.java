/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.util;

import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DecoratingFeatureCollection;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.type.FeatureType;

/**
 * This decorator can be used to process/modify the features
 * of the target {@link FeatureCollection}. The abstract method
 * {@link #retype(Feature)} is called to do the actual processing.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RetypingFeatureCollection<T extends FeatureType, F extends Feature>
        extends DecoratingFeatureCollection<T,F> {

    private T                   targetSchema;
    
    
    public RetypingFeatureCollection( FeatureCollection delegate, T targetSchema ) {
        super( delegate );
    }
    
    public T getSchema() {
        return targetSchema;
    }

    public Iterator<F> iterator() {
        return new RetypingIterator( delegate.iterator() );
    }

    public void close( Iterator<F> iterator ) {
        RetypingIterator retyping = (RetypingIterator) iterator;
        delegate.close( retyping.delegateIt );
    }

    public FeatureIterator<F> features() {
        return new DelegateFeatureIterator<F>(this, iterator());
    }

    public void close( FeatureIterator<F> iterator ) {
        ((DelegateFeatureIterator)iterator).close();
    }
    
    
    protected abstract F retype( F feature );
    
    
    /**
     *
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public class RetypingIterator 
            implements Iterator<F> {
        
        private Iterator<F>         delegateIt;
        
        public RetypingIterator( Iterator<F> delegateIt ) {
            this.delegateIt = delegateIt;
        }

        public boolean hasNext() {
            return delegateIt.hasNext();
        }

        public F next() {
            try {
                return retype( delegateIt.next() );
            } 
            catch (IllegalAttributeException e) {
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            delegateIt.remove();
        }
    }

}

