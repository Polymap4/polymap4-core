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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.qi4j.operations.SetPropertyOperation;
import org.polymap.core.style.IStyle;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.styler.DispatchStyleChangeListener;
import org.polymap.styler.FeatureTypeStyleWrapper;
import org.polymap.styler.FilterWrapper;
import org.polymap.styler.MapStyleChangeListener;
import org.polymap.styler.RuleWrapper;
import org.polymap.styler.SLDStyleChangeListener;
import org.polymap.styler.StyleChangeListenerInterface;
import org.polymap.styler.StyleWrapper;
import org.polymap.styler.StylerPlugin;
import org.polymap.styler.SymbolizerWrapper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class StyleView extends ViewPart implements ISelectionProvider
		 {

	private static final Log log = LogFactory.getLog(StyleView.class);

	public Composite editor_composite;
	public Composite editor_outer_composite;
	//public Composite parent_;
	private StyleTree tree;
	
	private SashForm sf1;
	private StyleView this_ref;

	private org.eclipse.ui.ISelectionListener selectionListener;

	// private ILayer selectedLayer;


	public ILayer act_layer = null;

    /** The cached style of the {@link #act_layer}. */
	public IStyle act_style;

    private IWorkbenchPage page;

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.polymap.styler.ui.StyleView"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public StyleView() {
		this_ref=this;
	}

	public Style read_style() {
		StyleFactory factory = CommonFactoryFinder.getStyleFactory(GeoTools
				.getDefaultHints());

		SLDParser styleReader = null;
		try {
			styleReader = new SLDParser(factory,

			// new
			// URL("http://sampleserver1.arcgisonline.com/arcgis/wms/slds/point_propertyIsNotEqualTo.xml")
					// new
					// URL("http://wms.wheregroup.com/sld/germany_coloured.xml")
					// !!TODO!! check why failing new
					// URL("http://www.openlayers.org/dev/examples/tasmania/sld-tasmania.xml")

					// new
					// URL("http://geoinformatik.htw-dresden.de/masterstyles/sld/2000.xml")
					new URL(
							"http://geoinformatik.htw-dresden.de/masterstyles/sld/2008-2-3-10245.xml") //$NON-NLS-1$

			);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return styleReader.readXML()[0];
	}

	public void refresh_tree() {
		tree.create_tree(act_style,null);
	}
	
	public void refresh_tree(Object preselected) {
		tree.create_tree(act_style,preselected);
	}
	

	public void commit_changes() throws ExecutionException, UnsupportedOperationException, IOException {
	    SetPropertyOperation op = ProjectRepository.instance().newOperation( SetPropertyOperation.class );
	    op.init( ILayer.class, act_layer, ILayer.PROP_STYLE, act_layer.getStyle() );
	    OperationSupport.instance().execute( op, false, false );
		
	    // store to catalog (after connecting to layer and catalog)
	    act_layer.getStyle().store( new NullProgressMonitor() );
	    //act_layer.setStyle(act_layer.getStyle());
		
	    // IGeoResource ig = act_layer.getGeoResource().parent(null).get
		// .getGeoResource().members(null).get(0). ;
	}

	public void setLayerBySelection(ISelection sel) {
		if (sel instanceof ISelection) {

			Object elm = ((IStructuredSelection) sel).getFirstElement();
			ILayer selectedLayer = (elm != null && elm instanceof ILayer) ? (ILayer) elm
					: null;

			if ((selectedLayer != null) && (selectedLayer.getStyle() != null)) {
				log.info("selected layer: " + selectedLayer); //$NON-NLS-1$

				if (act_layer != selectedLayer) {
					try {
                        act_layer = selectedLayer;
                       // act_style = selectedLayer.getStyle().resolve( Style.class, null );
                        act_style = selectedLayer.getStyle(); //.resolve( Style.class, null );
                        if (act_style == null) {
                            throw new IOException("Dieser Layer kann nicht mit diesem Styler bearbeitet werden.");
                        }
                        else {
                        	 tree.create_tree(act_style,null);
                        }
                    }
                    catch (IOException e) {
                        PolymapWorkbench.handleError(StylerPlugin.PLUGIN_ID, StyleView.this, e.getLocalizedMessage(), e);
                    }
				}

			}
		}
		
		
	}
	public void setLayer(ILayer layer) {
		act_layer=layer;
		try {
			//act_style=layer.getStyle().resolve(Style.class, null );
			act_style=layer.getStyle(); //.resolve(Style.class, null );
			tree.create_tree(act_style,null);
		} catch (Exception e) {
			log.warn("can't resolve Style");
		}
	
	}
	public StyleChangeListenerInterface getStyleChangeListenerBySymbolizer(
			SymbolizerWrapper sw) {
		MapStyleChangeListener scl_map = new MapStyleChangeListener();
		SLDStyleChangeListener scl_sld = new SLDStyleChangeListener(sw);
		final DispatchStyleChangeListener scl_disp = new DispatchStyleChangeListener();

		scl_disp.addListener(scl_map);
		scl_disp.addListener(scl_sld);
		return scl_disp;
	}

	public void refreshEditor(TreeItem item) {
		editor_composite.dispose();
		editor_composite = new Composite(editor_outer_composite,
				SWT.NONE);

		if (item.getData() != null) {
	
		
			if (item.getData() instanceof SymbolizerWrapper) 
				new StyleEditorComposite(
					editor_composite,
					getStyleChangeListenerBySymbolizer((SymbolizerWrapper) (item.getData())),
					(SymbolizerWrapper) (item.getData()) ,item );
			else if (item.getData() instanceof RuleWrapper) 
			// if Rule Item was clicked
				new RuleEditorComposite(editor_composite,
					(RuleWrapper)item.getData() , item);
			 
			else if (item.getData() instanceof FilterWrapper) 
				// 	if Filter Item was clicked
				new FilterEditorComposite(editor_composite,
						(FilterWrapper) item.getData() , item ,this);
			else if (item.getData() instanceof StyleWrapper) 
				new StyleRootEditorComposite(editor_composite,((StyleWrapper) (item.getData())), this_ref);
			else if (item.getData() instanceof FeatureTypeStyleWrapper) 
				new FeatureTypeStyleEditorComposite(editor_composite,((FeatureTypeStyleWrapper) (item.getData())),item);
			
			
		}
		
		sf1.layout(true);
		editor_composite.layout(true);
		editor_outer_composite.layout(true);
		
	}
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		page = window.getActivePage();
		// enable

		// register selection listener

		if (selectionListener == null) {
			selectionListener = new org.eclipse.ui.ISelectionListener() {
				public void selectionChanged(IWorkbenchPart part, ISelection sel) {

					setLayerBySelection(sel);
					// }
				}
			};
			page.addSelectionListener(selectionListener);
		}

		/*
		 * // disable else { if (selectionListener != null) {
		 * page.removeSelectionListener( selectionListener ); selectionListener
		 * = null; } }
		 */

		//this.parent_ = parent;

		sf1 = new SashForm(parent, SWT.HORIZONTAL);
		tree = new StyleTree(sf1,this);


		
		editor_outer_composite = new Composite(sf1, SWT.BORDER);
		FillLayout fill_layout = new FillLayout();
		editor_outer_composite.setLayout(fill_layout);

		// gridLayout.makeColumnsEqualWidth = true;

		// sf1.setLayout(gridLayout);

		// tree.dispose();

		
		tree.getTree().addSelectionListener(new SelectionAdapter() {
			

			public void widgetSelected(SelectionEvent e) {
				// int[] weights=sf1.getWeights();

				refreshEditor((TreeItem) e.item);

				// sf1.getWeights()

				// sf1.setWeights(weights);
			}
		});

		// create_tree(read_style());

		// sf1.setWeights(new int[] { 38,62 } );
		sf1.setWeights(new int[] { 500, 500 });
		// sf1.layout();

		// parent.pack();
	}
		
    public void dispose() {
        if (selectionListener != null) {
            page.removeSelectionListener( selectionListener );
            selectionListener = null;
        }
    }

    /**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		log.info("StyleView setFocus called "); //$NON-NLS-1$
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
	}

	@Override
	public void setSelection(ISelection selection) {
	}


}