package org.polymap.core.data.pipeline;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>19.10.2009: created</li>
 */
public interface ResponseHandler {
    
    public void handle( ProcessorResponse response )
            throws Exception;
    
}