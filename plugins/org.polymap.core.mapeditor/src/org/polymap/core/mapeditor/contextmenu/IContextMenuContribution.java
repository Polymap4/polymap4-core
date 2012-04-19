/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.contextmenu;

import org.eclipse.jface.action.IContributionItem;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContextMenuContribution
        extends IContributionItem {

    public static final String      GROUP_TOP = "top";
    public static final String      GROUP_HIGH = "high";
    public static final String      GROUP_MID = "mid";
    public static final String      GROUP_LOW = "low";
    
    public static int           PRIO_LOW = -1000;
    public static int           PRIO_MEDIUM = 0;
    public static int           PRIO_HIGH = 1000;

    IContextMenuContribution init( ContextMenuSite site );
    
    String getMenuGroup();
    
}
