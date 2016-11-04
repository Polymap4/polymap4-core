/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.ui;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.internal.graphics.ResourceFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.internal.lifecycle.CurrentPhase;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.widgets.WidgetUtil;

import org.polymap.core.runtime.UIThreadExecutor;

/**
 * Static methods that help to work with (RWT specific) settings of the UI.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class UIUtils {

    private static final Log log = LogFactory.getLog( UIUtils.class );
    
    //public static boolean       debug = true;

    
    public static Color getColor( int r, int g, int b ) {
        ResourceFactory resourceFactory = ContextProvider.getApplicationContext().getResourceFactory();
        return resourceFactory.getColor( r, g, b );
    }


//    public static Color getColor( java.awt.Color src ) {
//        HS
//        return getColor( src.getRed(), src.getGreen(), src.getBlue() );
//    }
//
//
//    public static java.awt.Color awtColor( Color src ) {
//        return new java.awt.Color( src.getRed(), src.getGreen(), src.getBlue() );
//    }


    public static Color getColor( RGB rgb ) {
        return getColor( rgb.red, rgb.green, rgb.blue );
    }


    /**
     * The {@link Display} of the session of the current thread. Null, if the current
     * thread has no session. The result is equivalent to
     * {@link Display#getCurrent()} except that the calling thread does not have to
     * be the UI thread of the session.
     * <p/>
     * If you want to execute something in the display thread then consider using
     * {@link UIThreadExecutor}.
     * 
     * @see UIThreadExecutor
     */
    public static Display sessionDisplay() {
        return LifeCycleUtil.getSessionDisplay();    
    }


    public static Shell shellToParentOn() {
        return sessionDisplay().getActiveShell();
    }


    /**
     * Set {@link RWT#CUSTOM_VARIANT} on the given control. Checks if a variant is
     * set already and logs the previous value. This also sets the client side
     * "test-id" to the variant.
     * <p/>
     * This method decouples client code from RWT specific API.
     *
     * @param control
     * @param variant
     */
    public static <T extends Control> T setVariant( T control, String variant ) {
//        Optional.of( control.getData( RWT.CUSTOM_VARIANT ) ).ifPresent( 
//                previous -> log.warn( "Control has variant: " + previous + ", new: " + variant ) );
        
        Object previous = control.getData( RWT.CUSTOM_VARIANT );
        if (previous != null) {
            log.debug( "Control: " + control.hashCode() + ", previous variant: " + previous + ", new: " + variant );
        }
        
        control.setData( RWT.CUSTOM_VARIANT, variant );
        
        if (log.isDebugEnabled()) {
            setAttribute( control, "variant", variant );
        }
        return control;
    }
    
    
    public static <T extends Widget> T setTestId( T widget, String value ) {
        if (log.isDebugEnabled()) {
            setAttribute( widget, "test-id", value );
        }
        return widget;
    }


    public static <T extends Widget> T setAttribute( T widget, String attr, String value ) {
        if (!widget.isDisposed()) {
            String $el = widget instanceof Text ? "$input" : "$el";
            String id = WidgetUtil.getId( widget );
            exec( "rap.getObject( '", id, "' ).", $el, ".attr( '", attr, "', '", value, "' );" );
        }
        return widget;
    }


    public static void exec( String... jscode ) {
        StringBuilder buf = new StringBuilder( 256 )
                .append( "try{" )
                .append( String.join( "", jscode ) )
                .append( "}catch(e){}" );
        
        JavaScriptExecutor executor = RWT.getClient().getService( JavaScriptExecutor.class );
        executor.execute( buf.toString() );
    }


    // http://www.mkyong.com/regular-expressions/how-to-validate-html-tag-with-regular-expression/
    public static final Pattern         htmlTag1 = Pattern.compile( "<(\"[^\"]*\"|'[^']*'|[^'\">])*>" );

    /** No '<>' allowed. Nothing. Nowhere. */
    public static final Pattern         htmlTag2 = Pattern.compile( "<[^>]*>" );
    
    /**
     * Sanitize the given user input string by removing HTML tags. We allow markdown,
     * that's cool! :)
     *
     * @param userInput
     */
    public static String sanitize( String userInput ) {
        return userInput
                .replace( "<", "&lt;" )
                .replace( ">", "&gt;" );
        //return htmlTag2.matcher( userInput ).replaceAll( "" );
    }

    
    /**
     * 
     * @see ServerPushManager
     * @param id
     */
    public static void activateCallback( String id ) {
        assert id != null;
        assert ContextProvider.hasContext() && CurrentPhase.get() != null;
        ServerPushManager.getInstance().activateServerPushFor( id );
    }
    
    
    /**
     * 
     * @see ServerPushManager
     * @param id
     */
    public static void deactivateCallback( String id ) {
        assert id != null;
        assert ContextProvider.hasContext();
        ServerPushManager.getInstance().deactivateServerPushFor( id );
    }

    
    /**
     * Performs the given visitor recursivly on every child of the given parent. The
     * loops stops if visitor returns false.
     *
     * @param parent
     * @param visitor
     */
    public static void visitChildren( Composite parent, Predicate<Control> visitor ) {
        Deque<Control> stack = new ArrayDeque();
        stack.addAll( Arrays.asList( parent.getChildren() ) );
        while (!stack.isEmpty()) {
            Control child = stack.removeLast();
            if (visitor.test( child ) == false) {
                break;
            }
            if (child instanceof Composite) {
                stack.addAll( Arrays.asList( ((Composite)child).getChildren() ) );
            }
        }
    }
    
    
    /**
     * Disposes all children of the given parent. Checks if childs are already
     * disposed.
     */
    public static void disposeChildren( Composite parent ) {
        assert parent != null;
        for (Control child : parent.getChildren()) {
            if (!child.isDisposed()) {
                child.dispose();
            }
        }
    }
    
}
