/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.event;

import org.polymap.core.runtime.SessionContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ScopeEventFilter
        implements EventFilter<Event> {
    
    /**
     * 
     */
    public static ScopeEventFilter forScope( Event.Scope scope ) {
        switch (scope) {
            case Session: return new SessionScope();
            case JVM: return JvmScope;
            default: throw new IllegalStateException( "Unknown event scope: " + scope );
        }
    }
    
    /** 
     * 
     */
    static final ScopeEventFilter JvmScope = new ScopeEventFilter() {
        @Override
        public boolean apply( Event ev ) {
            return true;
        }
    };

    
    /** 
     * 
     */
    static class SessionScope
            extends ScopeEventFilter {
        
        private SessionContext      session;
        
        protected SessionScope() {
            session = SessionContext.current();
            assert session != null;
        }
        
        @Override
        public boolean apply( Event ev ) {
            return SessionContext.current() == session;
        }
    };
    
}
