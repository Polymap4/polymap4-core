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
package org.polymap.core.data.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;

import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class HtmlReportFactory
        implements IReportFactory {

    private static Log log = LogFactory.getLog( HtmlReportFactory.class );
    

    @Override
    public List<IReport> createReports( IReportSite site ) {
        List<IReport> result = new ArrayList();

        try {
            IFolder reportsFolder = getOrCreateReportsFolder();
            
            // get changes from the filesystem
            reportsFolder.refreshLocal( IFolder.DEPTH_INFINITE, null );
            
            for (IResource file : reportsFolder.members()) {
                if (file instanceof IFile
                        //&& ((IFile)file).getName().startsWith( "report" )
                        && ((IFile)file).getName().endsWith( "html" )) {
                    result.add( new HtmlReport( site, (IFile)file ) );                    
                }
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, null, e.getLocalizedMessage(), e );
        }
        return result;
    }

    
    /**
     * Returns the {@link IProject} containing all the scripts. If it does not exist
     * then it is created. The project is stored under "<workspace>/Scripts" If it
     * does not exists.
     * 
     * @return The Scripts project.
     * @throws CoreException 
     */
    public static IFolder getOrCreateReportsFolder() 
    throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject( "Scripts" );
        
        if (!project.exists()) {
            // delete also ./.metadata/.plugins/org.eclipse.core.resources/.projects/Scripts/
            project.create( null );
        }
        project.open( null );
        
        IFolder reportsFolder = project.getFolder( "src/reports" );
        if (!reportsFolder.exists()) {
            reportsFolder.create( false, true, null );
        }
        return reportsFolder;
    }

    
    /**
     * 
     */
    static class HtmlReport
            implements IReport {

        private IReportSite         site;
        
        private IFile               htmlFile;
        
        
        public HtmlReport( IReportSite site, IFile htmlFile ) throws CoreException {
            this.site = site;
            this.htmlFile = htmlFile;
            htmlFile.refreshLocal( IFile.DEPTH_ZERO, null );
        }

        
        @Override
        public void execute( IProgressMonitor monitor ) {
            monitor.beginTask( i18n( "monitorTitle" ), 3 );
            monitor.worked( 1 );

            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    try {
                        // HTML download handler
                        String url = DownloadServiceHandler.registerContent( 
                                new FileContentProvider( htmlFile ) );

                        // JSON download handler
                        String url2 = DownloadServiceHandler.registerContent( 
                                new JsonContentProvider( site.getFeatures() ) );
                        url += "&json=" + URLEncoder.encode( url2, "UTF8" );
                        log.info( "Report - URL: " + url );

                        // support files (folder was refreshed on #createReports())
                        for (IResource file : htmlFile.getParent().members()) {
                            if (file instanceof IFile
                                    && !((IFile)file).getName().endsWith( ".html" )) {
                                DownloadServiceHandler.registerContent( file.getName(), 
                                        new FileContentProvider( (IFile)file ) );
                            }
                        }
                        // open browser window
                        ExternalBrowser.open( "html_reports_window", url,
                                ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS | ExternalBrowser.LOCATION_BAR );
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, i18n( "errorMsg" ), e.getLocalizedMessage(), e );
                    }
                }
            });
        }

        
        @Override
        public String getDescription() {
//            InputStream in = null;
//            try {
//                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
//                XMLReader reader = parser.getXMLReader();
//                
//                // SAX handler:
//                ContentHandler handler = new DefaultHandler() {
//                    public void startElement( String uri, String localName, String qName,
//                            Attributes attributes ) throws SAXException {
//                        log.debug( "    SAX: " + localName );
//                    }
//                };
//                reader.setContentHandler( handler );
//                
//                // parse
//                in = htmlFile.getContents();
//                reader.parse( new InputSource( in ) );
//                return "geparst.";
//            }
//            catch (Exception e) {
//                log.warn( "", e );
                return "- Für diesen Report ist keine Beschreibung verfügbar -";
//            }
//            finally {
//                IOUtils.closeQuietly( in );
//            }
        }

        @Override
        public String getLabel() {
            return StringUtils.substringBeforeLast( htmlFile.getName(), ".html" );
        }

        @Override
        public List<IWizardPage> getWizardPages() {
            return Collections.EMPTY_LIST;
        }
        
        protected String i18n( String key, Object... args ) {
            return Messages.get( "HtmlReport_" + key, args );
        }

    }

    
    /**
     * ContentProvider for {@link IFile} content. 
     */
    static class FileContentProvider
            implements ContentProvider {

        private IFile           htmlFile;
        
        public FileContentProvider( IFile htmlFile ) {
            this.htmlFile = htmlFile;
        }

        public String getContentType() {
            try {
                // reflect changes made in the filesystem when browser reloads
                htmlFile.refreshLocal( IFile.DEPTH_ZERO, null );
                
                // ignore IFile charset as this is ISO-8859-1
                return "text/html; charset=UTF8";  // + htmlFile.getCharset();
            } 
            catch (CoreException e) {
                return "text/html; charset=UTF8";
            }
        }

        public String getFilename() {
            return htmlFile.getName();
        }

        public InputStream getInputStream() throws Exception {
            return htmlFile.getContents( true );
        }

        public boolean done( boolean success ) {
            return false;
        }
    }
    

    /**
     * 
     */
    static class JsonContentProvider
            implements ContentProvider {

        private FeatureCollection       features;

        public JsonContentProvider( FeatureCollection features ) {
            this.features = features;
        }

        public boolean done( boolean success ) {
            return false;
        }

        public String getContentType() {
            return "application/json; charset=UTF8";
        }

        public String getFilename() {
            return "features.json";
        }

        public InputStream getInputStream() throws Exception {
            FeatureJSON encoder = new FeatureJSON();
            encoder.setEncodeFeatureBounds( false );
            encoder.setEncodeFeatureCollectionBounds( false );
            encoder.setEncodeFeatureCollectionCRS( false );
            encoder.setEncodeFeatureCRS( false );

            ByteArrayOutputStream out = new ByteArrayOutputStream( 128*1024 );
            Writer writer = new OutputStreamWriter( out, "UTF8" );
            encoder.writeFeatureCollection( features, writer );
            
            log.info( "JSON: " + out.toByteArray().length + " bytes" );
            
            return new ByteArrayInputStream( out.toByteArray() );
        }
        
    }
    
}
