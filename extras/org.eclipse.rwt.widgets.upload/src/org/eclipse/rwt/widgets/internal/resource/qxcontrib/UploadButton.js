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

************************************************************************ */

/**
 * An upload button which allows selection of a file through the browser fileselector.
 *
 */
qx.Class.define("uploadwidget.UploadButton",
{
  extend : qx.ui.form.Button,
  
  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(name, text, icon, iconWidth, iconHeight, flash)
  {
    this.base(arguments, text, icon, iconWidth, iconHeight, flash);
    // This state is needed for proper button CSS themeing
    this.addState( "rwt_PUSH" );
    this.setAppearance( "button" );
    if(name) {
      this.setName(name);
    }

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
      check : "String",
      init : "",
      apply : "_applyName"
    },

    /**
     * The value which is assigned to the form
     */
    value :
    {
      check : "String",
      init : "",
      apply : "_applyValue",
      event : "changeValue"
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
      MODIFIERS
    ---------------------------------------------------------------------------
    */

    /**
     * after the button element is present create the input file tag
     * 
     * @param value {Object} new dom element
     * @param old {Object} old one
     * @type member
     * @return {void}
     */
    _applyElement : function (value, old)
    {
      this.base(arguments, value, old);
      this._createInputFileTag(value);
    },


    /**
     * Modifies the value property of the hidden input type=file element.
     * Only an empty string is accepted for clearing out the value of the
     * selected file.
     * 
     * As a special case for IE the hidden input element is recreated because
     * setting the value is generally not allowed in IE.
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyValue : function(value, old) {
      if(this._valueInputOnChange) {
        delete this._valueInputOnChange;
      }
      else {
        if (!value || value == '') {
          if (qx.core.Variant.isSet("qx.client", "mshtml")) {
            this._createInputFileTag(this.getElement());
          }
          else {
            this._input.value = '';
          }
        }
        else {
          throw new Error("Unable to set value to non null or non empty!");
        }
      }
    },


    /**
     * Modifies the name property of the hidden input type=file element.
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyName : function(value, old) {
      if(this._input) {
        this._input.name = propValue;
      }
    },


    /**
     * Apply the enabled property.
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyEnabled : function(value, old)
    {
      if (this._input) {
        this._input.disabled = value===false;
      }

      return this.base(arguments, value, old);
    },

    /*
    ---------------------------------------------------------------------------
      EVENT-HANDLER
    ---------------------------------------------------------------------------
    */
    
 
    /**
     * Create an input type=file element, and set the onchange event handler which
     * fires if the user selected a file with the fileselector.
     *
     * @type member
     * @param e {Event|null} appear event
     * @return {void}
     */
    _createInputFileTag : function(elem)
    {
      if(this._input)
      {
        this._input.name += "_tmp_";
        if (this._input.parentNode) {
        	this._input.parentNode.removeChild(this._input);
        }
        this._input = null;
      }

      var input  = this._input = document.createElement("input");
      input.type = "file";
      input.name = this.getName();
      input.size = "1"; // make the text area as small as possible
      input.style.position  = "absolute";
      input.style.top		= "-2px"; // make sure to cover the whole area
      input.style.left      = "-100px";
      input.style.height    = "27px";
      input.style.fontSize  = "35px"; // previous value 60 was to small for some systems (depends on used font-family)
      var clipRight = 300 + this.getWidthValue(); //use variable to avoid string concatenation instead of adding
      input.style.clip      = "rect(auto, " + clipRight + "px, auto, 100px)";
      input.style.zIndex    = "100";
      input.style.cursor    = "pointer";
      input.style.filter    = "alpha(opacity=0)";
      input.style.opacity   = "0";
      input.style.MozOutlinestyle   = "none";
      input.style.hidefocus         = "true";
      input.disabled        = this.getEnabled() === false;

      var _this = this;
      input.onchange = function(ev) { return _this._onChange(ev); };

      elem.appendChild(input);
    },


    /**
     * Handle the onchange event of the hidden input type=file element
     *
     * @type member
     * @param e {Event} TODOC
     * @return {void}
     */
    _onChange : function(e) {
      // IE8 returns sth like "C:\fakepath\..." which should not be displayed.
      // Cut off everyhting before the "\", like Firefox does automatically, to have
      // an identical behaviour in all browsers.
      var inputStr = this._input.value;
      if (inputStr) {
        if (inputStr.indexOf("\\") != -1) {
            inputStr = inputStr.substr(inputStr.lastIndexOf("\\") + 1);
        } else if (inputStr.indexOf("/") != -1) {
            inputStr = inputStr.substr(inputStr.lastIndexOf("/") + 1);
        }            
        
        this._valueInputOnChange = true;
	    this.setValue(inputStr);
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
    if(this._input) {
      if (this._input.parentNode) {
    	  this._input.parentNode.removeChild(this._input);
      }    	  
      this._input.onchange = null;
      this._input = null;
    } 
  }

});
