/*
 * polymap.org
 * Copyright 2011-2012, Falko Bräutigam. All rights reserved.
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
        this._lineMarkers = {};
        
        this.initHeight();
        this.initOverflow();
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
	        var self = this;
	        this._codeMirror = CodeMirror( elm, {
	            value: "text text text...",
	            mode: "text/x-java",
	            theme: "eclipse",
	            indentUnit: 4,
	            lineNumbers: true,
	            matchBrackets: true,
                onChange: function( cm, info ) { self._syncServer( info, false ); },
                onCursorActivity: function( cm, info ) { 
                    self._onFocus( cm, info );
                    self._syncServer( info, false );
                },
                extraKeys: {
                    "Ctrl-S": function( cm ) { self._syncServer( null, true ); }
                }
	        });
	        this._codeMirror.setOption( "theme", "eclipse" );
	        if (this.getText() != null) {
	            this._codeMirror.setValue( this.getText() );
	        }
	    },
	    
	    /**
	     * Called onCursorActivity().
	     */
	    _onFocus : function( cm, info ) {
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
	        loadCSS( lib_url + "&res=lib/codemirror.css" );
            loadCSS( lib_url + "&res=theme/eclipse.css" );

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
	    
		/**
		 * @param info
		 * @param forceSave
		 */
		_syncServer : function( info, forceSave ) {
		    if (!org_eclipse_rap_rwt_EventUtil_suspend && this._codeMirror != null) {
		        var widgetId = org.eclipse.swt.WidgetManager.getInstance().findIdByWidget( this );
		        var req = org.eclipse.swt.Request.getInstance();
		        var sendDelay = 1500;

		        // text
		        if (this._codeMirror.getValue() != this.getText()) {
		            this.setText( this._codeMirror.getValue() );
		            req.addParameter( widgetId + ".text", this.getText() );
		        }
		        // cursorPos
                var cursorPos = this._codeMirror.getCursor( true );
                req.addParameter( widgetId + ".cursorpos", this._codeMirror.indexFromPos( cursorPos ) );
                // save
                if (forceSave) {
                    req.addParameter( widgetId + ".save", "true" );
                    sendDelay = 0;
                }
                
                if (this.sendTimeout) {
                    clearTimeout( this.sendTimeout );
                }
                this.sendTimeout = setTimeout( function() {
                    // XXX check if server side has a listener
                    req.send();
                }, sendDelay );
		    }
		},
		
		/**
		 * 
		 * @param id (String)
		 * @param line (String/int)
         * @param charStart (String/int)
         * @param charEnd (String/int)
		 */
        setLineMarker: function( id, line, charStart, charEnd, textClassName, text ) {
            if (this._codeMirror) {
                if (this._lineMarkers[id] != null) {
                    clearMarker( this._lineMarkers[id] );
                }
                var marker = this._codeMirror.setMarker( parseInt( line )-1, text );
                marker.id = id;
                
                marker.startPos = this._codeMirror.posFromIndex( parseInt( charStart ) );
                marker.endPos = this._codeMirror.posFromIndex( parseInt( charEnd ) );
                if (marker.startPos.ch != marker.endPos.ch) {
                    marker.markedText = this._codeMirror.markText( marker.startPos, marker.endPos, textClassName );
                }
                
                this._lineMarkers[id] = marker;
            }
        },
        
        clearLineMarker: function( id ) {
            var marker = this._lineMarkers[id];
            if (marker) {
                if (marker.markedText) {
                    marker.markedText.clear();
                    // XXX hack: the above method does not always clear everything
                    this._codeMirror.setLineClass( marker, null, null );
                }
                this._codeMirror.clearMarker( marker );
                this._lineMarkers[id] = null;
            }
        },
        
        clearLineMarkers: function() {
            for (var id in this._lineMarkers) {
                this.clearLineMarker( id );
            }
            this._lineMarkers = {};
        },
        
        setSelection: function( start, end ) {
            var startPos = this._codeMirror.posFromIndex( parseInt( start ) );
            var endPos = this._codeMirror.posFromIndex( parseInt( end ) );
            this._codeMirror.setSelection( startPos, endPos );
        },
        
        openCompletions: function( /**/json ) {
            var completions = eval( json );
            var editor = this._codeMirror;
            
            if (this.complete && this.complete.parentNode) {
                this.complete.parentNode.removeChild( this.complete );
            }
            
            // build the select widget
            var complete = document.createElement("div");
            this.complete = complete;
            complete.className = "CodeMirror-completions";
            var sel = complete.appendChild(document.createElement("select"));
            // Opera doesn't move the selection when pressing up/down in a
            // multi-select, but it does properly support the size property on
            // single-selects, so no multi-select is necessary.
            if (!window.opera) sel.multiple = true;
            for (var i=0; i<completions.length; ++i) {
                var opt = sel.appendChild(document.createElement("option"));
                opt.appendChild(document.createTextNode(completions[i].text));
            }
            sel.firstChild.selected = true;
            sel.size = Math.min(15, completions.length);
            //sel.style.padding = '2px';
            
            var pos = editor.cursorCoords();
            complete.style.left = pos.x + "px";
            complete.style.top = pos.yBot + "px";
            complete.style.zIndex = 200000;
            complete.style.position = 'absolute';
            //var parent = document.getElementById( this._id );
            var parent = document.body;
            parent.appendChild(complete);
            // If we're at the edge of the screen, then we want the menu to appear on the left of the cursor.
            var winW = window.innerWidth || Math.max(document.body.offsetWidth, document.documentElement.offsetWidth);
            if(winW - pos.x < sel.clientWidth)
                complete.style.left = (pos.x - sel.clientWidth) + "px";
            // Hack to hide the scrollbar.
            if (completions.length <= 15)
                complete.style.width = (sel.clientWidth - 1) + "px";

            var done = false;
            function close() {
                if (!done) {
                    done = true;
                    complete.parentNode.removeChild(complete);
                }
            }
            function pick() {
                var completion = completions[sel.selectedIndex];
                var start = editor.posFromIndex( completion.replaceStart );
                var end = editor.posFromIndex( completion.replaceEnd );
                //alert( completion.text + " : start=" + completion.replaceStart + " -> " + start );
                
                editor.replaceRange( completion.text, start, end );
                close();
                setTimeout(function() { editor.focus(); }, 50);
            }
            CodeMirror.connect(sel, "blur", close);
            CodeMirror.connect(sel, "keydown", function(event) {
                var code = event.keyCode;
                // Enter
                if (code == 13) {
                    CodeMirror.e_stop(event); 
                    pick();
                }
                // Escape
                else if (code == 27) {
                    CodeMirror.e_stop(event); 
                    close(); 
                    editor.focus();
                }
                else if (code != 38 && code != 40) {
                    close(); editor.focus();
                    // Pass the event to the CodeMirror instance so that it can handle things
                    // like backspace properly.
                    editor.triggerOnKeyDown(event);
                    //setTimeout(function() { CodeMirror.simpleHint(editor, getHints);}, 50);
                }
            });
            CodeMirror.connect(sel, "dblclick", pick);

            sel.focus();
            // Opera sometimes ignores focusing a freshly created node
            if (window.opera) setTimeout(function(){if (!done) sel.focus();}, 100);
            return true;
        },
        
        executeCode : function( code2eval ) {
		    var self = this;
		    alert( code2eval );
		    window.eval( code2eval );
		}
	}

});
