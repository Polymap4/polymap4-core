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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.rwt.widgets.codemirror.internal.CodeMirrorJSService;

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
    protected String            js_location   = CodeMirrorJSService.getBaseUrl();

    protected boolean           load_lib_done = false;

    protected String            text = StringUtils.EMPTY;

    
    static {
        CodeMirrorJSService.register();
    }

    
    public CodeMirror( final Composite parent, final int style ) {
        super( parent, style );
    }

    public CodeMirror( final Composite parent, final int style, String js_location ) {
        super( parent, style );
        this.js_location = js_location;
    }

    
    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom( CodeMirrorLCA.class )) {
            return new CodeMirrorLCA();
        }
        else {
            return super.getAdapter( adapter );
        }
    }


    public String getJSLocation() {
        return js_location;
    }

    public boolean isJSLoaded() {
        return load_lib_done;
    }

    // no layout
    public void setLayout( final Layout layout ) {
    }


    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text != null ? text : StringUtils.EMPTY;
    }
    
}
