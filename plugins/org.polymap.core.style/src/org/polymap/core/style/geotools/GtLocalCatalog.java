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
package org.polymap.core.style.geotools;

import java.util.List;
import java.util.UUID;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.styling.Style;

import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.catalog.ICatalogInfo;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolve;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.style.IStyle;
import org.polymap.core.style.IStyleCatalog;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Ligi</a>
 * @author <a href="http://www.polymap.de">Falko</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GtLocalCatalog
        extends IStyleCatalog {

    private static final Log log = LogFactory.getLog( GtLocalCatalog.class );

    public static final String  ENCODING = "UTF-8";
    
    private File                base_path;
    
    private ICatalogInfo        info;

    private DefaultStyles       defaultStyles = new DefaultStyles();
    

    public GtLocalCatalog( IPath ws ) {
        this.info = new ICatalogInfo( "GtLocalCatalog", "", null, null );
        this.base_path = new File( ws.toFile(), "sld" );
        base_path.mkdirs();
        log.info( "Crating path for Local Style Catalog " + base_path.getAbsolutePath() );
    }
    
    
    


    public void add( IStyle style )
            throws UnsupportedOperationException {
        assert (style instanceof GtStyle) : "Wrong style type.";
        GtStyle gtStyle = (GtStyle)style;
        
        if (gtStyle.getID() != null) {
            log.warn( "Style already has ID: " + gtStyle.getID() );
        }
        
        try {
            UUID uuid = UUID.randomUUID();
            File file = new File( base_path, uuid + ".sld" );
            ID id = new ID( file, "" );
            gtStyle.catalog = this;
            gtStyle.id = id;
            gtStyle.store( new NullProgressMonitor() );
        }
        catch (IOException e) {
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }
    }


    public void remove( IStyle style )
            throws UnsupportedOperationException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void replace( ID id, IStyle replacement )
            throws UnsupportedOperationException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public IStyle createDefaultStyle( FeatureSource fs ) {
        Style style = fs != null
                ? defaultStyles.findStyle( fs )
                : defaultStyles.createAllStyle();
        return new GtStyle( style );
    }


    public IStyle getById( ID id, IProgressMonitor monitor ) {
        // XXX check if it exists
        GtStyle result = new GtStyle( this, id );
        return result;
    }

    
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor )
    throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public <T> boolean canResolve( Class<T> adaptee ) {
        try {
            Object value = resolve( adaptee, null );
            return value != null;
        }
        catch (IOException e) {
            log.warn( e.getLocalizedMessage(), e );
            return false;
        }
    }


    public List<IResolve> find( ID resourceId, IProgressMonitor monitor ) {
        throw new RuntimeException( "not yet implemented." );
    }
    
    public List<IResolve> search( String pattern, Envelope bbox, IProgressMonitor monitor )
            throws IOException {
        throw new RuntimeException( "not yet implemented." );
    }

    
    public ID getID() {
        return new ID( getIdentifier() );
    }

    public URL getIdentifier() {
        return info.getSource();
    }

    public Throwable getMessage() {
        return null;
    }

    public Status getStatus() {
        return Status.CONNECTED;
    }

    public String getTitle() {
        return info.getTitle();
    }
    
    
    String loadSLD( ID id )
            throws IOException {
        File file = id.toFile();
        return FileUtils.readFileToString( file, ENCODING );
    }


    void storeSLD( ID id, String sld )
            throws IOException {
        File file = id.toFile();
        FileUtils.writeStringToFile( file, sld, ENCODING );
    }


}
