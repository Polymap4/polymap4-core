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
package org.eclipse.rwt.widgets.codemirror;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.rwt.widgets.codemirror.internal.CallMethodCommand;
import org.eclipse.rwt.widgets.codemirror.internal.CodeMirrorJSService;
import org.eclipse.rwt.widgets.codemirror.internal.CodeMirrorLCA;
import org.eclipse.rwt.widgets.codemirror.internal.RenderCommand;
import org.eclipse.rwt.widgets.codemirror.internal.SetPropertyCommand;
import org.eclipse.rwt.widgets.codemirror.internal.CodeMirrorLCA.WidgetAdapter;

/**
 * Widget that provides a <a href="http://codemirror.net">CodeMirror</a> syntax
 * highlighting code editor.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CodeMirror
        extends Composite {

    private static Log log = LogFactory.getLog( CodeMirror.class );

    /** External CodeMirror library location. **/
    private String                  js_location   = CodeMirrorJSService.getBaseUrl();

    private String                  text = StringUtils.EMPTY;
    
    private LineMarkers             lineMarkers = new LineMarkers();

    private TextSelection           selection;
    
    private LCAAdapter              lcaAdapter = new LCAAdapter();

    
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

    public String getJSLocation() {
        return js_location;
    }

    public void setLayout( final Layout layout ) {
    }

    
    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text != null ? text : StringUtils.EMPTY;
        lcaAdapter.pushCommand( new SetPropertyCommand( CodeMirrorLCA.PROP_TEXT, text ) );
    }
    
    
    public LineMarkers lineMarkers() {
        return lineMarkers;    
    }
    
    
    public void setSelection( int start, int end ) {
        selection = new TextSelection( start, end );
        lcaAdapter.pushCommand( new CallMethodCommand( "setSelection", start, end ) );
    }

    public void clearSelection() {
        selection = null;
        lcaAdapter.pushCommand( new CallMethodCommand( "setSelection", 0, 0 ) );
    }
    
    public TextSelection getSelection() {
        return selection;
    }


    /**
     * The internal interface for the {@link CodeMirrorLCA}.
     */
    class LCAAdapter 
            implements WidgetAdapter {

        private boolean                 load_lib_done = false;
        
        private Queue<RenderCommand>    commandQueue = new LinkedList();
        

        public void setText( String text ) {
            CodeMirror.this.text = text;
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
            extends TreeMap<Integer,LineMarker> {

        public void clear() {
            super.clear();
            lcaAdapter.pushCommand( new CallMethodCommand( "clearLineMarkers" ) );
        }

        public LineMarker put( LineMarker marker ) {
            return put( marker.getLine(), marker );
        }
        
        public LineMarker put( Integer line, LineMarker marker ) {
            LineMarker result = super.put( marker.getLine(), marker );

            StringBuilder buf = new StringBuilder( 256 );
            buf.append( "<div style=\"font-weight:bold;display:inline;position:relative;left:0px;top:0px;vertical-align:top;float:left;width:16px;");
//            if (marker.getFgColor() != null) {
//                buf.append( "color:" ).append( marker.getFgColor() ).append( ";" );
//            }
//            if (marker.getIcon() != null) {
                // XXX insert real image URL here
                buf.append( "background:url(" ).append( js_location ).append( "&res=icons/error_tsk.gif);" );
//            }
            buf.append( "\" " );
            if (marker.getText() != null) {
                buf.append( "title=\"" ).append( marker.getText() ).append( "\"" );
            }
            buf.append( "> </div>%N%" );

            lcaAdapter.pushCommand( new CallMethodCommand( "setLineMarker", marker.getLine(), buf.toString() ) );
            return result;
        }

        public void putAll( Map<? extends Integer, ? extends LineMarker> map ) {
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
