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

import java.util.AbstractCollection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.security.Principal;
import java.security.acl.Acl;
import java.security.acl.AclEntry;
import java.security.acl.NotOwnerException;
import java.security.acl.Permission;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;

import org.polymap.core.model.Entity;
import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;

import sun.security.acl.AclEntryImpl;
import sun.security.acl.AclImpl;
import sun.security.acl.PrincipalImpl;

/**
 * Provides an ACL implementation based on the {@link sun.security.acl} package.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface ACL
        extends org.polymap.core.model.security.ACL {

    static Log log = LogFactory.getLog( ACL.class );
    

    /** Internally used as the owner of all ACLs. */
    static final Principal                  DEFAULT_OWNER = new PrincipalImpl( "admin" );
    
    @UseDefaults
    @Optional
    abstract Property<String>               ownerName();

    /** String representations of the ACL entries. */
    @UseDefaults
    @Optional
    abstract Property<Set<String>>          aclEntries();

    
    /**
     * The mixin.
     */
    abstract static class Mixin
            implements ACL, IModelChangeListener {

        private Acl                     acl;
        
        /** Acl does not provide this direct mapping. */
        private Map<Principal,AclEntry> entries = new HashMap();
        
        @This private Entity            entity;
        
        
        protected final void checkInit() {
            if (acl == null) {
                if (aclEntries().get() == null) {
                    aclEntries().set( new HashSet() );
                    ownerName().set( "undefined" );
                }

                acl = new AclImpl( DEFAULT_OWNER, "acl" );
                
                for (String entry : aclEntries().get()) {
                    String[] parts = StringUtils.split( entry, ":, " );

                    Principal p = new CompatiblePrincipal( parts[0] );
                    AclEntry aclEntry = new AclEntryImpl( p );

                    for (int i=1; i<parts.length; i++) {
                        aclEntry.addPermission( AclPermission.forName( parts[i] ) ); 
                    }
                    try {
                        acl.addEntry( DEFAULT_OWNER, aclEntry );
                        entries.put( p, aclEntry );
                    }
                    catch (NotOwnerException e) {
                        throw new RuntimeException( "Sould never happen.", e );
                    }
                }
                
//                // listen to entity (model) changes
//                // don't care outside Session
//                if (Polymap.getSessionDisplay() != null) {
//                    final QiModule module = Qi4jPlugin.Session.instance().resolveModule( entity );
//                    if (module != null) {
//                        module.addModelChangeListener( new IModelChangeListener() {
//                        });
//                    }
//                }
//                else {
//                    log.warn( "No module found for this ACL entity. -> no model change events are catched!" );
//                }
            }
        }

        public void modelChanged( ModelChangeEvent ev ) {
            log.info( "modelChanged(): ..." );
            acl = null;
            entries.clear();

            final QiModule module = Qi4jPlugin.Session.instance().resolveModule( entity );
            if (module != null) {
                // the next checkInit() adds a new listener again; if this
                // entity is no longer used, then checkInit() is never called again
                // and the listener was removed correctly
                module.removeModelChangeListener( this );
            }
        }
        
        protected void serialize() {
            Set<String> aclEntries = new HashSet();
            
            for (AclEntry entry : entries.values()) {
                StringBuffer buf = new StringBuffer( entry.getPrincipal().getName() )
                        .append( ":" );
                
                for (Enumeration e = entry.permissions(); e.hasMoreElements(); ) {
                    AclPermission permission = (AclPermission)e.nextElement();
                    buf.append( permission.getName() )
                            .append( e.hasMoreElements() ? "," : "" );
                }
                aclEntries.add( buf.toString() );
            }
            
            aclEntries().set( aclEntries );
        }

        
        public boolean addPermission( String principalName, AclPermission... permissions ) {
            checkInit();
            // FIXME what?
            CompatiblePrincipal principal = new CompatiblePrincipal( principalName );

            boolean result = true;
            for (AclPermission permission : permissions) {
                result &= addPermission( principal, permission );
            }
            return result;
        }


        private boolean addPermission( Principal principal, AclPermission permission ) {
            if (acl.checkPermission( principal, permission )) {
                return false;
            }
            AclEntry entry = entries.get( principal );
            if (entry == null) {
                try {
                    entry = new AclEntryImpl( principal );
                    acl.addEntry( DEFAULT_OWNER, entry );
                    entries.put( principal, entry );
                }
                catch (NotOwnerException e) {
                    throw new SecurityException( "", e );
                }
            }
            boolean result = entry.addPermission( permission );
            serialize();
            return result;
        }


        public boolean removePermission( String principalName, AclPermission... permissions ) {
            checkInit();
            // FIXME
            CompatiblePrincipal principal = new CompatiblePrincipal( principalName );
            
            boolean result = true;
            for (AclPermission permission : permissions) {
                result &= removePermission( principal, permission );
            }
            return result;
        }


        private boolean removePermission( Principal principal, AclPermission permission ) {
            if (!acl.checkPermission( principal, permission )) {
                return false;
            }
            AclEntry entry = entries.get( principal );
            assert entry != null;
            boolean result = entry.removePermission( permission );
            serialize();
            return result;
        }


        public boolean checkPermission( Principal principal, AclPermission permission ) {
            checkInit();
            // while creating the entity there is no ACL initialized, allow
            // access
            if (entries.isEmpty()) {
                return true;
            }
            else {
                return acl.checkPermission( principal, permission );
            }
        }

        
        public Iterable<ACL.Entry> entries() {
            checkInit();
            
            return new AbstractCollection<ACL.Entry>() {
                Iterator<AclEntry> it = entries.values().iterator();
                
                public Iterator<ACL.Entry> iterator() {
                    return new Iterator<ACL.Entry>() {

                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        public Entry next() {
                            return new EntryFacade( it.next() );
                        }

                        public void remove() {
                            throw new UnsupportedOperationException( "Use ACL.add/removePermission() instead." );
                        }
                    };
                }

                public int size() {
                    return entries.size();
                }
            };
        }

    }

    
    /**
     * Facade of an {@link AclEntry} providing the {@link ACL.Entry} interface. 
     */
    static class EntryFacade
            implements ACL.Entry {

        private AclEntry        delegate;
        
        
        protected EntryFacade( AclEntry delegate ) {
            this.delegate = delegate;
        }

        public Principal getPrincipal() {
            return delegate.getPrincipal();
        }
        
        public Iterable<AclPermission> permissions() {
            return new AbstractCollection<AclPermission>() {
                
                Enumeration<Permission> en = delegate.permissions();
                
                public Iterator<AclPermission> iterator() {
                    return new Iterator<AclPermission>() {

                        public boolean hasNext() {
                            return en.hasMoreElements();
                        }

                        public AclPermission next() {
                            return (AclPermission)en.nextElement();
                        }

                        public void remove() {
                            throw new UnsupportedOperationException( "Use ACL.add/removePermission() instead." );
                        }
                    };
                }

                public int size() {
                    throw new UnsupportedOperationException();
                }
            };
        }

    }

    
    /**
     * This principal is compatible to all instances of {@link Principal}. 
     */
    final class CompatiblePrincipal
            implements Principal {

        private final String        name;
        

        public CompatiblePrincipal( String name ) {
            assert name != null : "name must not be null";
            this.name = name;
        }

        public boolean equals( Object object ) {
            if (this == object) {
                return true;
            }
            if (object instanceof Principal) {
                return name.equals( ((Principal)object).getName() );
            }
            return false;
        }

        public int hashCode() {
            return name.hashCode();
        }

        public String getName() {
            return name;
        }

        /**
         * Returns a string representation of this principal.
         */
        public String toString() {
            return name;
        }

    }

}
