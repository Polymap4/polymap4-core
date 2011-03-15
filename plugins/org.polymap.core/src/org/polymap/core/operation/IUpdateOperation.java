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

package org.polymap.core.operation;

import org.eclipse.core.commands.operations.IUndoableOperation;

/**
 * The update operation is used to update other user sessions and their data. An
 * {@link IUpdateOperation} is used as a normal operation in one user session.
 * After the user has complete his work he commits the changes. The creates
 * update operation is then used to propagate the changes - without the need to
 * reload the entire model.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface IUpdateOperation
        extends IUndoableOperation {

    /**
     * The returned operation should produce exactly the same results as the
     * operation itself but it must not use any direct references to the
     * underlying model to do so. The update operation is used to update other
     * user sessions and its data.
     */
    public IUndoableOperation createUpdateOperation();
    
}
