/* 
 * polymap.org
 * Copyright (C) 2015-2018, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.log;

import org.apache.commons.logging.Log;

/**
 * Implementation of the most commonly used log level - optimized fot JIT inlining.
 * 
 * @see SimpleLogFactory
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class SimpleInfoLog
        implements Log {

    // The instance must not hold any back references to other objects.
    // This helps the GC. Even a SimpleLogFactory reference might be a problem?
    
    private String          name;

    private final LogFormat format;

    
    protected SimpleInfoLog( String name, Level lowestLevel, LogFormat format ) {
        this.name = name;
        this.format = format;
    }

    public Level getLevel() {
        return Level.INFO;
    }

    @Override
    public final void info( Object msg, Throwable e ) {
        format.log( name, Level.INFO, msg, e );
    }
    
    @Override
    public final void info( Object msg ) {
        format.log( name, Level.INFO, msg );
    }
    
    @Override
    public final void warn( Object msg, Throwable e ) {
        format.log( name, Level.WARN, msg, e );
    }
    
    @Override
    public final void warn( Object msg ) {
        format.log( name, Level.WARN, msg );
    }
    
    @Override
    public final void debug( Object msg, Throwable e ) {
    }
    
    @Override
    public final void debug( Object msg ) {
    }
    
    @Override
    public final void trace( Object msg, Throwable e ) {
    }
    
    @Override
    public final void trace( Object msg ) {
    }
    
    @Override
    public final void error( Object msg, Throwable e ) {
        format.log( name, Level.ERROR, msg, e );
    }
    
    @Override
    public final void error( Object msg ) {
        format.log( name, Level.ERROR, msg );
    }
    
    @Override
    public final void fatal( Object msg, Throwable e ) {
        format.log( name, Level.FATAL, msg, e );
    }
    
    @Override
    public final void fatal( Object msg ) {
        format.log( name, Level.FATAL, msg );
    }
    
    /**
     * Are debug messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isDebugEnabled() {
        return false;
    }

    /**
     * Are error messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isErrorEnabled() {
        return true;
    }

    /**
     * Are fatal messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isFatalEnabled() {
        return true;
    }

    /**
     * Are info messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isInfoEnabled() {
        return true;
    }

    /**
     * Are trace messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isTraceEnabled() {
        return false;
    }

    /**
     * Are warn messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isWarnEnabled() {
        return true;
    }
    
}
