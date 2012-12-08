/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.service;

import java.util.List;

import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.project.IMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public interface IProvidedService
        extends Entity {

    public static final String  PROP_PATHSPEC = "PATH_SPEC";
    public static final String  PROP_SRS = "SRS";
    public static final String  PROP_SERVICE_TYPE = "SERVICE_TYPE";
    public static final String  PROP_ENABLED = "ENABLED";

    public boolean isEnabled();
    
    public boolean isStarted();
    
    @ModelProperty(PROP_ENABLED)
    public void setEnabled( Boolean enabled );
    
    public String getPathSpec();
    
    @ModelProperty(PROP_PATHSPEC)
    public void setPathSpec( String url );
    
    public String getMapId();

    public IMap getMap();

    /**
     * The type of this service.
     * 
     * @return One of the <code>SERVICE_TYPE_xxx</code> constants in
     *         {@link ServicesPlugin}.
     */
    public String getServiceType();

    /**
     * 
     * 
     * @param serviceType One of the <code>SERVICE_TYPE_xxx</code> constants in
     *        {@link ServicesPlugin}.
     */
    public boolean isServiceType( String serviceType );
    
    public List<String> getSRS();
    
    @ModelProperty(PROP_SRS)
    public void setSRS( List<String> srs );
    
    
    public void start() throws Exception;
    
    public void stop() throws Exception;

}
