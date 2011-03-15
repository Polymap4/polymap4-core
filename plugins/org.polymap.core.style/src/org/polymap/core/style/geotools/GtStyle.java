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

import java.util.Locale;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.styling.SLDParser;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;

import net.refractions.udig.catalog.ID;

import org.eclipse.rwt.RWT;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.style.IStyle;
import org.polymap.core.style.IStyleInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GtStyle
        extends IStyle {

    private static final Log log = LogFactory.getLog( GtStyle.class );

    public static StyleFactory  factory = CommonFactoryFinder.getStyleFactory( GeoTools.getDefaultHints() );
    
    protected GtLocalCatalog      catalog;
    
    protected ID                  id;
    
    /** Loaded on demand by {@link #resolveStyle()}. */
    protected Style               style=null;
    
    
    private Style getStyle() {
    	if (style==null)
			try {
				resolveStyle(null);
			} catch (IOException e) {
				log.warn("Style can't be loaded");
			}
    	return style;
    }
    
    public GtStyle( GtLocalCatalog catalog, ID id ) {
        super();
        this.catalog = catalog;
        this.id = id;
     }

    
    public GtStyle( Style style ) {
        this.style = style;
    }

    public synchronized Style resolveStyle( IProgressMonitor monitor ) 
    throws IOException {
        if (style == null) {
            String sld = catalog.loadSLD( id );

            log.debug( "Loading SLD..." );
            monitor = monitor != null ? monitor : new NullProgressMonitor();
            monitor.subTask( "Loading SLD" );

            SLDParser styleReader = new SLDParser( factory, new StringReader( sld ) );
            style = styleReader.readXML()[0];
            
            log.debug( "...done loading SLD." );
            monitor.worked( 1 );
        }
        return style;
    }
    
    public void store( IProgressMonitor monitor )
            throws IOException, UnsupportedOperationException {
        catalog.storeSLD( id, createSLD( monitor ) );
    }


    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor )
    throws IOException {
        assert adaptee != null : "No adaptor specified";
        monitor = monitor != null ? monitor : new NullProgressMonitor();

        if (adaptee.isAssignableFrom( Style.class )) {
            return adaptee.cast( resolveStyle( monitor ) );
        }
        // the SLD file
        if (adaptee.isAssignableFrom( File.class )) {
            return id != null ? adaptee.cast( id.toFile() ) : null;
        }
        return super.resolve( adaptee, monitor );    
    } 

    
    protected Object createInfo( IProgressMonitor monitor ) {
        Locale locale = RWT.getLocale();
        return new IStyleInfo( 
                style.getDescription().getTitle().toString( locale ),
                style.getDescription().getTitle().toString( locale ),
                style.getDescription().getAbstract().toString( locale ),
                new String[0],
                null);
    }


    public String createSLD( IProgressMonitor monitor ) {
        try {
            monitor.subTask( "Creating SLD" );
            SLDTransformer styleTransform = new SLDTransformer();
            styleTransform.setIndentation( 2 );

            String xml = styleTransform.transform( getStyle() );
            monitor.worked( 1 );
            return xml;
        } 
        catch (TransformerException e) {
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }
    }


    public ID getID() {
        return id;
    }


    public URL getIdentifier() {
        return id.toURL();
    }
    
    
    public void setStyle(Style new_style) {
    	this.style=new_style;
    }

}
