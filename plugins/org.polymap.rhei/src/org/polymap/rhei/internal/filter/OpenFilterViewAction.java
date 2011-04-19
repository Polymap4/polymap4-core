/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
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
 * $Id: $
 */
package org.polymap.rhei.internal.filter;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;
import org.polymap.rhei.filter.FilterView;
import org.polymap.rhei.filter.IFilter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class OpenFilterViewAction
        extends OpenFilterAction {

    OpenFilterViewAction( IFilter filter ) {
        super( filter );
        setText( Messages.get( "OpenFilterViewAction_name" ) );
        setToolTipText( Messages.get( "OpenFilterViewAction_tip" ) );
    }

    
    public void run() {
        try {
            FilterView.open( filter );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim Suchen und Öffnen der Ergebnistabelle.", e );
        }
    }

}
