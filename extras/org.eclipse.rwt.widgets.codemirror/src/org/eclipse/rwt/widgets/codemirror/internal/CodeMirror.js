/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam. All rights reserved.
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

var loadedScripts = new Object();

/**
 * Loads additional javascript in the document.
 * 
 * @param url
 */
function loadScript( url, callback, context ) {
    if (loadedScripts[url] != null) {
        // mimic the deferred callback call
        setTimeout( function() { callback( context ); }, 10 );
        return;
    }
    loadedScripts[url] = url;
    
    var script = document.createElement( "script" );
    script.type = "text/javascript";

    if (callback != null) {
        if (script.readyState) { // IE
            script.onreadystatechange = function() {
                if (script.readyState == "loaded" || script.readyState == "complete") {
                    script.onreadystatechange = null;
                    callback( context );
                }
            };
        } 
        else { // Others
            script.onload = function() {
                callback( context );
            };
        }
    }
    script.src = url;
    document.getElementsByTagName( "head" )[0].appendChild( script );
}

/**
 * Loads additional CSS in the document.
 * 
 * @param url
 */
function loadCSS( url ) {
    if (loadedScripts[url] != null) {
        return;
    }
    loadedScripts[url] = url;

    var ref = document.createElement( "link" );
    ref.setAttribute( "rel", "stylesheet" );
    ref.setAttribute( "type", "text/css" );
    ref.setAttribute( "href", url );
    document.getElementsByTagName("head")[0].appendChild( ref );
}


/**
 * JavaScript of the CodeMirror RWT Widget
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
qx.Class.define( "org.eclipse.rwt.widgets.CodeMirror", {
	extend : qx.ui.layout.CanvasLayout,

	construct : function( id ) {
		this.base( arguments );
		this.setHtmlAttribute( "id", id );
        this.set( { backgroundColor : "white" });
		this._id = id;
        this._codeMirror = null;
        this._libLoaded = false;
	},

	properties : {
	    text : {
            check : "String",
            init : null,
            apply : "_applyText",
            event : "_applyText"
        }
	},

	members : {
        /**
         * Lazy init function.
         */
	    _init : function( elm ) {
	        var context = this;
	        this._codeMirror = CodeMirror( elm, {
	            value: "text text text...",
	            mode: "text/x-java",
	            indentUnit: 4,
	            lineNumbers: true,
	            matchBrackets: true,
                onChange: function( codeMirror ) { context._onChange(); },
                onCursorActivity: function( codeMirror ) { context._onFocus(); }
	        });
	        //alert( "text after _init: " + this.getText() );
	        if (this.getText() != null) {
	            this._codeMirror.setValue( this.getText() );
	        }
	    },
	    
	    /**
	     * Called by CodeMirror, tell qooxdoo widget that we have to focus.
	     */
	    _onFocus : function() {
	        var shell = null;
	        var parent = this.getParent();
	        //alert( parent );
	        while (shell == null && parent != null) {
	            if (parent.classname == "org.eclipse.swt.widgets.Shell") {
	                shell = parent;
	            }
	            parent = parent.getParent();
	        }
	        if (shell != null) {
	            shell.setActiveChild( this );
	        }
	    },
	    
	    /**
	     * 
	     */
	    loadLib : function( lib_url ) {
	        //alert( "lib_url= " + lib_url );
            
	        loadCSS( lib_url + "&res=lib/codemirror.css" );
            loadCSS( lib_url + "&res=mode/clike/clike.css" );

            loadScript( lib_url + "&res=lib/codemirror.js", function( context ) {
                loadScript( lib_url + "&res=mode/clike/clike.js", function( context ) {
                    qx.ui.core.Widget.flushGlobalQueues();

                    if (!org_eclipse_rap_rwt_EventUtil_suspend) {
                        context._init( document.getElementById( context._id ) );

                        var widgetId = org.eclipse.swt.WidgetManager.getInstance().findIdByWidget( context );
                        var req = org.eclipse.swt.Request.getInstance();
                        req.addParameter( widgetId + ".load_lib_done", "true" );
                        req.send();
                        this._libLoaded = true;
                    }
                }
                , context ); // loadScript
            }
			, this ); // loadScript
		},
		
		/**
		 * Text modifier.
		 *
		 * @type member
		 * @param value {var} Current value
		 * @param old {var} Previous value
		 */
		_applyText : function( value, old ) {
		    //alert( "applyText(): " + this._codeMirror );
		    if (this._codeMirror != null && this._codeMirror.getValue() != value) {
		        this._codeMirror.setValue( value );
		    }
		},
	    
		_onChange : function() {
		    if (!org_eclipse_rap_rwt_EventUtil_suspend && this._codeMirror != null) {
		        this.setText( this._codeMirror.getValue() );
		        
		        var widgetId = org.eclipse.swt.WidgetManager.getInstance().findIdByWidget( this );
		        var req = org.eclipse.swt.Request.getInstance();
		        req.addParameter( widgetId + ".text", this.getText() );
		        // XXX check if server side has a listener
		        //req.send();
		    }
		},
		
        eval : function( code2eval ) {
		    eval( code2eval );
		}
	}

});
