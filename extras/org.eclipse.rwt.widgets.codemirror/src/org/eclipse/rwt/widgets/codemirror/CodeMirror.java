/* 
 * polymap.org
 * Copyright (C) 2011-2014, Falko Bräutigam. All rights reserved.
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
package org.eclipse.rwt.widgets.codemirror;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.rwt.widgets.codemirror.internal.CallMethodCommand;
import org.eclipse.rwt.widgets.codemirror.internal.CodeMirrorJSService;
import org.eclipse.rwt.widgets.codemirror.internal.CodeMirrorLCA;
import org.eclipse.rwt.widgets.codemirror.internal.RenderCommand;
import org.eclipse.rwt.widgets.codemirror.internal.SetPropertyCommand;
import org.eclipse.rwt.widgets.codemirror.internal.CodeMirrorLCA.WidgetAdapter;

import org.eclipse.core.runtime.ListenerList;

/**
 * Widget that provides a <a href="http://codemirror.net">CodeMirror</a> syntax
 * highlighting code editor.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CodeMirror
        extends Control {

    private static Log log = LogFactory.getLog( CodeMirror.class );

    public final static String      PROP_TEXT = "text";
    public final static String      PROP_SELECTION = "selection";
    public final static String      PROP_CURSOR_POS = "cursorpos";
    /** This is a pseudo property that indicated that 'Ctrl-S' was preset on the client. */
    public final static String      PROP_SAVE = "save";
    
    /** External CodeMirror library location. **/
    private String                  js_location   = CodeMirrorJSService.getBaseUrl();

    private String                  text = StringUtils.EMPTY;

    private int                     cursorPos;

    private LineMarkers             lineMarkers = new LineMarkers();

    private TextSelection           selection;
    
    private LCAAdapter              lcaAdapter = new LCAAdapter();
    
    private ListenerList            listeners = new ListenerList();

    
    static {
        CodeMirrorJSService.register();
    }

    
    public CodeMirror( Composite parent, int style ) {
        super( parent, style );
    }

    
    public CodeMirror( Composite parent, int style, String js_location ) {
        this( parent, style );
        this.js_location = js_location;
    }

    
    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom( CodeMirrorLCA.class )) {
            return new CodeMirrorLCA();
        }        
        else if (adapter.isAssignableFrom( WidgetAdapter.class )) {
            return lcaAdapter;
        }
        else {
            return super.getAdapter( adapter );
        }
    }

    /**
     * XXX Fix IControlThemeAdapter getControlWith() issue when using FormLayout
     */
    @Override
    public int getBorderWidth() {
        return 0;
    }


    public String getJSLocation() {
        return js_location;
    }

//    @Override
//    public void setLayout( final Layout layout ) {
//        throw new UnsupportedOperationException( "setLayout()" );
//    }

    
    public String getText() {
        return text;
    }

    
    public void setText( String text ) {
        this.text = text != null ? text : StringUtils.EMPTY;
        lcaAdapter.pushCommand( new SetPropertyCommand( PROP_TEXT, text ) );
        firePropertyEvent( PROP_TEXT, this.text, null );
    }

    
    public int getCursorPos() {
        return cursorPos;
    }

    
    public LineMarkers lineMarkers() {
        return lineMarkers;    
    }
    
    
    public void setSelection( int start, int end ) {
        selection = new TextSelection( start, end );
        lcaAdapter.pushCommand( new CallMethodCommand( "setSelection", start, end ) );
        firePropertyEvent( PROP_SELECTION, getSelection(), null );        
    }

    public void clearSelection() {
        selection = null;
        lcaAdapter.pushCommand( new CallMethodCommand( "setSelection", 0, 0 ) );
        firePropertyEvent( PROP_SELECTION, getSelection(), null );        
    }
    
    public TextSelection getSelection() {
        return selection;
    }

    /**
     * Add a listener to this editor. The following properties fire events:
     * <ul>
     * <li>{@link #PROP_TEXT}</li>
     * <li>{@link #PROP_SELECTION}</li>
     * <li>{@link #PROP_CURSOR_POS}</li>
     * </ul>
     * 
     * @param l The listener to register.
     */
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        listeners.add( l );
    }

    public void removePropertyCHangeListener( PropertyChangeListener l ) {
        listeners.remove( l );
    }

    protected void firePropertyEvent( String prop, Object newValue, Object oldValue ) {
        PropertyChangeEvent ev = new PropertyChangeEvent( this, prop, oldValue, newValue );
        for (Object l : listeners.getListeners()) {
            ((PropertyChangeListener)l).propertyChange( ev );
        }
    }

    public void openCompletions( List<ICompletion> proposals ) {
        StringBuilder json = new StringBuilder( 1024 );
        json.append( '[' );
        for (ICompletion proposal : proposals) {
            json.append( json.length() > 1 ? "," : "" );
            json.append( "{\"text\":\"" ).append( proposal.getCompletion() ).append( "\"," );
            json.append( "\"replaceStart\":" ).append( proposal.getReplaceStart() ).append( "," );
            json.append( "\"replaceEnd\":" ).append( proposal.getReplaceEnd() ).append( "}" );
        }
        json.append( ']' );
        lcaAdapter.pushCommand( new CallMethodCommand( "openCompletions", json.toString() ) );
    }
    
    /**
     * The internal interface for the {@link CodeMirrorLCA}.
     */
    class LCAAdapter 
            implements WidgetAdapter {

        private boolean                 load_lib_done = false;
        
        private Queue<RenderCommand>    commandQueue = new LinkedList();
        

        public void setText( String text ) {
            String old = CodeMirror.this.text;
            CodeMirror.this.text = text;
            firePropertyEvent( PROP_TEXT, CodeMirror.this.text, old );
        }

        public void setCursorPos( int cursorPos ) {
            int old = CodeMirror.this.cursorPos;
            CodeMirror.this.cursorPos = cursorPos;
            firePropertyEvent( PROP_CURSOR_POS, CodeMirror.this.cursorPos, old );
        }

        public void forceSave() {
            firePropertyEvent( PROP_SAVE, Boolean.TRUE, null );
        }

        public boolean isJSLoaded() {
            return load_lib_done;
        }

        public void setJSLoaded( boolean loaded ) {
            load_lib_done = loaded;
        }

        public Iterable<RenderCommand> nextCommands() {
            Queue<RenderCommand> result = commandQueue;
            commandQueue = new LinkedList();
            return result;
        }
        
        void pushCommand( RenderCommand command ) {
            commandQueue.offer( command );
        }
    };
    
    
    /**
     * 
     */
    public class LineMarkers
            extends TreeMap<Integer,ILineMarker> {

        public void clear() {
            super.clear();
            lcaAdapter.pushCommand( new CallMethodCommand( "clearLineMarkers" ) );
        }

        public ILineMarker put( ILineMarker marker ) {
            return put( marker.getLine(), marker );
        }
        
        @SuppressWarnings("restriction")
        public ILineMarker put( Integer line, ILineMarker marker ) {
            ILineMarker result = super.put( marker.getLine(), marker );

            StringBuilder buf = new StringBuilder( 256 );
            buf.append( "<div style=\"font-weight:bold;display:inline;position:relative;left:0px;top:0px;vertical-align:top;float:left;width:16px;");
//            if (marker.getFgColor() != null) {
//                buf.append( "color:" ).append( marker.getFgColor() ).append( ";" );
//            }
            String textClassName = "cm-error";
            if (marker.getIcon() != null) {
                // XXX insert real image URL here
                String imageUrl = "icons/error_tsk.gif";
                if (marker.getIcon().internalImage.getResourceName().contains( "warn" )) {
                    imageUrl = "icons/warn_tsk.gif";
                    textClassName = "cm-warn";
                }
                else if (marker.getIcon().internalImage.getResourceName().contains( "info" )) {
                    imageUrl = "icons/info_tsk.gif";
                    textClassName = "cm-info";
                }
                buf.append( "background:url(" ).append( js_location ).append( "&res=" + imageUrl + ");" );
            }
            buf.append( "\" " );
            if (marker.getText() != null) {
                buf.append( "title=\"" ).append( marker.getText() ).append( "\"" );
            }
            buf.append( "> </div>%N%" );

            lcaAdapter.pushCommand( new CallMethodCommand( "setLineMarker", 
                    marker.getId(), marker.getLine(), 
                    marker.getCharStart(), marker.getCharEnd(), 
                    textClassName, buf.toString() ) );
            return result;
        }

        public void putAll( Map<? extends Integer, ? extends ILineMarker> map ) {
            throw new RuntimeException( "not yet implemented" );
        }
    }
    
    
    /**
     * 
     */
    public class TextSelection {
        
        private int         start, end;

        protected TextSelection( int start, int end ) {
            this.start = start;
            this.end = end;
        }
        
        public int getStart() {
            return start;
        }
        
        public int getEnd() {
            return end;
        }

        public String getText() {
            return StringUtils.substring( text, start, end );
        }
    }
    
}
