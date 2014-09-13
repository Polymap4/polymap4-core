/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.query.grammar;

import org.polymap.core.model2.Composite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BooleanExpression {

    public BooleanExpression[]  children;

    
    public BooleanExpression( BooleanExpression... children ) {
        this.children = children;
    }
    
    /**
     * Evaluates the boolean expression agains a target object.
     *
     * @param target The target object.
     * @return true If boolean expression evaluates to TRUE for the target object.
     */
    public abstract boolean evaluate( Composite target );
    
}
