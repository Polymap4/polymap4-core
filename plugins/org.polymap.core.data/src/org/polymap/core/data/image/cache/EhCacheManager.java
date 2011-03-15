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

import java.io.File;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.TypedProperties;

/**
 * The CacheManager implementation based on EhCache.
 *
 * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
 */
class EhCacheManager
        extends CacheManager {

    private static final org.apache.commons.logging.Log log = 
            org.apache.commons.logging.LogFactory.getLog( EhCacheManager.class );

    private net.sf.ehcache.CacheManager     ehcm;
    
    private String                          diskStorePath;
    
    
    EhCacheManager() {
//        URL configURL = Polymap2.getInstance().getResource( "config/ehcache.xml" );
//        ehcm = new net.sf.ehcache.CacheManager( configURL );
        
        Configuration config = new Configuration();
        
        DiskStoreConfiguration diskStore = new DiskStoreConfiguration();
        File f = new File( Polymap.getWorkspacePath().toFile(), "cache" );
        diskStorePath = f.getAbsolutePath();
        log.info( "diskStorePath: " + diskStorePath );
        diskStore.setPath( diskStorePath );
        config.addDiskStore( diskStore );

        CacheConfiguration defaultCache = new CacheConfiguration();
        config.addDefaultCache( defaultCache );
        
        ehcm = new net.sf.ehcache.CacheManager( config );
    }
    

    public synchronized Cache getCache( String name, Properties props ) {
        TypedProperties params = new TypedProperties( props );
        net.sf.ehcache.Cache delegate = ehcm.getCache( name );
        int maxElementsInMemory = params.getInt( PARAM_MAX_ELEMENTS_IN_MEMORY, 20 );
        int maxElementsOnDisk = params.getInt( PARAM_MAX_ELEMENTS_ON_DISK, 10000 );
        int timeToLive = params.getInt( PARAM_TIME_TO_LIVE, 3600*24 );
        boolean eternal = false;
        if (timeToLive < 0) {
            timeToLive = Integer.MAX_VALUE;
            eternal = true;
        }
        boolean diskPersistent = params.getBoolean( PARAM_DISK_PERSISTENT, true );
        if (delegate == null) {
            log.info( "EhCacheManager: creating new cache instance:" +
                    "\n    diskStorePath = " + diskStorePath +
                    "\n    " + PARAM_MAX_ELEMENTS_IN_MEMORY + " = " + maxElementsInMemory +
                    "\n    " + PARAM_MAX_ELEMENTS_ON_DISK + " = " + maxElementsOnDisk + 
                    "\n    " + PARAM_TIME_TO_LIVE + " = " + timeToLive + 
                    "\n    eternal = " + eternal + 
                    "\n    " + PARAM_DISK_PERSISTENT + " = " + diskPersistent 
                    );
            delegate = new net.sf.ehcache.Cache( name, 
                    maxElementsInMemory, 
                    MemoryStoreEvictionPolicy.LRU, 
                    true,                   // overflow to disk 
                    diskStorePath, 
                    eternal,                // eternal
                    timeToLive, 
                    timeToLive, 
                    diskPersistent,
                    120,                    // disk expire thread interval 
                    null,
                    null,                   // bootstrap cache loader
                    maxElementsOnDisk, 
                    1);                     // disk spool buf MB
            delegate.setDiskStorePath( new File( diskStorePath ).getAbsolutePath() );
            ehcm.addCache( delegate );
            delegate.flush();
        }
        if (log.isDebugEnabled()) {
            log.debug( "CACHE: " + name + 
                    " - memory: " + delegate.getMemoryStoreSize() +
                    ", disk: " + delegate.getDiskStoreSize() );
        }
        return new EhCache( delegate );
    }


    void shutdown0() {
        ehcm.shutdown();
    }


    // EhCache ********************************************
    
    /**
     * The cache implementation.
     *
     * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
     *         <li>20.05.2008: created</li>
     */
    protected final class EhCache
            implements Cache {
        
        private net.sf.ehcache.Cache    delegate;

        protected EhCache( net.sf.ehcache.Cache delegate ) {
            this.delegate = delegate;
        }

        public CacheElement get( String key ) {
            log.debug( "### Cache - memory: " + delegate.getMemoryStoreSize()
                    + ", disk: " + delegate.getDiskStoreSize() );
            net.sf.ehcache.Element result = delegate.get( key );
            return result != null ? new EhCacheElement( result ) : null;
        }

        public void put( String key, byte[] data ) {
            delegate.put( new net.sf.ehcache.Element( key, data ) );
            delegate.flush();
            log.debug( "### Cache - memory: " + delegate.getMemoryStoreSize()
                    + ", disk: " + delegate.getDiskStoreSize() );
        }

        public void flush() {
            delegate.flush();
        }
        
    }

    
    // EhCacheElement *************************************
    
    /**
     * The CacheElement implementation.
     *
     * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
     *         <li>20.05.2008: created</li>
     */
    protected final class EhCacheElement
            implements CacheElement {
        
        private net.sf.ehcache.Element  delegate;

        protected EhCacheElement( net.sf.ehcache.Element delegate ) {
            this.delegate = delegate;
        }

        public byte[] getData() {
            return (byte[])delegate.getValue();
        }

        public String getKey() {
            return (String)delegate.getKey();
        }
    }
    
}
