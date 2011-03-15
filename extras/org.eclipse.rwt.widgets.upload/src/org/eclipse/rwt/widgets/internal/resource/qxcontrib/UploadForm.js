/* ************************************************************************

   qooxdoo - the new era of web development

   http://qooxdoo.org

   Copyright:
     2007 Visionet GmbH, http://www.visionet.de

   License:
     LGPL: http://www.gnu.org/licenses/lgpl.html
     EPL: http://www.eclipse.org/org/documents/epl-v10.php
     See the LICENSE file in the project's top-level directory for details.

   Authors:
     * Dietrich Streifert (level420)

************************************************************************ */

/* ************************************************************************

#module(uploadwidget_ui_io)
#require(qx.xml.Document)

************************************************************************ */

/**
 * An upload widget implementation capable of holding multiple upload buttons
 * and upload fields.
 * 
 * Each upload form creates an iframe which is used as a target for form submit.
 * 
 *
 */
qx.Class.define("uploadwidget.UploadForm",
{
  extend : qx.ui.layout.CanvasLayout,

  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  /**
   * @param name {String} form name ({@link #name}).
   * @param url {String} url for form submission ({@link #url}).
   * @param encoding {String} encoding for from submission. This is an instantiation only parameter and defaults to multipart/form-data
   */
  construct : function(name, url, encoding)
  {
    this.base(arguments);

    // Apply initial values
    if(name) {
      this.setName(name);
    }
    
    if(url) {
      this.setUrl(url);
    }

    this.setHtmlProperty("encoding", encoding || "multipart/form-data");

    // Initialize Properties
    this.initHeight();
    this.initWidth();
    this.initSelectable();
    this.initMethod();


    // Initialize Variables
    this._parameters = {};
    this._hidden = {};

    // create a hidden iframe which is used as form submission target
    this._createIFrameTarget();
  },


  /*
  *****************************************************************************
     EVENTS
  *****************************************************************************
  */

  events:
  {
    "sending"    : "qx.event.type.Event",
    "completed"  : "qx.event.type.Event"
  },


  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    /**
     * The name which is assigned to the form
     */
    name :
    {
      check    : "String",
      init     : "",
      apply    : "_applyName"
    },

    /**
     * The url which is used for form submission.
     */
    url :
    {
      check    : "String",
      init     : "",
      apply    : "_applyUrl"
    },

    /**
     * The target which is used for form submission.
     */
    target :
    {
      check    : "String",
      init     : "",
      apply    : "_applyTarget"
    },

    /**
     * Determines what type of request to issue (GET or POST).
     */
    method :
    {
      check : [ qx.net.Http.METHOD_GET, qx.net.Http.METHOD_POST ],
      init  : qx.net.Http.METHOD_POST,
      apply : "_applyMethod"
    },

    /**
     * refine the initial value of height to auto
     */
    height:
    {
      refine : true,
      init   : "auto"
    },

    /**
     * refine the initial value of width to auto
     */
    width:
    {
      refine : true,
      init   : "auto"
    },

    /**
     * refine the initial value of selectable to true
     */
    selectable :
    {
      refine : true,
      init   : true
    }
  },
  

  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {


    /*
    ---------------------------------------------------------------------------
      APPLY ROUTINES
    ---------------------------------------------------------------------------
    */

    _applyName : function(value, old) {
      this.setHtmlProperty("name", value);
    },
    
    _applyUrl : function(value, old) {
      this.setHtmlProperty("action", value);
    },

    _applyTarget : function(value, old) {
      this.setHtmlProperty("target", value);
    },

    _applyMethod : function(value, old) {
      this.setHtmlProperty("method", value);
    },
    
    
    /*
    ---------------------------------------------------------------------------
      UTILITIES
    ---------------------------------------------------------------------------
    */

    /**
     * Create a hidden iframe which is used as target for the form submission.
     * Don't need a src attribute, if it was set to javascript:void we get an insecure
     * objects error in IE.
     *
     * @type member
     * @return {void}
     */
    _createIFrameTarget : function()
    {
      var frameName = "frame_" + (new Date).valueOf();

      if (qx.core.Variant.isSet("qx.client", "mshtml"))
      {
        this._iframeNode = document.createElement('<iframe name="' + frameName + '"></iframe>');
      } else {
        this._iframeNode = document.createElement("iframe");
      }

      this._iframeNode.id = this._iframeNode.name = frameName;
      this._iframeNode.style.display = "none";
      this.setTarget(frameName);

      document.body.appendChild(this._iframeNode);

      this._iframeNode.onload = qx.lang.Function.bind(this._onLoad, this);
      this._iframeNode.onreadystatechange = qx.lang.Function.bind(this._onReadyStateChange, this);
    },
    
    
    /**
     * Create an empty form widget. IE is not able to change the enctype
     * after element creation, so the enctype is set by using a skeleton
     * form tag as parameter for createElement.
     *
     * @type member
     * @return {void}
     */
    _createElementImpl : function() {
      var tagName;
      if (qx.core.Variant.isSet("qx.client", "mshtml")) {
        tagName ='<form enctype="' + this.getHtmlProperty("encoding") + '"></form>';
      } else {
        tagName = 'form';
      }
      
      this.setElement(this.getTopLevelWidget().getDocumentElement().createElement(tagName));
    },


    /**
     * Add parameters as hidden fields to the form.
     *
     * @type member
     * @return {object}
     */
    _addFormParameters : function() {
      var form = this.getElement();
      var parameters = this.getParameters();

      for (var id in parameters) {
        form.appendChild(this._hidden[id]);
      }
    },


    /**
     * Create an input element of type hidden with the 
     * name ({@link #name}) and value ({@link #value})
     *
     * @type member
     * @param name {String} name attribute of the created element ({@link #name}).
     * @param value {String} value attribute of the created element ({@link #value}).
     * @return {void}
     */
    _createHiddenFormField : function(name,value) {
      var hvalue = document.createElement("input");
      hvalue.type = "hidden";
      hvalue.name = name;
      hvalue.value = value;
    
      return hvalue;
    },


    /**
     * Set a request parameter which is stored as an input type=hidden.
     * 
     * @param id String identifier of the parameter to add.
     * @param value String Value of parameter.
     * @return {void}
     */
    setParameter : function(id, value) {
      this._parameters[id] = value;
      if(this._hidden[id] && this._hidden[id].name) {
        this._hidden[id].value = value;
      }
      else {
        this._hidden[id] = this._createHiddenFormField(id, value);
      }
    },


    /**
     * Remove a parameter from the request.
     * 
     * @param id String identifier of the parameter to remove.
     * @return {void}
     */
    removeParameter : function(id) {
      delete this._parameters[id];
      if(this._hidden[id] && this._hidden[id].parentNode) {
        this._hidden[id].parentNode.removeChild(this._hidden[id]);
      }
      delete this._hidden[id];
    },


    /**
     * Get a parameter in the request.
     * 
     * @param id String identifier of the parameter to get.
     * @return {String}
     */
    getParameter : function(id) {
      return this._parameters[id] || null;
    },

    
    /**
     * Returns the array containg all parameters for the request.
     * 
     * @return {Array}
     */
    getParameters : function() {
      return this._parameters;
    },


    /**
     * Send the form via the submit method. Target defaults to the
     * self created iframe.
     * 
     * @return {void}
     */
    send :  function() {
      var form = this.getElement();
      if(form) {
        this._addFormParameters();

        form.submit();

        this._isSent = true;
        this.createDispatchEvent("sending");
      }
      else {
        throw new Error("Form element not created! Unable to call form submit!");
      }
    },



    /*
    ---------------------------------------------------------------------------
      FRAME UTILITIES
    ---------------------------------------------------------------------------
    */
    
    /**
     * Get the DOM window object of the target iframe.
     *
     * @type member
     * @return {DOMWindow} The DOM window object of the iframe.
     */
    getIframeWindow : function() {
      return qx.html.Iframe.getWindow(this._iframeNode);
    },

    
    /**
     * Get the DOM document object of the target iframe.
     *
     * @type member
     * @return {DOMDocument} The DOM document object of the iframe.
     */
    getIframeDocument : function() {
      return qx.html.Iframe.getDocument(this._iframeNode);
    },
    

    /**
     * Get the HTML body element of the target iframe.
     *
     * @type member
     * @return {Element} The DOM node of the <code>body</code> element of the iframe.
     */
    getIframeBody : function() {
      return qx.html.Iframe.getBody(this._iframeNode);
    },

    
    /**
     * Get the target iframe Element.
     *
     * @type member
     * @return {Element} The DOM element of the iframe.
     */
    getIframeNode : function() {
      return this._iframeNode;
    },



    /*
    ---------------------------------------------------------------------------
      RESPONSE DATA SUPPORT
    ---------------------------------------------------------------------------
    */
    
    /**
     * Get the text content of the target iframe. 
     *
     * @type member
     * @return {String} The text response of the submit.
     */
    getIframeTextContent : function() {
      var vBody = this.getIframeBody();
    
      if (!vBody) {
        return null;
      }
    
      // Mshtml returns the content inside a PRE
      // element if we use plain text
      if (vBody.firstChild && (vBody.firstChild.tagName == "PRE" || vBody.firstChild.tagName == "pre"))
      {
        return vBody.firstChild.innerHTML;
      }
      else
      {
        return vBody.innerHTML;
      }
    },
    

    /**
     * Get the HTML content of the target iframe. 
     *
     * @type member
     * @return {String} The html response of the submit.
     */
    getIframeHtmlContent : function() {
      var vBody = this.getIframeBody();
      return vBody ? vBody.innerHTML : null;
    },
    

    /**
     * Get the XML content of the target iframe. 
     * 
     * This is a hack for now because I didn't find a way
     * to send XML via the iframe response.
     * 
     * In the resulting text all occurences of the &lt;
     * and &gt; entities are replaces by < and > and
     * the Text is then parsed into a XML-Document instance.
     *
     * @type member
     * @return {Document} The XML response of the submit.
     */
    getIframeXmlContent : function() {
      var responsetext = this.getIframeTextContent();
    
      if(!responsetext || responsetext.length == 0) {
        return null;
      }
    
      var xmlContent = null;
      var newText = responsetext.replace(/&lt;/g,"<");
      newText = newText.replace(/&gt;/g, ">");
    
      try {
        xmlContent = qx.xml.Document.fromString(newText);
      }
      catch(ex) {};
    
      return xmlContent;
    },



    /*
    ---------------------------------------------------------------------------
      EVENT HANDLER
    ---------------------------------------------------------------------------
    */
    
    /**
     * Catch the onreadystatechange event of the target iframe.
     *
     * @type member
     * @param e {Event}
     * @return {void}
     */
    _onReadyStateChange : function(e) {
      if (this.getIframeNode().readyState == "complete" && this._isSent) {
        this.createDispatchEvent("completed");
        delete this._isSent;
      }
    },

    
    /**
     * Catch the onload event of the target iframe
     *
     * @type member
     * @param e {Event}
     * @return {void}
     */
    _onLoad : function(e) {
      if(this._isSent) {
        this.createDispatchEvent("completed");
        delete this._isSent;
      }
    }
  },

  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function()
  {
    if (this._iframeNode)
    {
      try
      {
        document.body.removeChild(this._iframeNode);
        this._iframeNode.onreadystatechange = null;
        this._iframeNode.onload = null;
        this._iframeNode = null;
      }
      catch (exc)
      {
        this.warn("can't remove iframe node from dom.");
      }
    }
  
    this._parameters = null;
  
    for (var id in this._hidden)
    {
      if(this._hidden[id] && this._hidden[id].parentNode)
      {
        this._hidden[id].parentNode.removeChild(this._hidden[id]);
      }
    }
    
    this._hidden = null;
  }
});  

