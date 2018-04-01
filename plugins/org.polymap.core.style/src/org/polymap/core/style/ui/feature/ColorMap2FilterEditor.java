/* 
 * polymap.org
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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
package org.polymap.core.style.ui.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import java.awt.Color;
import java.io.IOException;

import org.geotools.util.DefaultProgressListener;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.util.ProgressListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.RWT;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.Messages;
import org.polymap.core.style.StylePlugin;
import org.polymap.core.style.model.feature.FilterMappedColors;
import org.polymap.core.style.model.feature.MappedValues.Mapped;
import org.polymap.core.style.ui.ColorChooser;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.style.ui.UIService;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.StatusDispatcher.Style;
import org.polymap.core.ui.UIUtils;

/**
 * Editor that creates colors based on feature attributes.
 *
 * @author Steffen Stundzig
 * @author Falko Bräutigam
 */
public class ColorMap2FilterEditor
        extends StylePropertyEditor<FilterMappedColors> {

    private static final Log log = LogFactory.getLog( ColorMap2FilterEditor.class );

    private static final IMessages  i18n = Messages.forPrefix( "ColorMap2FilterEditor", "AbstractMap2FilterEditor" );

    private static final int        MAX_VALUES = 100;

    private String              propertyName;

    /** The model of the editor and it dialog. It reflects values of the style property. */
    private List<Triple>        colorMap = new ArrayList();

    private org.eclipse.swt.graphics.Color defaultFg;


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Color.class.isAssignableFrom( targetType( site ) ) 
                && site.featureStore.isPresent()
                && site.featureType.isPresent() ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( FilterMappedColors.defaults() );
    }


    protected void initValues() {
        List<Mapped<Filter,Color>> values = prop.get().values();
        for (Mapped<Filter,Color> entry : values) {
            PropertyIsEqualTo filter = (PropertyIsEqualTo)entry.key();
            propertyName = ((PropertyName)filter.getExpression1()).getPropertyName();
            Object value = ((Literal)filter.getExpression2()).getValue();
            colorMap.add( new Triple( value, -1, entry.value() ) );
        }
    }


    /**
     * Creates a new #colorMap with default color for values of the current #propertyName.
     */
    protected void createRandomColorMap() throws IOException {
        colorMap.clear();
    
        // count occurences; maps property value into count
        Map<Object,AtomicInteger> valueOccurences = new HashMap( MAX_VALUES*2 );
        ProgressListener progress = new DefaultProgressListener();
        site().featureStore.get().getFeatures().accepts( feature -> {
            // get value
            Object value = feature.getProperty( propertyName ).getValue();
            valueOccurences.computeIfAbsent( value, v -> new AtomicInteger() ).incrementAndGet();   
            // check MAX_VALUES
            if (valueOccurences.size() > MAX_VALUES) {
                progress.setCanceled( true );
            }
        }, progress );
        log.info( "Color map entries: " + valueOccurences.size() );
    
        if (progress.isCanceled()) {
            StatusDispatcher.handle( new Status( IStatus.INFO, CorePlugin.PLUGIN_ID, 
                    i18n.get( "tooManyEntries", MAX_VALUES ), null ), Style.SHOW, Style.LOG );
        }
        else {
            valueOccurences.entrySet().stream()
                    .sorted( (e1,e2) -> e2.getValue().get() - e1.getValue().get() )
                    .forEach( entry -> colorMap.add( new Triple( entry.getKey(), entry.getValue().get(), DefaultStyle.randomColor() ) ) );
        }
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Button button = new Button( parent, SWT.FLAT|SWT.LEFT );
        defaultFg = button.getForeground();

        initValues();

        button.addSelectionListener( UIUtils.selectionListener( ev -> {
            Dialog dialog = new Dialog();
            UIService.instance().openDialog( dialog.title(), dialogParent -> {
                dialog.createContents( dialogParent );
            }, () -> {
                submit();
                updateButtonColor( button );
                return true;
            } );
        }));
        updateButtonColor( button );
        return contents;
    }


    /**
     * Submits changes of {@link #colorMap} to {@link FilterMappedColors}.
     */
    protected void submit() {
        prop.get().clear();

        for (Triple entry : colorMap) {
            prop.get().add( 
                    ff.equals( ff.property( propertyName ), ff.literal( entry.value.get() ) ), 
                    entry.color.get() );
        }
    }
    

    protected void updateButtonColor( Button button ) {
        if (!StringUtils.isBlank( propertyName ) && !colorMap.isEmpty()) {
            button.setText( i18n.get( "rechoose", propertyName, colorMap.size() ) );
            button.setBackground( StylePlugin.okColor() );
            button.setForeground( defaultFg );
        }
        else {
            button.setText( i18n.get( "choose" ) );
            button.setBackground( StylePlugin.okColor() );
            button.setForeground( StylePlugin.errorColor() );
        }
    }


    @Override
    public boolean isValid() {
        return !StringUtils.isBlank( propertyName );
    }

    
    /**
     * Color chooser which loads all values for a selected property and calculates/maps
     * a color to them.
     */
    protected class Dialog {

        private Composite           tableViewer;

        private ScrolledComposite   scrolledComposite;

        private ComboViewer         propCombo;


        public String title() {
            return i18n.get( "dialogTitle" );
        }


        public void createContents( Composite parent ) {
            parent.setLayout( FormLayoutFactory.defaults().spacing( 8 ).create() );
        
            // tableViewer
            scrolledComposite = new ScrolledComposite( parent, SWT.V_SCROLL | SWT.H_SCROLL );
            scrolledComposite.setExpandHorizontal( true );
            scrolledComposite.setExpandVertical( true );

            tableViewer = new Composite( scrolledComposite, SWT.NONE );
            tableViewer.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
            scrolledComposite.setContent( tableViewer );
            scrolledComposite.setMinSize( tableViewer.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );

            // property chooser
            propCombo = new ComboViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY | SWT.DROP_DOWN );
            propCombo.getCombo().setVisibleItemCount( 12 );
            propCombo.setLabelProvider( new LabelProvider() {
                @Override public String getText( Object elm ) {
                    return ((PropertyDescriptor)elm).getName().getLocalPart();
                }
            });
            propCombo.addFilter( new ViewerFilter() {
                @Override public boolean select( Viewer viewer, Object parentElm, Object elm ) {
                    return !(elm instanceof GeometryDescriptor );
                }
            });
            propCombo.addSelectionChangedListener( ev -> {
                PropertyDescriptor sel = UIUtils.selection( propCombo.getSelection() ).first( PropertyDescriptor.class ).get();
                if (!sel.getName().getLocalPart().equals( propertyName )) {
                    propertyName = sel.getName().getLocalPart();
                    try {
                        createRandomColorMap();
                        refreshTable();
                    }
                    catch (IOException e) {
                        StatusDispatcher.handleError( "Unable to get initial color map from property values.", e );
                    }
                }
            });
            propCombo.setContentProvider( ArrayContentProvider.getInstance() );
            propCombo.setInput( site().featureType.get().getDescriptors() );

            if (propertyName != null) {
                PropertyDescriptor propDescriptor = site().featureType.get().getDescriptor( propertyName );
                propCombo.setSelection( new StructuredSelection( propDescriptor ) );
            }

            // layout
            FormDataFactory.on( propCombo.getControl() ).top( 0 ).left( 0 ).right( 100 );
            FormDataFactory.on( scrolledComposite ).fill().top( propCombo.getControl() ).height( 250 ).width( 380 );

            refreshTable();
        }


        protected void refreshTable() {
            UIUtils.disposeChildren( tableViewer );

            tableViewer.setLayout( ColumnLayoutFactory.defaults().columns( 1, 3 ).spacing( 1 ).margins( 2 ).create() );
            int count = 0;
            for (Triple triple : colorMap) {
                Composite row = new Composite( tableViewer, SWT.NONE );
                row.setLayout( FormLayoutFactory.defaults().spacing( 8 ).margins( 2 ).create() );
                if (count++ % 2 == 0) {
                    row.setBackground( UIUtils.getColor( 245, 245, 245 ) );
                }

                Button colorBtn = new Button( row, SWT.PUSH );
                colorBtn.setBackground( UIUtils.getColor( triple.color.get() ) );
                colorBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
                    ColorChooser cc = new ColorChooser( UIUtils.getRGB( triple.color.get() ) );
                    UIService.instance().openDialog( cc.title(), dialogParent -> {
                        cc.createContents( dialogParent );
                    }, () -> {
                        RGB rgb = cc.getRGB();
                        triple.color.set( new Color( rgb.red, rgb.green, rgb.blue ) );
                        colorBtn.setBackground( UIUtils.getColor( rgb ) );
                        colorBtn.setForeground( rgb.red * rgb.blue * rgb.green > 8000000
                                ? UIUtils.getColor( 30, 30, 30 )
                                : UIUtils.getColor( 225, 225, 225 ) );
                        return true;
                    } );
                }));

                Label text = new Label( row, SWT.NONE ) {
                    @Override public Point computeSize( int wHint, int hHint, boolean changed ) {
                        //return super.computeSize( wHint, hHint, changed );
                        return new Point( getText().length()*7, 16 );
                    }
                };
                String label = StringUtils.abbreviate( triple.value.orElse( "[NULL]" ).toString(), 40 );
                text.setText( label + "  (" + triple.count.get() + ")" );

                FormDataFactory.on( colorBtn ).left( 0 ).height( 18 ).width( 18 );
                FormDataFactory.on( text ).left( colorBtn )/*.width( 200 )*/.right( 100 );
            }
            tableViewer.layout( true );
            scrolledComposite.setMinSize( tableViewer.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        }
    }


    /**
     * 
     */
    public static class Triple
            extends Configurable {

        public Config<Object>   value;

        @Mandatory
        public Config<Integer>  count;

        @Mandatory
        public Config<Color>    color;


        public Triple( Object value, int count, Color color ) {
            this.value.set( value );
            this.count.set( count );
            this.color.set( color );
        }
    }

}
