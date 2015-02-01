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
 * $Id: $
 */
package org.polymap.core.workbench;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>24.06.2009: created</li>
 * @version $Revision: $
 */
public class PolymapPresentationWorkbenchAdvisor
        extends WorkbenchAdvisor {

    public void initialize( IWorkbenchConfigurer configurer ) {
        getWorkbenchConfigurer().setSaveAndRestore( false );
        super.initialize( configurer );
    }


    public String getInitialWindowPerspectiveId() {
        return "org.eclipse.rap.demo.perspective";
    }


    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
            final IWorkbenchWindowConfigurer windowConfigurer ) {
        return new PolymapPresentationWorkbenchWindowAdvisor( windowConfigurer );
    }
}
