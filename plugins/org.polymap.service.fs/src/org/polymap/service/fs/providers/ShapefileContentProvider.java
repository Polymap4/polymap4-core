/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.providers;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.project.ILayer;

import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;

/**
 * Provides a shapefile folder for every parent folder that exposes an {@link ILayer}
 * as source.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapefileContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( ShapefileContentProvider.class );


    public List<? extends IContentNode> getChildren( IPath path, IContentSite site ) {
        
        IContentFolder parent = site.getFolder( path );
        
        // shapefile
        if (parent instanceof ShapefileFolder) {
            return null;
        }
        
        // folder
        else if (parent != null && parent.getSource() instanceof ILayer) {
            return Collections.singletonList( 
                    new ShapefileFolder( path, this, (ILayer)parent.getSource() ) );
        }
        return null;
    }
    

    /*
     * 
     */
    public class ShapefileFolder
            extends DefaultContentFolder {

        public ShapefileFolder( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( "Shapefile", parentPath, provider, layer );
        }

        
        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält die <b>Shapefile-Daten</b> der Ebene \"" + getLayer().getLabel() + "\".";
        }
        
    }

}
