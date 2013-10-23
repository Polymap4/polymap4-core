/*
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.openlayers.rap.widget.base;

import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.util.Stringer;

/**
 * Client Side OpenLayers Object Base Class holding a reference to the widget
 * and keeps track of changes to the object
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OpenLayersObject {

    //private OpenLayersWidget widget       = null;

    private String           obj_ref;

    private StringBuilder    obj_mod_code;

    public OpenLayersEvents  events;


    public OpenLayersObject() {
        events = new OpenLayersEvents( this );
    }


    public void addObjModCode( String code ) {
        if (obj_mod_code == null) {
            obj_mod_code = new StringBuilder( 1024 );
        }
        obj_mod_code.append( code );
        changes2widget();
    }

    
    /**
     * Add code that calls the given function with the given arguments.
     * 
     * @param function The name of the function.
     * @param args Can be: {@link String}, kind of {@link Number}, {@link Boolean} or
     *        {@link OpenLayersObject}.
     */
    public void callObjFunction( String function, Object... args ) {
        StringBuilder buf = new StringBuilder( 128 )
                .append( getJSObjRef() ).append( '.' ).append( function ).append( '(' );
        
        for (int i=0; i<args.length; i++) {
            if (i > 0) {
                buf.append( ',' );
            }
            Object arg = args[i];
            if (arg instanceof OpenLayersObject) {
                buf.append( ((OpenLayersObject)arg).getJSObjRef() );
            }
            else if (arg instanceof Number) {
                buf.append( arg.toString() );
            }
            else if (arg instanceof Boolean) {
                buf.append( arg.toString() );
            }
            else if (arg instanceof String) {
                buf.append( '\'' ).append( (String)arg ).append( '\'' );
            }
            else {
                throw new IllegalArgumentException( "Unknown arg type: " + arg ); 
            }
        }
        addObjModCode( buf.append( ");" ).toString() );
    }

    public void addObjModCode( String function, OpenLayersObject obj ) {
        callObjFunction( function, obj );
    }

    public void addObjModCode( String function, OpenLayersObject obj , boolean bool) {
        callObjFunction( function, obj, bool );
    }

    public void addObjModCode( String function, double dbl , boolean bool) {
        callObjFunction( function, dbl, bool );
    }

    public void addObjModCode( String function, int val ) {
        callObjFunction( function, val );
    }

    public void addObjModCode( String function, boolean val ) {
        callObjFunction( function, val );
    }

    public void addObjModCode( String function, double val ) {
        callObjFunction( function, val );
    }


    /**
     * Adds code that sets the value of the given attribute.
     * 
     * @param attr The name of the attribute.
     * @param args Can be: {@link String}, kind of {@link Number}, {@link Boolean} or
     *        {@link OpenLayersObject}.
     */
    public void setObjAttr( String attr, Object arg ) {
        StringBuilder buf = new StringBuilder( 128 )
                .append( getJSObjRef() ).append( '.' ).append( attr ).append( '=' );
        
        if (arg instanceof OpenLayersObject) {
            buf.append( ((OpenLayersObject)arg).getJSObjRef() );
        }
        else if (arg instanceof Number) {
            buf.append( arg.toString() );
        }
        else if (arg instanceof Boolean) {
            buf.append( arg.toString() );
        }
        else if (arg instanceof String) {
            buf.append( '\'' ).append( (String)arg ).append( '\'' );
        }
        else {
            throw new IllegalArgumentException( "Unknown arg type: " + arg ); 
        }
        addObjModCode( buf.append( ';' ).toString() );
    }

//    public void setObjAttr( String attr, OpenLayersObject obj ) {
//        setObjAttr( attr, (Object)obj );
//    }
//
//    public void setObjAttr( String attr, int val ) {
//        addObjModCode( getJSObjRef() + "." + attr + "=" + val + ";" );
//    }
//
//    public void setObjAttr( String attr, String val ) {
//        addObjModCode( getJSObjRef() + "." + attr + "='" + val + "';" );
//    }
//
//    public void setObjAttr( String attr, boolean val ) {
//        addObjModCode( getJSObjRef() + "." + attr + "=" + val + ";" );
//    }
//
//    public void setObjAttr( String attr, double val ) {
//        addObjModCode( getJSObjRef() + "." + attr + "=" + val + ";" );
//    }

    
    public void createCSS(String name,String css) {
        addObjModCode( "  var css=\"  " + name + " { " + css + " } \" ; var p= document.getElementsByTagName('head')[0] ;   var el= document.createElement('style');  el.type= 'text/css';   el.media= 'screen';       if(el.styleSheet) el.styleSheet.cssText= css;  else el.appendChild(document.createTextNode(css));    p.appendChild(el); " );
    }
    
/*
   public OpenLayersWidget getWidget() {
        if (widget == null) {
            OpenLayersWidgetProvider wp = OpenLayersWidgetProvider.getInstance();
            this.widget = wp.getWidget();
        }
        return widget;
    }
*/

    public void create( String js_create_code ) {
        OpenLayersSessionHandler wp = OpenLayersSessionHandler.getInstance();
        this.setObjRef( wp.generateObjectReference( "o", this ) );
        OpenLayersSessionHandler.getInstance().addCommand( new OpenLayersCommand( getJSObjRef() + "=" + js_create_code) );
    }
    
    public void create_with_widget( String js_create_code,OpenLayersWidget widget ) {
        OpenLayersSessionHandler wp = OpenLayersSessionHandler.getInstance();
        this.setObjRef( wp.generateObjectReference( "o", this ) );
        OpenLayersSessionHandler.getInstance().addCommand( new OpenLayersCommand( getJSObjRef() + "=" + js_create_code,widget) );
    }


    public void changes2widget() {
//        if (getWidget() != null) {
            if (obj_mod_code != null) {
                OpenLayersSessionHandler.getInstance().addCommand( new OpenLayersCommand( 
                        new Stringer( "obj=", getJSObjRef(), "; ", obj_mod_code ).toString() ) );
                obj_mod_code = null;
            }
//        }
    }


    public void setObjRef( String obj_ref ) {
        this.obj_ref = obj_ref;
    }


    public String getObjRef() {
        return obj_ref;
    }


    public String getJSObjRef() {
        return "objs['" + obj_ref + "']";
    }


    public String getJSObj( OpenLayersObject object ) {
        if (object == null)
            return "null";
        else
            return object.getJSObjRef();
    }


    public String getJSObj( OpenLayersObject[] oa ) {
        if (oa == null)
            return "[null]";
        else {
            String res = "[";
            for (OpenLayersObject obj : oa) {
                if (!res.equals( "[" ))
                    res += ",";
                res += getJSObj( obj );
            }
            return res + "]";
        }

    }

    /**
     * 
     * dispose the object
     * 
     */
    public void dispose() {
    	 OpenLayersSessionHandler.getInstance().obj_ref2obj.remove( getJSObjRef());
    	 addObjModCode( getJSObjRef() +"=null;" );
    }
}
