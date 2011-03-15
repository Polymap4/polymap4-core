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
package org.polymap.core.data.image.cache;

import java.util.Properties;

/**
 * The CacheManager manages creation and lifecycle of {@link Cache} instances.
 * 
 * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
 */
public abstract class CacheManager {

    private static final org.apache.commons.logging.Log log = 
            org.apache.commons.logging.LogFactory.getLog( CacheManager.class );

    // property names *************************************
    
    public static final String      PARAM_MAX_ELEMENTS_IN_MEMORY = "maxElementsInMemory"; 

    public static final String      PARAM_MAX_ELEMENTS_ON_DISK = "maxElementsOnDisk"; 

    public static final String      PARAM_TIME_TO_LIVE = "timeToLive"; 
    
    public static final String      PARAM_DISK_PERSISTENT = "diskPersistent"; 
    
    // static factory *************************************
    
    private static CacheManager     instance;
    
    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new EhCacheManager();
        }
        return instance;
    }
 
    public static synchronized void shutdown() {
        if (instance != null) {
            instance.shutdown0();
            instance = null;
        }
    }
 
    // API ************************************************
    
    public abstract Cache getCache( String name, Properties props );
        
    abstract void shutdown0();
    
}
