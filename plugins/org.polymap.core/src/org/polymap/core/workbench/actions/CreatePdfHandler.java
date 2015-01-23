package org.polymap.core.workbench.actions;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * A command handler that creates PDF from the current {@link IMap}. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class CreatePdfHandler 
        extends AbstractHandler {
	
    private static Log log = LogFactory.getLog( CreatePdfHandler.class );
    
    public Object execute( ExecutionEvent ev) 
            throws ExecutionException {
        return null;
    }

//    /**
//	 * The constructor.
//	 */
//	public CreatePdfHandler() {
//	    // funktioniert nicht wie gedacht...
//	    IGeoSelectionManager sm = GeoSelectionService.getDefault().getPlatformSelectionManager();
//	    sm.addListener( new IGeoSelectionChangedListener() {
//            public void geoSelectionChanged( GeoSelectionChangedEvent ev ) {
//                log.debug( "event: " + ev.getContext() );
//            }
//        });
//	}
//
//	/**
//	 * the command has been executed, so extract extract the needed information
//	 * from the application context.
//	 */
//	public Object execute( ExecutionEvent ev) 
//	        throws ExecutionException {
//        try {
//            // render map
//            int scaleDenom = 30000;
//            BoundsStrategy boundsStrategy = new BoundsStrategy( scaleDenom );
//            BufferedImage image = new BufferedImage( 500, 500, BufferedImage.TYPE_4BYTE_ABGR );
//            Graphics g = image.getGraphics();
//            IMap map = ApplicationGIS.getActiveMap();
//
//            DrawMapParameter drawMapParameter = new DrawMapParameter( 
//                    (Graphics2D)g, 
//                    new java.awt.Dimension( 500, 500 ), 
//                    map, 
//                    boundsStrategy, 
//                    192, 
//                    SelectionStyle.OVERLAY, 
//                    new NullProgressMonitor() );
//            IMap renderedMap = ApplicationGIS.drawMap( drawMapParameter );
//            g.dispose();
//
//            File f = new File( "/tmp", "polymap3.export.pdf" );
//            Image2Pdf.write( image, f.getAbsolutePath(), Paper.A4,
//                    0, 0, false, 72 );
//
//            //
//            IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked( ev );
//            MessageDialog.openInformation(
//            		window.getShell(),
//            		"polymap3.core Plug-in",
//            		"File size: " + f.length() );
//
//        }
//        catch (RenderException e) {
//            e.printStackTrace();
//        }
//		return null;
//	}
}
