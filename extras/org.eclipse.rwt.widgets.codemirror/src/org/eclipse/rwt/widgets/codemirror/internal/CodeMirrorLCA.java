/*
 * polymap.org Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.eclipse.rwt.widgets.codemirror.internal;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.ControlLCAUtil;
import org.eclipse.rwt.lifecycle.ILifeCycleAdapter;
import org.eclipse.rwt.lifecycle.JSWriter;
import org.eclipse.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.rwt.widgets.codemirror.CodeMirror;

/**
 * Widget life cycle adapter of the {@link CodeMirror}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CodeMirrorLCA 
        extends AbstractWidgetLCA
        implements ILifeCycleAdapter {

    private static Log log = LogFactory.getLog( CodeMirrorLCA.class );
    

    /*
     * Initial creation procedure of the widget
     */
    public void renderInitialization( final Widget widget )
            throws IOException {
        JSWriter writer = JSWriter.getWriterFor( widget );
        
        String id = WidgetUtil.getId( widget );
        writer.newWidget( "org.eclipse.rwt.widgets.CodeMirror", new Object[] { id } );
        writer.set( "appearance", "composite" );
        // XXX scrolling should be done by CodeMirror -> the CodeMirror div needs to be
        // resized as the widget is resized
        writer.set( "overflow", "auto" /* "hidden"*/ );
        
        ControlLCAUtil.writeStyleFlags( (CodeMirror)widget );
        writer.call( widget, "loadLib",
                new Object[] { ((CodeMirror)widget).getJSLocation() } );
    }


    public void preserveValues( final Widget widget ) {
        // preserve properties that are inherited from Control
        ControlLCAUtil.preserveValues( ( Control )widget );
        
//        IWidgetAdapter adapter = WidgetUtil.getAdapter( widget );
//        adapter.preserve( PROP_TEXT, ((CodeMirror)widget).getText() );
        
        // only needed for themeing
        WidgetLCAUtil.preserveCustomVariant( widget );
    }


    /*
     * Read the parameters transfered from the client
     */
    public void readData( final Widget widget ) {
        CodeMirror cm = (CodeMirror)widget;
        
//        HttpServletRequest request = ContextProvider.getRequest();
//        log.info( "readData(): " + request.getParameterMap() );

        WidgetAdapter widgetAdapter = adapter( widget );
        if (!widgetAdapter.isJSLoaded()) {
            String load_lib_done = WidgetLCAUtil.readPropertyValue( cm, "load_lib_done" );
            if (load_lib_done != null) {
                widgetAdapter.setJSLoaded( Boolean.valueOf( load_lib_done ).booleanValue() );
            }
        }

        String text = WidgetLCAUtil.readPropertyValue( cm, CodeMirror.PROP_TEXT );
        if (text != null) {
            widgetAdapter.setText( text );
        }
        String cursorpos = WidgetLCAUtil.readPropertyValue( cm, CodeMirror.PROP_CURSOR_POS );
        if (cursorpos != null) {
            widgetAdapter.setCursorPos( Integer.parseInt( cursorpos ) );
        }
        if ("true".equals( WidgetLCAUtil.readPropertyValue( cm, CodeMirror.PROP_SAVE ) )) {
            widgetAdapter.forceSave();
        }
    }


    public void renderChanges( final Widget widget )
            throws IOException {
        CodeMirror cm = (CodeMirror)widget;
        ControlLCAUtil.writeChanges( cm );
         
        JSWriter writer = JSWriter.getWriterFor( widget );
        WidgetAdapter widgetAdapter = adapter( widget );
        
        // issue render commands
        if (widgetAdapter.isJSLoaded()) {
            for (RenderCommand command : widgetAdapter.nextCommands()) {
                command.renderChanges( cm, writer );
            }
        }

        // only needed for custom variants (theming)
        WidgetLCAUtil.writeCustomVariant( widget );
    }


    public void renderDispose( final Widget widget )
            throws IOException {
        JSWriter writer = JSWriter.getWriterFor( widget );
        writer.dispose();
    }


    public void createResetHandlerCalls( String typePoolId )
            throws IOException {
    }


    public String getTypePoolId( Widget widget ) {
        return null;
    }

    
    protected WidgetAdapter adapter( Widget widget ) {
        if (widget instanceof CodeMirror) {
            return (WidgetAdapter)((CodeMirror)widget).getAdapter( WidgetAdapter.class );
        }
        throw new IllegalArgumentException( "No CodeMirror widget: " + widget );
    }
    
    
    /**
     * The intertface exposed by the {@link CodeMirror} widget for internal use
     * be {@link CodeMirrorLCA}. 
     *
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public static interface WidgetAdapter {
    
        public void setText( String text );

        public void forceSave();

        public void setCursorPos( int cursorPos );

        public boolean isJSLoaded();

        public void setJSLoaded( boolean loaded );

        public Iterable<RenderCommand> nextCommands();
        
    }
    
}
