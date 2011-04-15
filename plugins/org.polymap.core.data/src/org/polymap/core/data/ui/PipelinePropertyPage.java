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
package org.polymap.core.data.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;

import org.polymap.core.data.Messages;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.PipelineHolder;
import org.polymap.core.project.PipelineProcessorConfiguration;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.qi4j.operations.SetProcessorConfigurationsOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class PipelinePropertyPage
        extends PropertyPage
        implements IWorkbenchPropertyPage {

    private static Log log = LogFactory.getLog( PipelinePropertyPage.class );

    private ProcessorExtension[]    allExtensions = ProcessorExtension.allExtensions();
        
    private TableViewer             extsTable;
    
    /** Table presenting the {@link #result} elements as content. */
    private TableViewer             procsTable;
    
    private ResultArray<PipelineProcessorConfiguration> result = new ResultArray();

    private Button                  addBtn, removeBtn;
    
    private Composite               propertiesSection;

    private ProcessorPropertyPage   propertyPage;


    static class ResultArray<T>
            implements IStructuredContentProvider {

        private List<T>         content = new ArrayList();
        
        private Viewer          viewer;

        public ResultArray() {
        }
        
        public ResultArray( T[] content ) {
            for (T elm : content) {
                this.content.add( elm );
            }
        }

        public Object[] getElements( Object inputElement ) {
            return content.toArray();
        }

        public List<T> getContent() {
            return content;
            
        }
        
        public void dispose() {
            content = null;
        }

        public void inputChanged( Viewer _viewer, Object oldInput, Object newInput ) {
            this.viewer = _viewer;
        }
        
        public boolean add( T elm ) {
            content.add( elm );
            viewer.refresh();
            return true;
        }

        public boolean remove( T elm ) {
            content.remove( elm );
            viewer.refresh();
            return true;
        }

        public boolean contains( T elm ) {
            return content.contains( elm );
        }
        
    }
    
    public PipelinePropertyPage() {
    }


    public Control createContents( Composite parent ) {
        SashForm composite = new SashForm( parent, SWT.VERTICAL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );
        GridData gdata = new GridData( GridData.FILL );
        gdata.grabExcessHorizontalSpace = true;
        composite.setLayoutData( gdata );
//        composite.setBackground( new Color() )

        createFirstSection( composite );
        createPropsSection( composite );

        updateEnables();

        return composite;
    }


    public boolean performOk() {
        log.info( "performOK()..." );
        if (propertyPage != null) {
            propertyPage.performOk();
        }

        try {
            List<PipelineProcessorConfiguration> content = result.getContent();
            PipelineProcessorConfiguration[] array = (PipelineProcessorConfiguration[]) content.toArray(new PipelineProcessorConfiguration[content.size()]);
            
            SetProcessorConfigurationsOperation op = ProjectRepository.instance().newOperation( 
                    SetProcessorConfigurationsOperation.class );
            op.init( (PipelineHolder)getElement(), array );
            OperationSupport.instance().execute( op, false, false );
            return true;
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            return false;
        }
        
//        setErrorMessage( "Abspeichern der Änderungen ist noch nicht möglich." );
//        return false;
    }

    
    protected void createPropsSection( Composite parent ) {
        Composite composite = new Composite( parent, SWT.NONE );
        FillLayout layout = new FillLayout( SWT.VERTICAL );
//        layout.numColumns = 3;
        composite.setLayout( layout );

        composite.setLayoutData( new GridData( 
                GridData.FILL, GridData.FILL, false, true ) );

        propertiesSection = composite;
    }
    
    
    protected void createFirstSection( Composite parent ) {
        Composite section = new Composite( parent, SWT.NONE );
        RowLayout sectionLayout = new RowLayout( SWT.HORIZONTAL );
        sectionLayout.pack = true;
        sectionLayout.wrap = false;
        sectionLayout.marginLeft = 0;
        sectionLayout.marginRight = 0;
        section.setLayout( sectionLayout );
        
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
//        col.setLayoutData( gridData );


        // pipeline section
        Composite procsSection = new Composite( section, SWT.NONE );
        RowLayout procsLayout = new RowLayout( SWT.VERTICAL );
        procsLayout.pack = true;
        procsLayout.marginLeft = 0;
        procsLayout.marginRight = 0;
        procsSection.setLayout( procsLayout );
        
        Label l = new Label( procsSection, SWT.NONE );
        l.setText( Messages.get( "PipelinePropertyPage_activeProcessors" ) );

        procsTable = new TableViewer( procsSection, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        procsTable.getControl().setLayoutData( new RowData( 120, 140 ) );

        procsTable.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                try {
                    IStructuredSelection sel = (IStructuredSelection)ev.getSelection();
                    showPropertyPage( (PipelineProcessorConfiguration)sel.getFirstElement() );
                    updateEnables();
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, PipelinePropertyPage.this, e.getMessage(), e );
                }
            }
        });
        
        PipelineHolder holder = (PipelineHolder)getElement();
        result = new ResultArray( holder.getProcessorConfigs() );
        procsTable.setContentProvider( result );
        procsTable.setInput( new ArrayList() );

        procsTable.setLabelProvider( new LabelProvider() {
            public String getText( Object elm ) {
                if (elm instanceof PipelineProcessorConfiguration) {
                    return ((PipelineProcessorConfiguration)elm).getName();
                }
                else {
                    return super.getText( elm );
                }
            }
        });
        
        // buttons col
        Composite buttons = new Composite( section, SWT.NONE );
        RowLayout buttonsLayout = new RowLayout( SWT.VERTICAL );
        buttonsLayout.pack = false;
        buttonsLayout.justify = true;
        buttonsLayout.marginTop = 30;
        buttons.setLayout( buttonsLayout );

        // add button
        addBtn = new Button( buttons, SWT.BORDER );
        addBtn.setText( "<< " + Messages.get( "PipelinePropertyPage_add" ) );
        addBtn.setToolTipText( Messages.get( "PipelinePropertyPage_addTip" ) );
        addBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                IStructuredSelection sel = (IStructuredSelection)extsTable.getSelection();
                addProcessor( (ProcessorExtension)sel.getFirstElement() );
                updateEnables();
                extsTable.refresh();
            }
        });
        
        // remove button
        removeBtn = new Button( buttons, SWT.BORDER );
        removeBtn.setText( ">> " + Messages.get( "PipelinePropertyPage_remove" ) );
        removeBtn.setToolTipText( Messages.get( "PipelinePropertyPage_removeTip" ) );
        removeBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                IStructuredSelection sel = (IStructuredSelection)procsTable.getSelection();
                PipelineProcessorConfiguration proc = (PipelineProcessorConfiguration)sel.getFirstElement();
                
                result.remove( proc );
                updateEnables();
                extsTable.refresh();
            }
        });
        
        // extensions section
        Composite extsSection = new Composite( section, SWT.NONE );
        RowLayout extsLayout = new RowLayout( SWT.VERTICAL );
        extsLayout.pack = true;
        extsSection.setLayout( extsLayout );
        
        Label l2 = new Label( extsSection, SWT.None );
        l2.setText( Messages.get( "PipelinePropertyPage_availableProcessors" ) );
        
        // extsTable
        extsTable = new TableViewer( extsSection, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        extsTable.getControl().setLayoutData( new RowData( 120, 140 ) );

        extsTable.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent event ) {
                updateEnables();
            }
        });

        extsTable.setFilters( new ViewerFilter[] {new ViewerFilter() {
            public boolean select( Viewer viewer, Object parentElm, Object elm ) {
                ProcessorExtension ext = (ProcessorExtension)elm;
                log.debug( "filtering: " + ext.getId() );
                if (ext.isTerminal()) {
                    return false;
                }
                for (PipelineProcessorConfiguration proc : result.getContent()) {
                    if (proc.getExtensionId().equals( ext.getId() )) {
                        return false;
                    }
                }
                return true;
            }
        }} );
        extsTable.setContentProvider( new ArrayContentProvider() );
        extsTable.setInput( allExtensions );
        extsTable.setSorter( new ViewerSorter() {
            public int compare( Viewer viewer, Object e1, Object e2 ) {
                ProcessorExtension ext1 = (ProcessorExtension)e1;
                ProcessorExtension ext2 = (ProcessorExtension)e2;
                return ext1.getName().compareTo( ext2.getName() );
            }
        });
        
        extsTable.setLabelProvider( new LabelProvider() {
            public String getText( Object elm ) {
                if (elm instanceof ProcessorExtension) {
                    return ((ProcessorExtension)elm).getName();
                }
                else {
                    return super.getText( elm );
                }
            }
        });

        //createSeparator( composite );
    }

    
    protected void updateEnables() {
        PipelineHolder elm = (PipelineHolder)getElement();
        
        addBtn.setEnabled( !extsTable.getSelection().isEmpty() );
        removeBtn.setEnabled( !procsTable.getSelection().isEmpty() );
    }

    
    protected void showPropertyPage( PipelineProcessorConfiguration config )
    throws CoreException {
        if (propertyPage != null) {
            propertyPage.performOk();
            propertyPage.dispose();
            propertyPage = null;
        }
        if (config == null) {
            return;
        }
        
        ProcessorExtension ext = ProcessorExtension.forExtensionId( config.getExtensionId() );
        try {
            if (ext.hasPropertyPage()) {
                propertyPage = ext.newPropertyPage();

                PipelineHolder elm = (PipelineHolder)getElement();
                propertyPage.init( elm, config.getConfig() );
                propertyPage.createControl( propertiesSection );
            }
            else {
                Label l = new Label( propertiesSection, SWT.NONE );
                l.setText( "No properties to setup." );
            }
            propertiesSection.getParent().layout( true );
            propertiesSection.layout( true );
        }
        catch (Exception e) {
            // XXX Auto-generated catch block
            
        }
    }

    
    protected void addProcessor( ProcessorExtension ext ) {
        PipelineHolder holder = (PipelineHolder)getElement();
        result.add( new PipelineProcessorConfiguration( ext.getId(), ext.getName() ) );
    }
    

    protected Composite createDefaultComposite( Composite parent ) {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout( layout );

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessVerticalSpace = true;
        composite.setLayoutData( data );

        return composite;
    }

    
    protected void createSeparator( Composite parent ) {
        Label separator = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        separator.setLayoutData( gridData );
    }

}
