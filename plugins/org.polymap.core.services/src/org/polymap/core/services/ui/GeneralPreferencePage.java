/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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
package org.polymap.core.services.ui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.core.runtime.preferences.InstanceScope;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.services.Messages;
import org.polymap.core.services.ServicesPlugin;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeneralPreferencePage
        extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private ScopedPreferenceStore   prefStore;

    
    public GeneralPreferencePage() {
        super( GRID );
        setDescription( Messages.get( "GeneralPreferencesPage_description" ) );
        
        // see ServicesPlugin
        prefStore = new ScopedPreferenceStore( new InstanceScope(), ServicesPlugin.getDefault().getBundle().getSymbolicName() );
        setPreferenceStore( prefStore );
    }


    public void init( IWorkbench workbench ) {
    }


    public boolean performOk() {
        super.performOk();
        try {
            prefStore.save();
            return true;
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( ServicesPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            return false;
        }
    }


    protected Control createContents( Composite parent ) {
        // check admin
        if (!SecurityUtils.isAdmin( Polymap.instance().getPrincipals() )) {
            Label msg = new Label( parent, SWT.None ); 
            msg.setText( Messages.get( "GeneralPreferencesPage_noAccess" ) );
            return msg;
        }
        // normal content
        else {
            return super.createContents( parent );
        }
    }


    protected void createFieldEditors() {
        Composite fieldParent = getFieldEditorParent();
        StringFieldEditor proxyUrlEditor = new StringFieldEditor(
                ServicesPlugin.PREF_PROXY_URL, Messages.get( "GeneralPreferencesPage_proxyUrlLabel" ), 
                 fieldParent );
        addField( proxyUrlEditor );
        proxyUrlEditor.getLabelControl( fieldParent ).setToolTipText( Messages.get( "GeneralPreferencesPage_proxyUrlTip" ) );
        proxyUrlEditor.getTextControl( fieldParent ).setToolTipText( Messages.get( "GeneralPreferencesPage_proxyUrlTip" ) );
    }

}
