/*******************************************************************************
 * Copyright (c) 2002-2007 Critical Software S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tiago Rodrigues (Critical Software S.A.) - initial implementation
 *     Joel Oliveira (Critical Software S.A.) - initial commit
 ******************************************************************************/
qx.Class.define( "org.eclipse.rwt.widgets.Upload", {
  extend: qx.ui.layout.VerticalBoxLayout,

  construct : function( servlet, flags ) {
    this.base( arguments );

    this._servlet = servlet;
    this._isStarted = false;
    
    /*
     * Identifies a upload process at the server.
     */
    this._uploadProcessId = "";
    
    /*
     * Is used to retry sending a progress polling
     * request only once.
     */
    this._monitorRequestFailed = false;
    
    this._showProgress = ( flags & 1 ) > 0;
    this._showUploadButton = ( flags & 2 ) > 0;
    this._fireProgressEvents = ( flags & 4 ) > 0;

    this.initHeight();
    this.initOverflow();

    var topLayout = new qx.ui.layout.HorizontalBoxLayout();
    topLayout.set({left:0,right:0,height:"1*"});

    // Upload Form
    this._uploadForm = new uploadwidget.UploadForm("uploadForm", this._servlet);
    // Make the widget use the entire assigned space
    this._uploadForm.set({top:0,left:0,width:"1*", height:"100%"});
    topLayout.add(this._uploadForm);
    

    // Browse File Button
    this._uploadField = new uploadwidget.UploadField("uploadFile", "Browse");
    this._uploadField.set({left:0,right:0});
    this._uploadField.addEventListener( "changeValue", this._onChangeValue, this );
    
    // workaround adjust browse button position
    // Set default width for default text "Browse"
    this._uploadField._button.set({top:0,right:0,height:"100%"});
    this._uploadField._text.set({marginTop:0});

    this._uploadForm.add(this._uploadField);
    
	this._uploadField.setHeight("100%");
	this._uploadField._text.setHeight("100%");

    // Upload Button
    if( this._showUploadButton ) {
      this._uploadButton = new qx.ui.form.Button("Upload");
    	
      // This state is needed for proper button CSS themeing
      this._uploadButton.addState( "rwt_PUSH" );
      this._uploadButton.addEventListener("click", this._uploadFile, this);
      this._uploadButton.set({height:"100%"});
      topLayout.add(this._uploadButton);
    }

    this.add(topLayout);

    if (this._showProgress) {
        // Progress Bar
        this._progressBar = new org.eclipse.swt.widgets.ProgressBar();
        this._progressBar.set({left:0,height:20});
        this._progressBar.setMinimum(0);
        this._progressBar.setMaximum(100);
        this._progressBar.setFlag(org.eclipse.swt.widgets.ProgressBar.FLAG_HORIZONTAL);
        
        this.add(this._progressBar);
    }
    
    if (this._fireProgressEvents) {
	    this._uploadForm.addEventListener("sending", this._monitorUpload, this);
    }
    
    this._uploadForm.addEventListener("completed", this._cleanUp, this);
    this.addEventListener("upload", this._fireEvent, this);
    this.addEventListener( "changeEnabled", this._onEnabled, this );
    this._uploadField._button.addEventListener( "click", this._onFocus, this );
  },
  
  destruct : function() {   
  	// SR: Seems as if _uploadField's button has already been disposed in some cases,
  	// e.g. if the user reloads the application pressing F5.
  	// See 275144: [Upload] Contextual JS errors with the upload widget
    if (this._uploadField._button) {
    	this._uploadField._button.removeEventListener( "click", this._onFocus );
    }
    this.removeEventListener( "changeEnabled", this._onEnabled );
    this._uploadField.removeEventListener( "changeValue", this._onChangeValue );
    
    if( this._showUploadButton ) {
      this._uploadButton.removeEventListener("click", this._uploadFile);
    }
    
    if (this._fireProgressEvents) {
        this._uploadForm.removeEventListener("sending", this._monitorUpload);
    }
    
    this._uploadForm.removeEventListener("completed", this._cleanUp);

    this.removeEventListener("upload", this._fireEvent);
  },        

  events: {
    "upload" : "qx.event.type.DataEvent"
  },

  properties :
  {
    /**
     * The last file that was uploaded.
     */
    lastFileUploaded :
    {
      check : "String",
      init  : ""
    }
  },

  members : {
    _fireEvent : function (e) {
        var wm = org.eclipse.swt.WidgetManager.getInstance();
        var id = wm.findIdByWidget(this);
        var req = org.eclipse.swt.Request.getInstance();
        req.addParameter(id + ".finished", e.getData());
        req.addParameter(id + ".lastFileUploaded", this.getLastFileUploaded());
        req.send();
    },

    _cleanUp : function () {
        if (this._isStarted == true) {
            var filename = this._uploadField.getValue();
            
            if (filename.indexOf("\\") != -1) {
                filename = filename.substr(filename.lastIndexOf("\\") + 1);
            } else if (filename.indexOf("/") != -1) {
                filename = filename.substr(filename.lastIndexOf("/") + 1);
            }                
            
            this.setLastFileUploaded(filename);
            // Stefan Röck: The field shouldn't be cleaned automatically, this can
            // still be achvied by calling reset()
            //this._uploadField.setValue("");
            //if( this._showProgress ) {
            //  this._progressBar.setWidth("0%");
            // }
            
            // make sure, that the progressbar (if visible) is filled completely after 
            // uploading finished
            if (this._showProgress) {
              this._progressBar.setSelection(100);
            }
            this._isStarted = false;
    
            this.createDispatchDataEvent("upload", true);
        }
    },

    /*
     * Each upload process uses a unique id to allow the server
     * to identify the incoming requests to return the correct upload 
     * progress.
     */
    _generateUniqueUploadUrl : function () {
        // generate a new id for this upload
        this._uploadProcessId = new Date().valueOf();
        
        this._uploadForm.setUrl(this._getUniqueUploadUrl());
    },
    
    /*
     * Attaches the upload process id to the servlet url.
     */
    _getUniqueUploadUrl : function () {
        var hasUrlParameter = this._servlet && this._servlet.indexOf("&");
        var newUploadUrl;
        if (hasUrlParameter != -1) {
        	newUploadUrl = this._servlet + "&processId=" + this._uploadProcessId;
        } else {
        	newUploadUrl = this._servlet + "?processId=" + this._uploadProcessId;
        }
    	return newUploadUrl;
    },

    _uploadFile : function () {
        if (this._uploadField.getValue() != "") {
            this._isStarted = true;
            this._monitorRequestFailed = false;
            this._generateUniqueUploadUrl();
            this._uploadForm.send();
        }
    },
    
    _monitorUpload : function () {
    	qx.ui.core.Widget.flushGlobalQueues();
		var req = new qx.io.remote.Request( this._getUniqueUploadUrl(), qx.net.Http.METHOD_GET, qx.util.Mime.TEXT );
		req.setAsynchronous( false );
		req.addEventListener( "completed", this._handleCompleted, this );
		req.send();
    },
    
    _handleCompleted : function( evt ) {
    	var req = evt.getTarget().getImplementation().getRequest();
     	if (req.status == 200) {
  		    var xml = req.responseXML;

            var finished = xml.getElementsByTagName("finished")[0];
            var percentCompleted = xml.getElementsByTagName("percent_complete")[0];

            // Check to see if it's even started yet
            if ((finished == null) && (percentCompleted == null)) {
                if (!this._monitorRequestFailed) {
	                // Try refreshing once only to avoid endless loops
	                this._monitorRequestFailed = true;
	                qx.client.Timer.once(this._monitorUpload, this, 1000);
                }
            }
            else if (finished == null) {
                var bytesRead = xml.getElementsByTagName("bytes_read")[0];
                var contentLength = xml.getElementsByTagName("content_length")[0];

                // Started, get the status of the upload
                if (percentCompleted != null) {
                    
                    if (this._showProgress) {
                        var progress = percentCompleted.firstChild.data;
                        this.debug("New Progress " + progress);
                    	this._progressBar.setSelection(progress);
                    }

                    this.createDispatchDataEvent("upload", false);
                    
                    qx.client.Timer.once(this._monitorUpload, this, 1000);
                }
                else {
                    // Finished
                }
            }
        }
        else {
            this.debug("HTTP Response NOK: "+ this._req.statusText);
        }
    	
    },
    
    _onChangeValue : function( evt ) {
      var wm = org.eclipse.swt.WidgetManager.getInstance();
      var id = wm.findIdByWidget( this );
      var req = org.eclipse.swt.Request.getInstance();
      req.addParameter( id + ".path", evt.getValue() );
      req.send();
    },
    
    _performUpload : function() {
      this._uploadFile();
    },
    
    _resetUpload : function() {
      if (this._progressBar) {
         this._progressBar.setSelection(0);
      }
      if (this._uploadField) {
        this._uploadField.setValue("");
      }
    },
    
    /*
     * This is a workaround as enablement does not work with the base
     * file upload widgets.
     * TODO: [sr] disable/enable doesn't work with IE8.
     */
    _onEnabled : function( evt ) {
      qx.ui.core.Widget.flushGlobalQueues();

      if( evt.getValue() ) {
        this._uploadField._button._input.style.height
          = this._uploadField.getHeight();        
      } else {
        this._uploadField._button._input.style.height = "0px";
      }
    },
    
    /*
     * This is a workaround as the underlying file upload takes the focus
     * despite the hidefocus style settings.
     */
    _onFocus : function( evt ) {
      var input = this._uploadField._button._input;
      this._uploadField._button._input.onblur = function() {
        input.onblur = null;
        input.onfocus = function() { input.blur(), input.onfocus = null };
      };
    },
    
    // TODO [fappel]: would a property be the better solution?
    setBrowseButtonText : function( browseButtonText, width ) {
      this._uploadField._button.setLabel( browseButtonText );
      this._uploadField._button.setWidth( width );
    },
    
    // TODO [fappel]: would a property be the better solution?
    setUploadButtonText : function( uploadButtonText, width ) {
      if( this._showUploadButton ) {
        this._uploadButton.setLabel( uploadButtonText );
        this._uploadButton.setWidth( width );
      }
    }
  }
} );
