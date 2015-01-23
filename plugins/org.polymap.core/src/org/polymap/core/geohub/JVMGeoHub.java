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
package org.polymap.core.geohub;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.rap.rwt.SessionSingletonBase;

import org.polymap.core.geohub.event.GeoEvent;

/**
 * Simple implementation of the {@link GeoHub} interface based on synchronal
 * in-VM events. This uses and depends on the RAP session singleton mechanism.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class JVMGeoHub
        extends GeoHub {

    private static Log log = LogFactory.getLog( JVMGeoHub.class );

    public static JVMGeoHub instance() {
        return (JVMGeoHub)SessionSingletonBase.getInstance( JVMGeoHub.class );
    }

    
    // instance *******************************************
    
    private Set<Handler>                handlers = new HashSet();
    
    
    public synchronized void send( GeoEvent ev, GeoEventListener... exclude )
            throws GeoEventException {
//        Set<GeoEventListener> excludeSet = new HashSet();
//        for (GeoEventListener listener : exclude) {
//            excludeSet.add( listener );
//        }
        
        for (Handler handler : handlers) {
            try {
                // exclude contains very few objects, linear search seems to be
                // the cheapest way of searching
                if (!ArrayUtils.contains( exclude, handler.listener )
                        && handler.selector.accept( ev )) {
                    handler.listener.onEvent( ev );
                }
            }
            catch (Exception e) {
                log.error( e.getLocalizedMessage(), e );
            }
        }
    }


    public synchronized boolean subscribe( GeoEventListener listener, GeoEventSelector selector ) {
        return handlers.add( new Handler( listener, selector ) );
    }


    public synchronized boolean unsubscribe( GeoEventListener listener ) {
        return handlers.remove( new Handler( listener, null ) );
    }

    
    /**
     * 
     */
    class Handler {
        
        private GeoEventListener        listener;
        
        private GeoEventSelector        selector;

        public Handler( GeoEventListener listener, GeoEventSelector selector ) {
            this.listener = listener;
            this.selector = selector;
        }

        public int hashCode() {
            return listener.hashCode();
        }

        public boolean equals( Object obj ) {
            if (obj instanceof Handler) {
                return listener.equals( ((Handler)obj).listener );
            }
            return false;
        }

    }

}
