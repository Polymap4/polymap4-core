/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.data.entitystore.lucene;

import java.util.Iterator;
import java.util.NoSuchElementException;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class LuceneManyAssociationState
        implements ManyAssociationState, Serializable {

    private static final Log log = LogFactory.getLog( LuceneManyAssociationState.class );

    private LuceneEntityState   entityState;

    private JSONArray           references;
    
    private String              fieldName;


    public LuceneManyAssociationState( LuceneEntityState entityState, String fieldName, JSONArray references ) {
        this.entityState = entityState;
        this.references = references;
        this.fieldName = fieldName;
    }


    public String getFieldName() {
        return fieldName;
    }


    public int count() {
        return references.length();
    }


    public boolean contains( EntityReference entityReference ) {
        try {
            for (int i=0; i<references.length(); i++) {
                if (references.get( i ).equals( entityReference.identity() )) {
                    return true;
                }
            }
            return false;
        }
        catch (JSONException e) {
            throw new EntityStoreException( e );
        }
    }


    public boolean add( int idx, EntityReference entityReference ) {
        try {
            if (contains( entityReference )) {
                return false;
            }

            // _p3: insert() is not available
            references.put( idx, entityReference.identity() );
            entityState.setManyAssociation( this, references );
            return true;
        }
        catch (JSONException e) {
            throw new EntityStoreException( e );
        }
    }


    public boolean remove( EntityReference entityReference ) {
        try {
            for (int i = 0; i < references.length(); i++) {
                if (references.get( i ).equals( entityReference.identity() )) {
                    references.remove( i );
                    entityState.setManyAssociation( this, references );
                    return true;
                }
            }
            return false;
        }
        catch (JSONException e) {
            throw new EntityStoreException( e );
        }
    }


    public EntityReference get( int i ) {
        try {
            return new EntityReference( references.getString( i ) );
        }
        catch (JSONException e) {
            throw new EntityStoreException( e );
        }
    }


    public Iterator<EntityReference> iterator() {
        
        return new Iterator<EntityReference>() {

            int index = 0;

            public boolean hasNext() {
                return index < references.length();
            }

            public EntityReference next() {
                try {
                    EntityReference ref = new EntityReference( references.getString( index ) );
                    index++;
                    return ref;
                }
                catch (JSONException e) {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException( "Use ManyAssociation.remove() instead." );
            }
        };
    }

}
