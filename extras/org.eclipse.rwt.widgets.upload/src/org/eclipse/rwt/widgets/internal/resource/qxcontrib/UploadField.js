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
 * UploadField: A textfield which holds the filename of the file which
 * should be uploaded and a button which allows selecting the file via the native
 * file selector 
 *
 */
qx.Class.define("uploadwidget.UploadField",
{
  extend : qx.ui.layout.BoxLayout,

  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(name, text, icon, iconHeight, flash)
  {
    this.base(arguments);
    
  	if(name) {
      this.setName(name);
    }

    this.initHeight();
    this.initOverflow();


  	this._text = new qx.ui.form.TextField();
  	this._text.set({readOnly:true,left:0,marginTop:0,paddingTop:0,width:"1*"});
  	this._text.setAppearance( "upload-field" );
  	this.add(this._text);

  	this._button = new uploadwidget.UploadButton(this.getName(), text, icon, iconHeight, flash);
  	this._button.set({right:0});
    this._button.addEventListener("changeValue", this._onChangeValue, this);
  	this.add(this._button);
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
      init  : "",
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
     * refine the initial value of overflow to hidden
     */
    overflow :
    {
      refine : true,
      init   : "hidden"
    },
    
    /**
     * refine the initial value of spacing to 4
     */
    spacing :
    {
      refine : true,
      init   : 3
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
    ------------------------------------------------------------------------------------
      Instance variables
    ------------------------------------------------------------------------------------
    */

    _value : "",


    /*
    ---------------------------------------------------------------------------
      MODIFIERS
    ---------------------------------------------------------------------------
    */
    
    /**
     * Value modifier. Sets the value of both the text field and
     * the UploadButton. The setValue modifier of UploadButton
     * throws an exception if the value is not an empty string.
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyValue : function(value, old) {
      this._button.setValue(value);
      this._text.setValue(value);
    },


    /**
     * Upload parameter value modifier. Sets the name attribute of the
     * the hidden input type=file element in UploadButton which should.
     * This name is the form submission parameter name.
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyName : function(value, old) {
      if(this._button) {
        this._button.setName(value);
      }
    },


    
    /*
    ---------------------------------------------------------------------------
      EVENT HANDLER
    ---------------------------------------------------------------------------
    */
    
    /**
     * If the user select a file by clicking the button, the value of
     * the input type=file tag of the UploadButton changes and
     * the text field is set with the value of the selected filename.
     *
     * @type member
     * @param e {Event} change value event data
     * @return {void}
     */
    _onChangeValue : function(e) {
      var value = e.getValue();
      this._text.setValue(value);
      this.setValue(value);
    }
  },    


  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */
  destruct : function()
  {
    this._disposeObjects("_button", "_text");
  }
});  
