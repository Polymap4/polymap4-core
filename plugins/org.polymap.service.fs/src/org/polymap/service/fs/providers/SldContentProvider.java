/* 
 * polymap.org
 * Copyright 2011-2012, Polymap GmbH. All rights reserved.
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
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.project.ILayer;
import org.polymap.core.style.IStyle;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.Range;

/**
 * Provides the style of the {@link ILayer} as SLD.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SldContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( SldContentProvider.class );


    public List<? extends IContentNode> getChildren( IPath path ) {
        IContentFolder parent = getSite().getFolder( path );
        
        // file
        if (parent instanceof SldFolder) {
            return Collections.singletonList( 
                    new SldFile( path, this, (ILayer)parent.getSource() ) );
        }
        // folder
        else if (parent instanceof ProjectContentProvider.LayerFolder) {
            return Collections.singletonList( 
                    new SldFolder( path, this, (ILayer)parent.getSource() ) );
        }
        return null;
    }
    

    /*
     * 
     */
    public static class SldFolder
            extends DefaultContentFolder {

        public SldFolder( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( "sld", parentPath, provider, layer );
        }

        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält die <b>SLD-Daten</b> Ebene \"" + getLayer().getLabel() 
                    + "\".";
        }
    }


    /*
     * 
     */
    public static class SldFile
            extends DefaultContentNode
            implements IContentFile {

        private byte[]              content;
        
        private Exception           lastException;
        
        private Date                modified;
        
        
        public SldFile( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( layer.getLabel() + ".sld", parentPath, provider, layer );
            try {
                lastException = null;
                IStyle layerStyle = getLayer().getStyle();

                String sld = layerStyle.createSLD( new NullProgressMonitor() );
                content = sld.getBytes( "UTF-8" );
                modified = new Date();
            }
            catch (Exception e) {
                log.warn( "", e );
                lastException = e;
            }
        }

        public int getSizeInMemory() {
            return (content != null ? content.length : 0) + super.getSizeInMemory();
        }


        public ILayer getLayer() {
            return (ILayer)getSource();
        }


        public Long getContentLength() {
            if (lastException != null) {
                log.warn( "", lastException );
                return null;
            }
            else {
                return (long)content.length;
            }
        }


        public String getContentType( String accepts ) {
            return "text/plain";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            return modified;
        }


        public void sendContent( final OutputStream out, Range range, Map<String, String> params, String contentType )
        throws IOException, BadRequestException {
            log.debug( "range: " + range + ", params: " + params + ", contentType: " + contentType );
            if (lastException != null) {
                log.warn( "", lastException );
            }
            else {
                out.write( content );
            }
        }

    }

}
