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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembly;

/**
 * Assembler and factory for an {@link QiModule}. There is one instance per
 * application.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class QiModuleAssembler {

    private static Log log = LogFactory.getLog( QiModuleAssembler.class );
    

    protected QiModuleAssembler() {
    }
    
    
    public abstract Module getModule();

    
    /**
     * This method is called right after {@link #assemble(ApplicationAssembly)}.
     */
    protected abstract void setApp( Application app );

    
    /**
     * Called ones during init of the application.
     * 
     * @param app
     * @throws Exception 
     */
    public abstract void assemble( ApplicationAssembly assembly ) 
    throws Exception;
    
    public abstract void createInitData() 
    throws Exception;

    /**
     * Create a new module for a new user session.
     * @param session 
     * 
     * @return Newly created module.
     */
    public abstract QiModule newModule();
    
}
