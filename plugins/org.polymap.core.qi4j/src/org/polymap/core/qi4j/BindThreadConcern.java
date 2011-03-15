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

package org.polymap.core.qi4j;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

/**
 * Binds the current thread to the UnitOfWork of the session.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class BindThreadConcern
        extends GenericConcern {

    private static Log log = LogFactory.getLog( BindThreadConcern.class );

    @Structure Module       _module;
    
    
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable {
        try {
            // check all modules
            QiModule applied = Qi4jPlugin.Session.instance().resolveModule( _module );
            applied.bindThread();
        }
        catch (Throwable e) {
            // XXX hack to allow ProjectRepository#init() to run
            log.info( "unhandled:" + e );
        }
        
        return next.invoke( proxy, method, args );
    }
 
}
