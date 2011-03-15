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

package org.polymap.core.workbench;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.internal.ui.UDIGWorkbenchAdvisor;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.lifecycle.IEntryPoint;

import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


/**
 * Allows to start polymap with original udig workbench. For testing.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>29.10.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class UdigWorkbench
        implements IEntryPoint {

    private static final Log log = LogFactory.getLog( UdigWorkbench.class );

    /**
     * 
     */
    public UdigWorkbench() {
        log.info( "..." );
    }


    /*
     *
     */
    public int createUI() {
        log.info( "..." );

        ScopedPreferenceStore prefStore = (ScopedPreferenceStore)PrefUtil.getAPIPreferenceStore();
        String keyPresentationId = IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID;
        String presentationId = prefStore.getString( keyPresentationId );

        WorkbenchAdvisor worbenchAdvisor = new UDIGWorkbenchAdvisor();
//        if (POLYMAP_PRESENTATION.equals( presentationId )) {
//            worbenchAdvisor = new PolymapPresentationWorkbenchAdvisor();
//        }

        Display display = PlatformUI.createDisplay();
        return PlatformUI.createAndRunWorkbench( display, worbenchAdvisor );
    }

}
