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
package org.polymap.core.model2.runtime;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denote {@link UnitOfWork} propagation and boundaries.
 *
 * @deprecated Yet to be supported.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Documented
public @interface UnitOfWorkPropagation {

    public enum Propagation {
        REQUIRES_NEW,
        REQUIRES_NEW_NESTED
    }
    
    public Propagation value();
    
    /** @Deprecated Do we need to support this anyway? */
    public boolean readOnly() default false;
}
