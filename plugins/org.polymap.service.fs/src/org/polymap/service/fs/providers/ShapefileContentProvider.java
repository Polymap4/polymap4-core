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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;
import org.polymap.service.fs.spi.Range;

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
            List<IContentNode> result = new ArrayList();
            
            ILayer layer = ((ShapefileFolder)parent).getLayer();
            ShapefileContainer container = new ShapefileContainer( layer );
            result.add( new ShapefileFile( path, this, (ILayer)parent.getSource()
                    , container, "shp" ) );
            result.add( new ShapefileFile( path, this, (ILayer)parent.getSource()
                    , container, "shx" ) );
            result.add( new ShapefileFile( path, this, (ILayer)parent.getSource()
                    , container, "qix" ) );
            result.add( new ShapefileFile( path, this, (ILayer)parent.getSource()
                    , container, "fix" ) );
            result.add( new ShapefileFile( path, this, (ILayer)parent.getSource()
                    , container, "dbf" ) );
            result.add( new ShapefileFile( path, this, (ILayer)parent.getSource()
                    , container, "prj" ) );
            return result;
        }
        // folder
        else if (parent instanceof ProjectContentProvider.LayerFolder) {
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
            super( "shapefile", parentPath, provider, layer );
        }
        
        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält die <b>Shapefile-Daten</b> der Ebene \"" + getLayer().getLabel() + "\".";
        }
    }

    
    /*
     * 
     */
    static class ShapefileContainer {
        
        File            file;
        
        Exception       exception;
        
        ILayer          layer;
        
        Date            modified = new Date();
        
        
        public ShapefileContainer( ILayer layer ) {
            this.layer = layer;
        }

        public InputStream getFileContent( String fileSuffix )
        throws IOException {
            // init shapefile
            getFileSize( fileSuffix );
            
            if (exception == null) {
                File f = Path.fromOSString( file.getAbsolutePath() )
                        .removeFileExtension().addFileExtension( fileSuffix ).toFile();
                return new FileInputStream( f );
            }
            else {
                throw (IOException)exception;
            }
        }
        
    
        public synchronized Long getFileSize( String fileSuffix ) {
            if (file == null) {
                try {
                    exception = null;
                    PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );

                    ShapefileGenerator generator = new ShapefileGenerator();
                    file = generator.writeShapefile( fs.getFeatures() );

                    modified = new Date();
                }
                catch (Exception e) {
                    log.warn( "", e );
                    exception = e;
                }
            }
            if (exception == null) {
                File f = Path.fromOSString( file.getAbsolutePath() )
                        .removeFileExtension().addFileExtension( fileSuffix ).toFile();
                return f.length();
            }
            else {
                return null;
            }
        }
        
    }
    
    
    /*
     * 
     */
    public class ShapefileFile
            extends DefaultContentNode
            implements IContentFile {

        private ShapefileContainer  container;
        
        private String              fileSuffix;
        
        
        public ShapefileFile( IPath parentPath, IContentProvider provider, ILayer layer,
                ShapefileContainer container, String fileSuffix ) {
            super( layer.getLabel() + "." + fileSuffix, parentPath, provider, layer );
            this.container = container;
            this.fileSuffix = fileSuffix;
        }

        
        public ILayer getLayer() {
            return (ILayer)getSource();
        }

        
        public Long getContentLength() {
            if (container.exception != null) {
                log.warn( "", container.exception );
                return null;
            }
            else {
                return container.getFileSize( fileSuffix );                
            }
        }


        public String getContentType( String accepts ) {
            return "application/octec-stream ";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            return container.modified;
        }


        public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
        throws IOException, BadRequestException {
            log.info( "range: " + range + ", params: " + params + ", contentType: " + contentType );
            
            if (container.exception != null) {
                log.warn( "", container.exception );
            }
            else {
                InputStream in = container.getFileContent( fileSuffix );                
                try {
                    IOUtils.copy( in, out );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }
        }

    }

}
