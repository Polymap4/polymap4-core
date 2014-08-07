/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.ui.upload;

import java.io.InputStream;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IUploadHandler {

    /**
     * Called when the upload has been initated for this handler. The upload has to
     * have finished and the given InputStream has to have closed when this method
     * returns.
     * <p/>
     * Note: This method is called from outside the UI thread.
     * 
     * @param name
     * @param contentType
     * @param contentLength The content length of the request. This is approx(!) the
     *        size of the bytes available for reading. -1 signals that the content
     *        length could not be determined.
     * @param in
     * @throws Exception
     */
    public void uploadStarted( String name, String contentType, int contentLength, InputStream in ) throws Exception;
    
}
