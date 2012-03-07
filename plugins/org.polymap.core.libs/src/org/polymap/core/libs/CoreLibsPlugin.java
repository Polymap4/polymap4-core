/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.libs;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.net.URL;

import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.factory.Hints.Key;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.osgi.framework.BundleContext;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class CoreLibsPlugin 
        extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.core.libs";

	// The shared instance
	private static CoreLibsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public CoreLibsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
    public void start( BundleContext context )
            throws Exception {
        super.start( context );
        plugin = this;
        // WARNING - the above hints are recommended to us by GeoServer
        // but they cause epsg-wkt not to work because the
        // various wrapper classes trip up over a CRSAuthorityFactory
        // that is not also a OperationAuthorityFactory (I think)
        // prime the pump - ensure EPSG factory is found //$NON-NLS-1$
        CoordinateReferenceSystem wgs84 = CRS.decode( "EPSG:4326" );
        if (wgs84 == null) {
            String msg = "Unable to locate EPSG authority for EPSG:4326; consider removing temporary geotools/epsg directory and trying again."; //$NON-NLS-1$
            System.out.println( msg );
            // throw new FactoryException(msg);
        }
        Map<Key, Boolean> map = new HashMap<Key, Boolean>();
        // these commented out hints are covered by the forceXY system property
        //
        // map.put( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true );
        // map.put( Hints.FORCE_STANDARD_AXIS_DIRECTIONS, true );
        // map.put( Hints.FORCE_STANDARD_AXIS_UNITS, true );
        map.put( Hints.LENIENT_DATUM_SHIFT, true );
        Hints global = new Hints( map );
        GeoTools.init( global );

        URL epsg = null;
        Location configLocaiton = Platform.getInstallLocation();
        Location dataLocation = Platform.getInstanceLocation();
        if (dataLocation != null) {
            try {
                URL url = dataLocation.getURL();
                URL proposed = new URL( url, "epsg.properties" );
                if ("file".equals( proposed.getProtocol() )) {
                    File file = new File( proposed.toURI() );
                    if (file.exists()) {
                        epsg = file.toURI().toURL();
                    }
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (epsg == null && configLocaiton != null) {
            try {
                URL url = configLocaiton.getURL();
                URL proposed = new URL( url, "epsg.properties" );
                if ("file".equals( proposed.getProtocol() )) {
                    File file = new File( proposed.toURI() );
                    if (file.exists()) {
                        epsg = file.toURI().toURL();
                    }
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (epsg == null) {
            try {
                URL internal = context.getBundle().getEntry( "epsg-gt2.7.properties" );
                URL fileUrl = FileLocator.toFileURL( internal );
                epsg = fileUrl.toURI().toURL();
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (epsg != null) {
            Hints hints = new Hints( Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class );
            ReferencingFactoryContainer referencingFactoryContainer = ReferencingFactoryContainer
                    .instance( hints );

            PropertyAuthorityFactory factory = new PropertyAuthorityFactory(
                    referencingFactoryContainer, Citations.fromName( "EPSG" ), epsg );

            ReferencingFactoryFinder.addAuthorityFactory( factory );
        }
        ReferencingFactoryFinder.scanForPlugins();
//        if (false) { // how to do debug check with OSGi bundles?
//            CRS.main( new String[] { "-dependencies" } ); 
//        }

        verifyReferencingEpsg();
        verifyReferencingOperation();   
    }


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CoreLibsPlugin getDefault() {
		return plugin;
	}

	/**
     * If this method fails it's because, the epsg jar is either 
     * not available, or not set up to handle math transforms
     * in the manner udig expects.
     * 
     * @throws Exception if we cannot even get that far
     */
    private void verifyReferencingEpsg() throws Exception {
        CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326"); // latlong //$NON-NLS-1$
        CoordinateReferenceSystem BC_ALBERS = CRS.decode("EPSG:3005"); //$NON-NLS-1$
        
        MathTransform transform = CRS.findMathTransform(BC_ALBERS, WGS84 );
        DirectPosition here  = new DirectPosition2D( BC_ALBERS, 1187128, 395268 );
        DirectPosition there = new DirectPosition2D( WGS84, -123.47009173007372,48.54326498732153 );
            
        DirectPosition check = transform.transform( here, new GeneralDirectPosition(WGS84) );
        //DirectPosition doubleCheck = transform.inverse().transform( check, new GeneralDirectPosition(BC_ALBERS) );        
//        if( !check.equals(there)){
//          String msg = "Referencing failed to produce expected transformation; check that axis order settings are correct.";
//          System.out.println( msg );
//          //throw new FactoryException(msg);
//        }
        double delta = Math.abs(check.getOrdinate(0) - there.getOrdinate(0))+Math.abs(check.getOrdinate(1) - there.getOrdinate(1));
        if( delta > 0.0001){
            String msg = "Referencing failed to transformation with expected accuracy: Off by "+delta + "\n"+check+"\n"+there;   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            System.out.println( msg );
            //throw new FactoryException(msg);
        }   
    }

    /**
     * If this method fails it's because, the epsg jar is either 
     * not available, or not set up to handle math transforms
     * in the manner udig expects.
     * 
     * @throws Exception if we cannot even get that far
     */
    private void verifyReferencingOperation() throws Exception {
           // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
        // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
        CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326"); //$NON-NLS-1$
        ReferencedEnvelope pixelBounds = new ReferencedEnvelope( -0.24291497975705742, 0.24291497975711265, -0.5056179775280899, 0.0, EPSG4326 );
        CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
        
        ReferencedEnvelope latLong = pixelBounds.transform( WGS84, false );
        if( latLong == null){
            String msg = "Unable to transform EPSG:4326 to DefaultGeographicCRS.WGS84"; //$NON-NLS-1$
            System.out.println( msg );              
            //throw new FactoryException(msg);
        }
    }

}
