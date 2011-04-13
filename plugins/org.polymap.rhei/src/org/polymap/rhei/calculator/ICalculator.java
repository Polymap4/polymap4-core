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
package org.polymap.rhei.calculator;

import java.io.PrintStream;

import org.json.JSONObject;

/**
 * A calculator allows to  
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version POLYMAP3
 * @since 3.0
 */
public interface ICalculator {

    public void setParam( String name, JSONObject json );
    
    public void setParam( String name, Object bean );

    public void setOut( PrintStream out );
    
    public PrintStream getOut();
    
    public void setErr( PrintStream out );
    
    public PrintStream getErr();
    
    public void eval()        
    throws ParseException, TargetException;
    
    public Object getResult( String name );
}
