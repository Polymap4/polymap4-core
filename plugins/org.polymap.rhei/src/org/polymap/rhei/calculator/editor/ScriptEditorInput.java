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
package org.polymap.rhei.calculator.editor;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 1.0
 */
public class ScriptEditorInput
        implements IEditorInput {

    private static Log log = LogFactory.getLog( ScriptEditorInput.class );

    private URL             scriptUrl;
    
    private String          lang;
    
    
    public ScriptEditorInput( URL scriptUrl, String lang ) {
        super();
        assert scriptUrl != null : "scriptUrl is null!";

        this.scriptUrl = scriptUrl;
        this.lang = lang;
    }
    
    public URL getScriptUrl() {
        return scriptUrl;
    }

    public String getLang() {
        return lang;
    }
    
    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof ScriptEditorInput) {
            return ((ScriptEditorInput)obj).scriptUrl.equals( scriptUrl );
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return scriptUrl.hashCode();
    }

    public String getEditorId() {
        return ScriptEditor.ID;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "ScriptEditorInput";
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "tooltip";
    }

    public Object getAdapter( Class adapter ) {
        if (URL.class.isAssignableFrom( adapter )) {
            return scriptUrl;
        }
        return null;
    }

}
