/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j.security;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;

/**
 * Filters the return values of assocations regarding the READ permission
 * of the principal of the current session.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ACLFilterConcern
        extends GenericConcern {

    private static Log log = LogFactory.getLog( ACLFilterConcern.class );

    @This ACL               composite;
    
    @Structure Module       _module;

    
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable {                

        // call underlying
        Object result = next.invoke( proxy, method, args );
        
        // filter result
        if (Association.class.isAssignableFrom( method.getReturnType() )) {
            Association assoc = (Association)result;
            return (org.polymap.core.model.security.ACL.class.isAssignableFrom( (Class)assoc.type() ))
                    ? new FilteredAssociation( assoc )
                    : result;
        }
        else if (ManyAssociation.class.isAssignableFrom( method.getReturnType() )) {
            ManyAssociation assoc = (ManyAssociation)result;
            return (org.polymap.core.model.security.ACL.class.isAssignableFrom( (Class)assoc.type()))
                    ? new FilteredManyAssociation( assoc )
                    : result;
        }
        else {
            return result;
        }
    }

    
    /**
     * 
     */
    protected static class FilteredManyAssociation
            implements ManyAssociation {
    
        private ManyAssociation     delegate;

        
        protected FilteredManyAssociation( ManyAssociation delegate ) {
            super();
            this.delegate = delegate;
        }

        protected Object filter( Object entity ) {
            org.polymap.core.model.security.ACL result = (org.polymap.core.model.security.ACL)entity;
            return ACLUtils.checkPermission( result, AclPermission.READ, false )
                    ? result : null;
        }
        
        public boolean contains( Object entity ) {
            return delegate.contains( entity )
                    && filter( entity ) != null;
        }

        public int count() {
            int i = 0;
            for (Iterator it=iterator(); it.hasNext(); i++) {
                it.next();
            }
            return i;
        }

        public Object get( int index ) {
            // XXX if frequently called, this needs optimization
            Iterator it = iterator();
            int i = 0;
            for (; it.hasNext() && i<index; i++) {
                it.next();
            }
            return i == index ? it.next() : null;
        }

        public Iterator iterator() {
            // XXX might be better to copy the entries to avoid 
            // concurrent modification!?
            return new Iterator() {
                private Iterator    it = delegate.iterator();
                private Object      cursor;
                private boolean     isOnNext;
                
                public boolean hasNext() {
                    if (!isOnNext) {
                        cursor = null;
                        while (cursor == null && it.hasNext()) {
                            cursor = filter( it.next() );
                        }
                        isOnNext = true;
                    }
                    return cursor != null;
                }

                public Object next() {
                    if (!isOnNext) {
                        hasNext();
                    }
                    if (cursor == null) {
                        throw new NoSuchElementException();
                    }
                    isOnNext = false;
                    return cursor;
                }
                
                public void remove() {
                    it.remove();
                }
            };
        }

        public List toList() {
            return delegate.toList();
        }

        public Set toSet() {
            return delegate.toSet();
        }

        public boolean add( int i, Object entity ) {
            return delegate.add( i, entity );
        }

        public boolean add( Object entity ) {
            return delegate.add( entity );
        }

        public boolean isAggregated() {
            return delegate.isAggregated();
        }

        public boolean isImmutable() {
            return delegate.isImmutable();
        }

        public <T> T metaInfo( Class<T> infoType ) {
            return delegate.metaInfo( infoType );
        }

        public QualifiedName qualifiedName() {
            return delegate.qualifiedName();
        }

        public boolean remove( Object entity ) {
            return delegate.remove( entity );
        }

        public Type type() {
            return delegate.type();
        }
    }
    
    
    /**
     * 
     */
    protected static class FilteredAssociation
            implements Association {

        private Association     delegate;

        protected FilteredAssociation( Association delegate ) {
            super();
            this.delegate = delegate;
        }

        public Object get() {
            org.polymap.core.model.security.ACL result = (org.polymap.core.model.security.ACL)delegate.get();
            return result != null && ACLUtils.checkPermission( result, AclPermission.READ, false )
                    ? result : null;
        }

        public void set( Object associated )
                throws IllegalArgumentException {
            delegate.set( associated );
        }

        public boolean isAggregated() {
            return delegate.isAggregated();
        }

        public boolean isImmutable() {
            return delegate.isImmutable();
        }

        public <T> T metaInfo( Class<T> infoType ) {
            return delegate.metaInfo( infoType );
        }

        public QualifiedName qualifiedName() {
            return delegate.qualifiedName();
        }

        public Type type() {
            return delegate.type();
        }
        
    }
    
}
