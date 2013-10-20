/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package net.refractions.udig.catalog.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.log4j.lf5.util.StreamUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.internal.Messages;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.ui.upload.IUploadHandler;
import org.polymap.core.ui.upload.Upload;

/**
 * A wizard page that opens a file dialog and closes the wizard when dialog is closed.
 * 
 * @author jeichar
 * @since 0.9.0
 */
public class FileConnectionPage extends AbstractUDIGImportPage implements UDIGConnectionPage {

    private final Set<URL>        list        = new HashSet<URL>();

    private Composite             comp;

    private FileConnectionFactory factory     = new FileConnectionFactory();

    // private FileDialog fileDialog;
    private Collection<URL>       resourceIds = new HashSet<URL>();

    private ListViewer            viewer;
    
    private Upload                upload;


    public String getId() {
        return "net.refractions.udig.catalog.ui.openFilePage"; //$NON-NLS-1$
    }


    /**
     * Construct <code>OpenFilePage</code>.
     */
    public FileConnectionPage() {
        super(Messages.get("OpenFilePage_pageTitle"));
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
        return true;
        //return (list != null && list.size() > 1);
    }

    /**
     *
     */
    public void createControl( Composite parent ) {
        comp = new Composite( parent, SWT.NONE );
        comp.setLayout( new GridLayout( 1, true ) );

        Label label = new Label( comp, SWT.NONE );
        GridDataFactory.swtDefaults().applyTo( label );
        label.setText( "Dateien wählen..." );

        createUpload( comp );
        
        viewer = new ListViewer( comp, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.setContentProvider( new ArrayContentProvider() );
        viewer.setLabelProvider( new UploadItemLabelProvider() );
        GridDataFactory.fillDefaults().grab( true, true ).applyTo( viewer.getControl() );

        setControl( comp );
    }

    /**
     *
     */
    private void createUpload( Composite parent ) {
        upload = new Upload( parent, SWT.NONE, /*Upload.SHOW_PROGRESS |*/ Upload.SHOW_UPLOAD_BUTTON );
        //upload.setBrowseButtonText( "Browse" );
        //upload.setUploadButtonText( "Upload" );

        this.upload.setHandler( new IUploadHandler() {
            @Override
            public void uploadStarted( String name, String contentType, InputStream in ) throws Exception {
                //upload.reset();
                
                OutputStream out = null;
                try {
                    // see Polymap.getWorkspacePath()
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    IWorkspaceRoot root = workspace.getRoot();
                    IPath path = root.getLocation().append( "data/filedata" );

                    File uploadDir = path.toFile();
                    uploadDir.mkdirs();
                    File dest = new File( uploadDir, item.getFileName() );
                    out = new FileOutputStream( dest );
                    StreamUtils.copy( item.getFileInputStream(), out );
                    System.out.println( "FileConnectionPage: ## copied to: " + dest );
                    
                    if (dest.getName().endsWith( "zip" )) {
                        handleZipFile( dest, uploadDir );
                    }
                    else {
                        viewer.add( item );
                        list.add( dest.toURI().toURL() );
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        if (out != null) { out.close(); }
                        item.getFileInputStream().close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

//            public void uploadInProgress( UploadEvent uploadEvent ) {
//                System.out.println( "## partial: " + uploadEvent.getUploadedParcial() );
//                System.out.println( "## total: " + uploadEvent.getUploadedTotal() );
//                int percent = (int)((float)uploadEvent.getUploadedParcial()
//                        / (float)uploadEvent.getUploadedTotal() * 100);
//            }
        });
    }

    
    private void handleZipFile( File f, File uploadDir ) 
            throws IOException {
        ZipFile zipFile = new ZipFile( f, "UTF-8", true );
        Enumeration entries = zipFile.getEntries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)entries.nextElement();

            InputStream zipin = zipFile.getInputStream( entry );
            File dest = new File( uploadDir, entry.getName() );
            OutputStream fileout = new FileOutputStream( dest );
            StreamUtils.copyThenClose( zipin, fileout );
            
            viewer.add( new UploadItem( zipin, "", dest.getName(), "" ) );
            if (dest.getName().endsWith( "shp" )) {
                list.add( dest.toURI().toURL() );
            }
        }
        f.delete();
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
        
//        /*
//         * XXX I'm not liking this. I think the workflow should be used to drive the pages because
//         * by trying to put the buttons it is dependent the implementation of
//         * ConnectionPageDecorator's isPageComplete method as well as what order the
//         * WorkflowWizard's canFinish method is implemented. IE if canFinish does not call
//         * isPageComplete before calling dryRun() the finish button will not be activated.
//         */
//        if (okPressed) {
//            if (findButton(getShell().getChildren(), IDialogConstants.FINISH_ID).isEnabled()) {
//                pushButton(IDialogConstants.FINISH_ID);
//            } else {
//                pushButton(IDialogConstants.NEXT_ID);
//            }
//        } else {
//            pushButton(IDialogConstants.BACK_ID);
//        }
    }

//_p3: no DND    
//    private boolean checkDND( FileDialog fileDialog ) {
//        try {
//
//            Object context = getState().getWorkflow().getContext();
//
//            // IStructuredSelection selection = ((IDataWizard) getWizard()).getSelection();
//
//            Set<URL> urlList = new HashSet<URL>();
//
//            URL url = factory.createConnectionURL(context);
//            if (url != null) {
//                urlList.add(url);
//            }
//
//            if (urlList.size() != 0) {
//                list.addAll(urlList);
//                String file = urlList.iterator().next().getFile();
//                String ext = file.substring(file.lastIndexOf('.'));
//                String dir = new File(file).getParent();
//
//                file = file.substring(file.lastIndexOf(File.separator) + 1);
//                fileDialog.setFilterPath(dir);
//                fileDialog.setFileName(file);
//
//                String[] filters = fileDialog.getFilterExtensions();
//                if (filters == null || filters.length == 0) {
//                    // no filters set, set em up
//                    fileDialog.setFilterExtensions(new String[]{"*" + ext, "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$	
//                } else {
//                    // we have some filters, look for the one in question
//                    // in the list
//                    int i = 0;
//                    for( ; i < filters.length; i++ ) {
//                        if (("*" + ext).equals(filters[i]))break; //$NON-NLS-1$
//                    }
//
//                    if (i < filters.length) {
//                        // we found it, reorganize the array so that
//                        // it is first
//                        String[] nfilters = new String[filters.length];
//                        nfilters[0] = filters[i];
//                        System.arraycopy(filters, 0, nfilters, 1, i);
//                        System.arraycopy(filters, i + 1, nfilters, i + 1, filters.length - i - 1);
//                        fileDialog.setFilterExtensions(nfilters);
//                    } else {
//                        // no dice, add the filter
//                        String[] nfilters = new String[filters.length + 1];
//                        nfilters[0] = "*" + ext; //$NON-NLS-1$
//                        System.arraycopy(filters, 0, nfilters, 1, filters.length);
//                        fileDialog.setFilterExtensions(nfilters);
//                    }
//                }
//
//                return true;
//            }
//        } catch (Exception e) {
//            CatalogUIPlugin.log(e.getLocalizedMessage(), e);
//        }
//
//        return false;
//
//    }

//    public FileDialog getFileDialog() {
//        return fileDialog;
//    }
//
//    private boolean openFileDialog( Composite parent ) {
//        String lastOpenedDirectory = PlatformUI.getPreferenceStore().getString(
//                CatalogUIPlugin.PREF_OPEN_DIALOG_DIRECTORY);
//        
//        fileDialog = new FileDialog(parent.getShell(), SWT.MULTI | SWT.OPEN);
//
//        List<String> fileTypes = factory.getExtensionList();
//
//        StringBuffer all = new StringBuffer();
//        for( Iterator<String> i = fileTypes.iterator(); i.hasNext(); ) {
//            all.append(i.next());
//            if (i.hasNext())
//                all.append(";"); //$NON-NLS-1$ //semicolon is magic in eclipse FileDialog
//        }
//        fileTypes.add(0, all.toString());
//
//        fileTypes.add("*.*"); //$NON-NLS-1$
//
//        fileDialog.setFilterExtensions(fileTypes.toArray(new String[fileTypes.size()]));
//
//        if (lastOpenedDirectory != null && !checkDND(fileDialog)) {
//            fileDialog.setFilterPath(lastOpenedDirectory);
//        }
//
//        // //this is a HACK to check for headless execution
//        // if (getContainer() instanceof HeadlessWizardDialog) {
//        // if (dnd)
//        // return true; //there was a workbench selection that allowed us select a file
//        // }
//        //        
//        String result = fileDialog.open();
//        if (result == null) {
//            return false;
//        }
//        String path = fileDialog.getFilterPath();
//        PlatformUI.getPreferenceStore().setValue(CatalogUIPlugin.PREF_OPEN_DIALOG_DIRECTORY, path);
//        String[] filenames = fileDialog.getFileNames();
//        for( int i = 0; i < filenames.length; i++ ) {
//            try {
//                //URL url = new File(path + System.getProperty("file.separator") + filenames[i]).toURL(); //$NON-NLS-1$
//                URL url = new File(path + System.getProperty("file.separator") + filenames[i]).toURI().toURL(); //$NON-NLS-1$
//                list.add(url);
//            } catch (Throwable e) {
//                CatalogUIPlugin.log("", e); //$NON-NLS-1$
//            }
//        }
//        return true;
//    }

    @Override
    public Collection<IService> getServices() {
        System.out.println( "FileConnectionPage.getServices(): ..." );
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

    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class UploadItemLabelProvider
            extends LabelProvider {

        public String getText( Object element ) {
            UploadItem item = (UploadItem)element;
            return item.getFileName();
        }
        
    }
    
}
