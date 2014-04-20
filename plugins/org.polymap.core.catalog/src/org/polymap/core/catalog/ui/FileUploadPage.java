/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *    Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.polymap.core.catalog.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.FileConnectionFactory;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.catalog.Messages;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.upload.IUploadHandler;
import org.polymap.core.ui.upload.Upload;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Taken from uDig to change to upload behaviour.
 * 
 * @author jeichar
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FileUploadPage 
        extends AbstractUDIGImportPage 
        implements UDIGConnectionPage {

    private static Log log = LogFactory.getLog( FileUploadPage.class );
    
    private static final IMessages      i18n = Messages.forPrefix( "FileUploadPage" );

    private final Set<URL>        list = new HashSet();

    private Composite             comp;

    private FileConnectionFactory factory = new FileConnectionFactory();

    // private FileDialog fileDialog;
    private Collection<URL>       resourceIds = new HashSet();

    private ListViewer            viewer;
    
    private Upload                upload;

    private CCombo                charsetCombo;

    protected String charsetName;


    public String getId() {
        return "org.polymap.core.catalog.openFilePage"; //$NON-NLS-1$
    }


    public FileUploadPage() {
        super( i18n.get( "pageTitle" ) );
    }

    
    List<IService> process( List<URL> urls, IProgressMonitor monitor ) {
        List<IService> resources = new ArrayList<IService>();
        monitor.beginTask( Messages.get("OpenFilePage_1"), list.size() );
        int worked = 0;
        for (URL url : urls) {
            if (monitor.isCanceled()) {
                return null;
            }
            try {
                monitor.subTask( url.toExternalForm() );
                List<IService> acquire = CatalogPlugin.getDefault().getServiceFactory()
                        .createService( url );
                resources.addAll( acquire );
            }
            catch (Throwable e) {
                CatalogUIPlugin.log( "error obtaining services from service factory", e ); //$NON-NLS-1$
            }
            monitor.worked( worked++ );
        }
        return resources;
    }

    
    private void pushButton( final int buttonId ) {
        try {
            findButton( getShell().getChildren(), buttonId )
                    .notifyListeners( SWT.Selection, new Event() );
        }
        catch (Exception e) {
            CatalogUIPlugin.log( "", e ); //$NON-NLS-1$
        }
    }

    
    Button findButton( Control[] children, int id ) {
        if (((Integer)getShell().getDefaultButton().getData()).intValue() == id)
            return getShell().getDefaultButton();

        for (Control child : children) {
            if (child instanceof Button) {
                Button button = (Button)child;
                if (button.getData() != null && ((Integer)button.getData()).intValue() == id)
                    return button;
            }
            if (child instanceof Composite) {
                Composite composite = (Composite)child;
                Button button = findButton( composite.getChildren(), id );
                if (button != null)
                    return button;
            }
        }
        return null;
    }
    
    
    protected boolean hasOneResource( SubProgressMonitor monitor, List<IService> services )
            throws IOException {
        if (services.size() > 1 || services.isEmpty()) {
            return false;
        }
        if (services.get( 0 ).resources( monitor ).size() == 1) {
            return true;
        }
        return false;
    }

    
    /**
     *
     */
    public boolean canFlipToNextPage() {
        return viewer != null && viewer.getList().getItemCount() > 0;
        //return (list != null && list.size() > 1);
    }

    
    /**
     *
     */
    public void createControl( Composite parent ) {
        list.clear();
        resourceIds.clear();
        
        comp = new Composite( parent, SWT.NONE );
        comp.setLayout( FormLayoutFactory.defaults().margins( 5 ).spacing( 5 ).create() );

        Label label = new Label( comp, SWT.NONE );
        label.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        label.setText( i18n.get( "groupTitle" ) );

        createUpload( comp );
        upload.setLayoutData( FormDataFactory.filled().top( label ).bottom( -1 ).right( -1 ).create() );

        createCharsetCombo( comp );
        charsetCombo.setLayoutData( FormDataFactory.filled().top( label ).left( upload ).bottom( -1 ).create() );
        
        viewer = new ListViewer( comp, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.setContentProvider( new ArrayContentProvider() );
        viewer.setLabelProvider( new LabelProvider() {
            public String getText( Object elm ) {
                return elm instanceof File ? ((File)elm).getName() : elm.toString();
            }
        });
        viewer.getControl().setLayoutData( FormDataFactory.filled().top( upload ).create() );

        setControl( comp );
    }

    
    private CCombo createCharsetCombo( Composite parent ) {
        charsetCombo = new CCombo( parent, SWT.BORDER | SWT.READ_ONLY );
        charsetCombo.setToolTipText( i18n.get( "charsetTip" ) );
        ArrayList items = new ArrayList( 256 );
        int first = 0;
        for (String input : Charset.availableCharsets().keySet()) {
            if (input.toLowerCase().startsWith( charsetCombo.getText().toLowerCase() )) {
                items.add( input );
                first = input.equals( "UTF-8" ) ? items.size()-1 : first;
            }
        }
        charsetCombo.setItems( Iterables.toArray( items, String.class ) );
        charsetCombo.setVisibleItemCount( 17 );
        charsetCombo.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                charsetName = charsetCombo.getText();
            }
        });
        charsetCombo.select( first );
        return charsetCombo;
    }
    
    
    /**
     *
     */
    private void createUpload( Composite parent ) {
        upload = new Upload( parent, SWT.BORDER /*Upload.SHOW_PROGRESS |*/ );
//        upload.setBrowseButtonText( i18n.get( "uploadBrowse" ) );

//        this.upload.addModifyListener( new ModifyListener() {
//            public void modifyText( ModifyEvent ev ) {
//                upload.performUpload();
//            }
//        } );

        setPageComplete( false );
        
        this.upload.setHandler( new IUploadHandler() {
            public void uploadStarted( final String name, String contentType, final InputStream in ) throws Exception {
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        try {
                            Charset charset = Charset.forName( charsetName );
                            final List<File> files = new FileImporter()
                                    .setCharset( charset )
                                    .setOverwrite( false )
                                    .doRun( name, null, in );

                            for (File f : files) {
                                viewer.add( f );
                                //if (f.getName().endsWith( "shp" )) {
                                list.add( f.toURI().toURL() );
                                //}
                            }
                            upload.setText( "" );
                            setPageComplete( true );
                            getContainer().updateButtons();
                        }
                        catch (Exception e) {
                            PolymapWorkbench.handleError( CatalogPlugin.ID, FileUploadPage.this, e.getMessage(), e );
                        }
                    }
                });
            }
        });
    }

    
    @Override
    public void shown() {
        // FIXME _p3: all this complex worklow stuff is very suspicious to me; I don't see
        // the advantages over plain eclipse stuff; So, I don't see where and when this page
        // gets initialized: it seems that it is used over multiple runs of the Import wizard
        // -> the list holds the previous entries here.
        list.clear();
        
//        Runnable openFileDialog = new Runnable(){
//            public void run() {
//                selectAndContinueWizard();
//            }
//        };
//        // file dialog must be opened asynchronously so that the workflow can finish the
//        // next action. Otherwise we will deadlock
//        PlatformGIS.asyncInDisplayThread(openFileDialog, false);
    }

    private void selectAndContinueWizard() {
        boolean okPressed;
        list.clear();
//        okPressed = openFileDialog(comp);
        viewer.setInput(list);
        getContainer().updateButtons();
    }


    @Override
    public Collection<IService> getServices() {
        System.out.println( "FileUploadPage.getServices(): ..." );
        resourceIds.clear();

        final Collection<IService> services = new ArrayList<IService>();
        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            public void run( IProgressMonitor monitor )
                    throws InvocationTargetException, InterruptedException {

                services.addAll( EndConnectionState.constructServices( monitor,
                        new HashMap<String, Serializable>(), list ) );
                for (IService service : services) {
                    try {
                        List<? extends IGeoResource> resources = service.resources( SubMonitor
                                .convert( monitor ) );
                        if (resources.size() == 1) {
                            IGeoResource resource = resources.iterator().next();
                            resourceIds.add( resource.getIdentifier() );
                        }
                    }
                    catch (IOException e) {
                        // skip
                        CatalogUIPlugin.log( "error resolving:" + service.getIdentifier(), e ); //$NON-NLS-1$
                    }
                }
            }

        };
        try {
            getContainer().run( false, true, runnable );
        }
        catch (InvocationTargetException e) {
            throw (RuntimeException)new RuntimeException().initCause( e );
        }
        catch (InterruptedException e) {
            throw (RuntimeException)new RuntimeException().initCause( e );
        }
        return services;
    }

    
    @Override
    public Collection<URL> getResourceIDs() {
        return resourceIds;
    }
    
}
