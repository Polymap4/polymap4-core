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
appearances = {
// BEGIN TEMPLATE //
    
    /*
    ---------------------------------------------------------------------------
      UPLOAD
    ---------------------------------------------------------------------------
    */

    "upload-field" : {
      style : function( states ) {
        var tv = new org.eclipse.swt.theme.ThemeValues( states );
        return {
          border : tv.getCssBorder( "Text", "border" ),
          font : tv.getCssFont( "Text", "font" ),
          padding : tv.getCssBoxDimensions( "Text", "padding" ),
          textColor : tv.getCssColor( "Text", "color" ),
          backgroundColor : tv.getCssColor( "Text", "background-color" )
        };
      }
    }
    
// END TEMPLATE //
};
