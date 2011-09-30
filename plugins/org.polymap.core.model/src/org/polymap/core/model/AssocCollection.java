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

package org.polymap.core.model;

import java.util.Collection;
import java.util.Iterator;

/**
 * The entities of the domain model provide "to-many" associations via an
 * {@link Iterable}. This helps the model framework implementation to lazily
 * fetch entities. On the other hand it lacks support for Collection operations
 * like {@link Collection#isEmpty()}. The {@link AssocCollection} provides
 * helper methods to bridge this gap - without exposing the internal structure.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface AssocCollection<E>
        extends Collection<E> {

    public boolean isEmpty();

    public Iterator<E> iterator();

    public int size();
    
}
