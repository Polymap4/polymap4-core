/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Empty implementation of the {@link IPartListener} interface.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class PartListenerAdapter
        implements IPartListener {

    public void partActivated( IWorkbenchPart part ) {
    }

    public void partBroughtToTop( IWorkbenchPart part ) {
    }

    public void partClosed( IWorkbenchPart part ) {
    }

    public void partDeactivated( IWorkbenchPart part ) {
    }

    public void partOpened( IWorkbenchPart part ) {
    }

}
