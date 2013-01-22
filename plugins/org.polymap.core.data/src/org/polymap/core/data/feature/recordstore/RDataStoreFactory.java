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
package org.polymap.core.data.feature.recordstore;

import java.util.Map;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RDataStoreFactory
        implements DataStoreFactorySpi {

    private static Log log = LogFactory.getLog( RDataStoreFactory.class );

    @Override
    public DataStore createDataStore( Map<String, Serializable> params )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public DataStore createNewDataStore( Map<String, Serializable> params )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean canProcess( Map<String, Serializable> params ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public String getDescription() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public String getDisplayName() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Param[] getParametersInfo() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean isAvailable() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Map<Key, ?> getImplementationHints() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
