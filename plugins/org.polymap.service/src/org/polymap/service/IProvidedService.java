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
package org.polymap.service;

import java.util.List;

import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.project.IMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface IProvidedService
        extends Entity {

//    public static final int     SERVICE_TYPE_WMS = 1;
//    public static final int     SERVICE_TYPE_WFS = 2;
    
    public static final String  PROP_PATHSPEC = "PATH_SPEC";
    public static final String  PROP_SRS = "SRS";
    public static final String  PROP_SERVICE_TYPE = "SERVICE_TYPE";
    public static final String  PROP_ENABLED = "ENABLED";


//    @ModelProperty(PROP_SERVICE_TYPE)
//    public void setServiceType( int serviceType );
//    
//    public int getServiceType();
    
    public boolean isEnabled();
    
    public boolean isStarted();
    
    @ModelProperty(PROP_ENABLED)
    public void setEnabled( Boolean enabled );
    
    public String getPathSpec();
    
    @ModelProperty(PROP_PATHSPEC)
    public void setPathSpec( String url );
    
    public String getMapId();

    public IMap getMap();

    public Class getServiceType();

    public List<String> getSRS();
    
    @ModelProperty(PROP_SRS)
    public void setSRS( List<String> srs );
    
    
    public void start() throws Exception;
    
    public void stop() throws Exception;

}
