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
 * 
 * @see SimpleLogFactory
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class SimpleLog
        implements Log {

    // The instance must not hold any back references to other objects.
    // This helps the GC. Even a SimpleLogFactory reference might be a problem?
    
    private String              name;

    private final Level         lowestLevel;
    
    private final LogFormat     format;

    
    protected SimpleLog( String name, Level lowestLevel, LogFormat format ) {
        this.name = name;
        this.lowestLevel = lowestLevel;
        this.format = format;
    }

    public Level getLevel() {
        return lowestLevel;
    }

    @Override
    public final void info( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.INFO )) {
            format.log( name, Level.INFO, msg, e );
        }
    }
    
    @Override
    public final void info( Object msg ) {
        if (isLevelEnabled( Level.INFO )) {
            format.log( name, Level.INFO, msg );
        }
    }
    
    @Override
    public final void warn( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.WARN )) {
            format.log( name, Level.WARN, msg, e );
        }
    }
    
    @Override
    public final void warn( Object msg ) {
        if (isLevelEnabled( Level.WARN )) {
            format.log( name, Level.WARN, msg );
        }
    }
    
    @Override
    public final void debug( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.DEBUG )) {
            format.log( name, Level.DEBUG, msg, e );
        }
    }
    
    @Override
    public final void debug( Object msg ) {
        if (isLevelEnabled( Level.DEBUG )) {
            format.log( name, Level.DEBUG, msg );
        }
    }
    
    @Override
    public final void trace( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.TRACE )) {
            format.log( name, Level.TRACE, msg, e );
        }
    }
    
    @Override
    public final void trace( Object msg ) {
        if (isLevelEnabled( Level.TRACE )) {
            format.log( name, Level.TRACE, msg );
        }
    }
    
    @Override
    public final void error( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.ERROR )) {
            format.log( name, Level.ERROR, msg, e );
        }
    }
    
    @Override
    public final void error( Object msg ) {
        if (isLevelEnabled( Level.ERROR )) {
            format.log( name, Level.ERROR, msg );
        }
    }
    
    @Override
    public final void fatal( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.FATAL )) {
            format.log( name, Level.FATAL, msg, e );
        }
    }
    
    @Override
    public final void fatal( Object msg ) {
        if (isLevelEnabled( Level.FATAL )) {
            format.log( name, Level.FATAL, msg );
        }
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
        return isLevelEnabled( Level.DEBUG );
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
        return isLevelEnabled( Level.ERROR );
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
        return isLevelEnabled( Level.FATAL );
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
        return isLevelEnabled( Level.INFO );
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
        return isLevelEnabled( Level.TRACE );
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
        return isLevelEnabled( Level.WARN );
    }
    
    protected boolean isLevelEnabled( Level level ) {
        return level.ordinal() >= lowestLevel.ordinal();
    }
    
}
