/*
 * polymap.org Copyright 2009, Polymap GmbH, and individual contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 * 
 * $Id$
 */

package org.polymap.core.project;

import org.qi4j.api.common.Optional;

import org.polymap.core.model.ModelProperty;

/**
 * This general entity feature allows to associate an entity to an {@link IMap}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface ParentMap {

    public static final String      PROP_PARENTMAP = "parentmap";

    /**
     * The children maps association.
     */
    public IMap getMap();

    /**
     * Do not call directly.
     */
    @ModelProperty(PROP_PARENTMAP)
    public void setParentMap( @Optional IMap map );

}