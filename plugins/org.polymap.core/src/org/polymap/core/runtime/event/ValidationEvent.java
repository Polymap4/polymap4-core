/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.runtime.event;

import java.util.EventObject;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ValidationEvent
        extends EventObject {
    private static final long serialVersionUID = 5882085604264672738L;
    private final int severity;
    private final String message;

    /**
     * @param source
     */
    public ValidationEvent( Object source, int severity, String message ) {
        super( source );
        this.severity = severity;
        this.message = message;
    }
    
    /**
     * @return the severity
     */
    public int getSeverity() {
        return severity;
    }

    
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
