/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.model2;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.polymap.core.model2.runtime.ModelRuntimeException;

/**
 * Denotes that a {@link Property} is immutable. An attempt to modify an immutable
 * property results in a {@link ModelRuntimeException}.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD } )
@Documented
public @interface Immutable {

    boolean value() default true;
   
}
