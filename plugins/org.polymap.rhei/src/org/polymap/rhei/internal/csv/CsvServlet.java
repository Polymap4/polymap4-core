/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 */
package org.polymap.rhei.internal.csv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides the content of a FeatureCollection encoded as CSV via HTTP/REST
 * interface.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 1.0
 */
public class CsvServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( CsvServlet.class );

    public static final FastDateFormat  df = DateFormatUtils.ISO_DATE_FORMAT;

    /** Hackish way to deliver the content to the servlet. */
    static Map<String,List<Feature>>    map = new WeakHashMap();


    public CsvServlet() {
        log.info( "CsvServlet..." );
    }


    public boolean isValid() {
        return true;
    }


    public void init( ServletConfig config )
            throws ServletException {
        super.init( config );
        log.info( "    contextPath: " + config.getServletContext().getContextPath() );
    }


    protected void doGet( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
        log.info( "Request: " + request.getPathInfo() );
        try {
            // download.html
            if (request.getPathInfo().startsWith( "/download.html" )) {
                // sending an HTML page helps debugging on IE, which often blocks or
                // otherwise fails to download directly
                String id = request.getParameter( "id" );
                String filename = request.getParameter( "filename" );
                String linkTarget = "../csv/" + id + "/" + (filename != null ? filename : "polymap3_export.csv");

                response.setContentType( "text/html; charset=ISO-8859-1" );

                PrintWriter out = response.getWriter();
                out.println( "<html><head>" );
                out.println( "<meta HTTP-EQUIV=\"REFRESH\" content=\"0; url=" + linkTarget + "\">" );
                out.println( "</head>" );
                out.println( "<a href=\"" + linkTarget + "\">Download starten</a>" );
                out.println( "</html>" );
                out.flush();
            }
            // CSV
            else {
                doGetFeatures( request, response );
            }
        }
        catch (Exception e) {
            log.debug( "", e );
            throw new ServletException( e );
        }
    }


    private void doGetFeatures( HttpServletRequest request, HttpServletResponse response )
    throws Exception {
        String[] pathInfo = StringUtils.split( request.getPathInfo(), "/" );
        String id = pathInfo[0];
        log.debug( "Request: id=" + id );

        String filename = pathInfo.length > 1 ? pathInfo[1] : "polymap3_export.csv";
        List<Feature> features = map.get( id );

        response.setContentType( "text/csv; charset=ISO-8859-1" );
        response.setHeader( "Content-disposition", "attachment; filename=" + filename );
        response.setHeader( "Pragma", "public" );
        response.setHeader( "Cache-Control", "must-revalidate, post-check=0, pre-check=0" );
        response.setHeader( "Cache-Control", "public" );
        response.setHeader( "Expires", "0" );

        PrintWriter writer = response.getWriter();

        CsvPreference prefs = new CsvPreference('"', ';', "\r\n");  //CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
        CsvListWriter csvWriter = new CsvListWriter( writer, prefs );

        // all features
        boolean noHeaderYet = true;
        for (Feature feature : features) {

            // header
            if (noHeaderYet) {
                List<String> header = new ArrayList( 32 );
                for (Property prop : feature.getProperties()) {
                    Class<?> binding = prop.getType().getBinding();
                    if (Number.class.isAssignableFrom( binding )
                            || Boolean.class.isAssignableFrom( binding )
                            || Date.class.isAssignableFrom( binding )
                            || String.class.isAssignableFrom( binding )) {
                        header.add( prop.getName().getLocalPart() );
                    }
                }
                csvWriter.writeHeader( header.toArray(new String[header.size()]) );
                noHeaderYet = false;
            }

            // all properties
            List line = new ArrayList( 32 );
            for (Property prop : feature.getProperties()) {
                Class binding = prop.getType().getBinding();
                Object value = prop.getValue();

                // Number
                if (Number.class.isAssignableFrom( binding )) {
                    line.add( value != null ? value.toString() : "" );
                }
                // Boolean
                else if (Boolean.class.isAssignableFrom( binding )) {
                    line.add( value == null ? "" :
                            ((Boolean)value).booleanValue() ? "ja" : "nein");
                }
                // Date
                else if (Date.class.isAssignableFrom( binding )) {
                    line.add( value != null ? df.format( (Date)value ) : "" );
                }
                // String
                else if (String.class.isAssignableFrom( binding )) {
                    String s = value != null ? (String)value : "";
                    // Excel happens to interprete decimal value otherwise! :(
                    s = StringUtils.replace( s, "/", "-" );
                    line.add( s );
                }
                // other
                else {
                    log.debug( "skipping: " + prop.getName().getLocalPart() + " type:" + binding );
                }
            }
            log.debug( "LINE: " + line );
            csvWriter.write( line );
        }

        csvWriter.close();
    }

}
