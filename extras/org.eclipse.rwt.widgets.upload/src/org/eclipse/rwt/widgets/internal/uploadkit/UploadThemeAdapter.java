/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/

package org.eclipse.rwt.widgets.internal.uploadkit;

import org.eclipse.rwt.internal.theme.*;
import org.eclipse.rwt.widgets.Upload;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapter;

public class UploadThemeAdapter extends ControlThemeAdapter {

  public int getButtonBorderWidth( final Upload upload ) {
    SimpleSelector selector = new SimpleSelector( new String[] { "[PUSH" } );
    QxType cssValue
      = ThemeUtil.getCssValue( "Button", "border", selector, null );
    return ( ( QxBorder )cssValue ).width;
  }
}
