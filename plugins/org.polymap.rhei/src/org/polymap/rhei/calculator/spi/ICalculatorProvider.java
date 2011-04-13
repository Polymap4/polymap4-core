/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 */
package org.polymap.rhei.calculator.spi;

import org.polymap.rhei.calculator.ICalculator;
import org.polymap.rhei.calculator.ParseException;
import org.polymap.rhei.calculator.TargetException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version POLYMAP3
 * @since 3.0
 */
public interface ICalculatorProvider {

    /**
     * The ID of this calculator, something like "javascript" or "java".
     */
    public String getId();

    public ICalculator newCalculator( String code );
    
    public void eval( ICalculator calculator )
    throws ParseException, TargetException;
    
}
