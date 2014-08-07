/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
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
 */

package org.polymap.styler.ui;

import java.io.InputStream;

import net.refractions.udig.catalog.ID;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.styling.SLDParser;
import org.geotools.styling.StyleFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.polymap.core.style.StylePlugin;
import org.polymap.core.style.geotools.GtStyle;
import org.polymap.core.ui.upload.IUploadHandler;
import org.polymap.core.ui.upload.Upload;

import org.polymap.styler.StyleWrapper;
import org.polymap.styler.helper.LayoutHelper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class StyleRootEditorComposite 
	implements Runnable
	{

	private static final Log log = LogFactory
			.getLog(StyleRootEditorComposite.class);

	private Browser browser;
	private StyleWrapper style;
	private Button dump_style_btn;
	
	/*
	private String[] predefined_slds = {
			"http://polymap.de/geoserver/rest/styles/population.sld",
		"http://github.com/iwillig/openstreetmap-sld/raw/2588e14151ef805e76f4900de53386e131abee85/default/osm_roads.sld"	
	}; //$NON-NLS-1$
	 */
	
	

	Display display;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public StyleRootEditorComposite(Composite parent, StyleWrapper _style_w,final StyleView view) {
		log.info("creating style editor layout"); //$NON-NLS-1$

		display=parent.getDisplay();
		
		style = _style_w;
		// style_change_listener=scl;
		/*
		 * FillLayout toolbar_layout = new FillLayout(); toolbar_layout.type =
		 * SWT.HORIZONTAL; parent.setLayout(toolbar_layout);
		 */

		/*
		RowLayout layout = new RowLayout();
		layout.pack = false;
		layout.wrap = true;
		layout.type = SWT.VERTICAL;
		layout.fill = true;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.spacing = 0; */
		parent.setLayout(LayoutHelper.getDefaultRowLayout());
		
		Composite export_composite = LayoutHelper.subpart(parent, "Export", 1); //$NON-NLS-1$

		dump_style_btn = new Button(export_composite, SWT.PUSH);
		
		
		dump_style_btn.setEnabled(style.getIStyle().getID()!=null);
		
		dump_style_btn.setText(Messages.get().DOWNLOAD); 
		dump_style_btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// style_change_listener.debug_out();
				ID sld_id=style.getIStyle().getID();
									
				if (sld_id!=null)
					browser.execute("var newWindow = window.open('" + StylePlugin.getDefault().getServletPathForId(style.getIStyle().getID()) +  "', '_blank');");
				

			}
		});
		
		
		Composite import_composite = LayoutHelper.subpart(parent, "Import", 1); //$NON-NLS-1$

/*		
		final Text sld_txt = new Text(parent, SWT.MULTI);

		Button sld_import_btn = new Button(parent,SWT.NONE);
		*/
		
		final Upload upload = new Upload( import_composite, SWT.NONE, /*Upload.SHOW_PROGRESS |*/ Upload.SHOW_UPLOAD_BUTTON );
//	    upload.setBrowseButtonText( "Browse" );
//	    upload.setUploadButtonText( "Import" );
	    upload.setHandler( new IUploadHandler() {
            public void uploadStarted( String name, String contentType, int contentLength, InputStream in ) throws Exception {
                try {
                	StyleFactory factory = CommonFactoryFinder.getStyleFactory(GeoTools
    						.getDefaultHints());
    				SLDParser styleReader;
    		
    					//System.out.println("importing sld from" + _sld_import_combo.getText().toString());
    					styleReader = new SLDParser(factory,in);
    					
    					((GtStyle)style.getIStyle()).setStyle(styleReader.readXML()[0]);
    					
    					
    					//style.getStyle().setStyle( styleReader.readXML()[0]);
    					System.out.println("done reading - refreshing tree");
    					view.refresh_tree();
    				
    				
                } 
                catch (Exception e1) {
                    e1.printStackTrace();
                }
             //   checkFinish();
            }
        });

		/*
		sld_import_btn.setText("Import");
		
		sld_import_btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent evt) {
				System.out.println("importing sld from 1" + evt.widget);
	
				StyleFactory factory = CommonFactoryFinder.getStyleFactory(GeoTools
						.getDefaultHints());
				SLDParser styleReader;
		
					//System.out.println("importing sld from" + _sld_import_combo.getText().toString());
					styleReader = new SLDParser(factory,
							new StringReader(sld_txt.getText()));
					
					((GtStyle)style.getIStyle()).setStyle(styleReader.readXML()[0]);
					
					
					//style.getStyle().setStyle( styleReader.readXML()[0]);
					System.out.println("done reading - refreshing tree");
					view.refresh_tree();
				
				
				
				
			}
		});
		*/
/*
		Combo sld_import_combo=new Combo(parent, SWT.BORDER);
		sld_import_combo.setItems(predefined_slds);
		
		Button button = new Button(parent, SWT.PUSH);
		button.setText(Messages.get().IMPORT);
		button.setData(sld_import_combo);
		
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent evt) {
				System.out.println("importing sld from 1" + evt.widget);
				Combo _sld_import_combo = (Combo)evt.widget.getData();
				StyleFactory factory = CommonFactoryFinder.getStyleFactory(GeoTools
						.getDefaultHints());
				SLDParser styleReader;
				try {
					System.out.println("importing sld from" + _sld_import_combo.getText().toString());
					styleReader = new SLDParser(factory,
					new URL(_sld_import_combo.getText().toString()) );
//					style.setStyle( styleReader.readXML()[0]);
					System.out.println("done reading - refreshing tree");
					view.refresh_tree();
				} catch (MalformedURLException e) {
				} catch (IOException e) {
				}
				
				
				
			}
		});
		
	*/
		
		 browser = new Browser( parent, SWT.NONE );
		// only needed to execute Javascript - so hide it from the user
		browser.setVisible(false); 
		browser.setBounds(0, 0, 0, 0);
		/*Button button = new Button(parent, SWT.PUSH);
		button.setText("push me");

		final Browser browser = new Browser(parent, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent evt) {
				browser
						.setUrl("http://docs.geoserver.org/1.7.6/user/_sources/extensions/rest/style_sld.txt");
			}
		});*/
		
		
		new Thread(this).start();
	}

	boolean need_to_monitor_download_btn=true;
	
	@Override
	public void run() {
	
		while(need_to_monitor_download_btn) {
			
			display.asyncExec(new download_btn_updater());

/*			log.info("enabled " + dump_style_btn.isEnabled());
			if (dump_style_btn.isEnabled())
				break;
	*/		
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			
		}
	}
	
	class download_btn_updater implements Runnable {

		@Override
		public void run() {
			try {
				dump_style_btn.setEnabled(style.getIStyle().getID()!=null);
				if (dump_style_btn.isEnabled())
					need_to_monitor_download_btn=false;
			}
			catch (Exception e) { // cuz disposed widget
				need_to_monitor_download_btn=false;
			}
		}
		
	}

}