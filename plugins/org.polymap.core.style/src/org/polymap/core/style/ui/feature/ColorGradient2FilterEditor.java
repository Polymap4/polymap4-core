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

import java.util.List;

import java.awt.Color;
import java.io.IOException;
import java.text.NumberFormat;

import org.geotools.util.NullProgressListener;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.StylePlugin;
import org.polymap.core.style.model.feature.FilterMappedColors;
import org.polymap.core.style.model.feature.MappedValues.Mapped;
import org.polymap.core.style.ui.ColorChooser;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.style.ui.UIService;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

/**
 * Editor that creates a (linear) gradient of {@link Color}s between adjustable
 * bounds with an adjustable number of breakpoints. The numbers a mapped to
 * an adjustable range of property values.
 *
 * @author Falko Bräutigam
 */
public class ColorGradient2FilterEditor
        extends StylePropertyEditor<FilterMappedColors> {

    private static final Log log = LogFactory.getLog( ColorGradient2FilterEditor.class );
    
    private static final IMessages i18n = Messages.forPrefix( "ColorGradient2FilterEditor", "AbstractGradient2FilterEditor" );

    public static final double      UNINITIALIZED = Double.NaN;

    /** The name of the Feature property to use to calculate {@link #lowerBound}/{@link #upperBound}.*/
    private String              propertyName;
    
    /** Feature attribut value range lower bound. */
    private double              lowerBound;

    /** Feature attribut value range upper bound. */
    private double              upperBound;

    /** Generated number gradient minimum value. */
    private Color               minimumValue;

    /** Generated number gradient maximum value. */
    private Color               maximumValue;

    private int                 breakpoints;

    private org.eclipse.swt.graphics.Color defaultFg;

    private NumberFormat        df;

    /** The button to open the dialog. */
    private Button              btn;


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
        List<Mapped<Filter,java.awt.Color>> values = prop.get().values();

        df = NumberFormat.getInstance( Polymap.getSessionLocale() );
        df.setMaximumFractionDigits( 2 );
        df.setMinimumFractionDigits( 0 );
        
        // defaults
        if (values.isEmpty()) {
            minimumValue = Color.LIGHT_GRAY;
            maximumValue = Color.BLACK;
            breakpoints = 10;
        }
        // from prop
        else {
            And first = (And)values.get( 0 ).key();
            BinaryComparisonOperator lessThan = (BinaryComparisonOperator)first.getChildren().get( 0 );
            propertyName = ((PropertyName)lessThan.getExpression1()).getPropertyName();
            String value = (String)((Literal)lessThan.getExpression2()).getValue();
            lowerBound = Double.parseDouble( value ); 

            And last = (And)values.get( values.size()-1 ).key();
            BinaryComparisonOperator greaterThan = (BinaryComparisonOperator)last.getChildren().get( 0 );
            value = (String)((Literal)greaterThan.getExpression2()).getValue();
            upperBound = Double.parseDouble( value ); 

//            minimumValue = values.get( 0 ).value().doubleValue();
//            maximumValue = values.get( values.size() - 1 ).value().doubleValue();
            breakpoints = values.size() + 1;
        }
    }


    @Override
    public Composite createContents( Composite parent ) {
        initValues();

        Composite contents = super.createContents( parent );
        btn = new Button( parent, SWT.FLAT|SWT.PUSH|SWT.LEFT );
        defaultFg = btn.getForeground();
        btn.addSelectionListener( UIUtils.selectionListener( ev -> {
            Dialog dialog = new Dialog();
            UIService.instance().openDialog( dialog.title(), dialogParent -> {
                dialog.createContents( dialogParent );
            }, () -> {
                submit();
                updateButton();
                return true;
            });
        }));
        updateButton();
        return contents;
    }

    
    protected void submit() {
        prop.get().clear();

//        Color lastValue = minimumValue;
//        
//        // only linear currently
//        int intervals = breakpoints - 1;
//        for (int i = 0; i < steps; i++) {
//            float ratio = (float)i / (float)steps;
//            
//            float red = minimumValue.getRed() + (maximumValue.getRed() - minimumValue.getRed()) * ratio;
//            
//            int red = (int) (maximumValue.getRed() * ratio + minimumValue.getRed() * (1 - ratio));
//            int green = (int) (maximumValue.getGreen() * ratio + minimumValue.getGreen() * (1 - ratio));
//            int blue = (int) (maximumValue.getBlue() * ratio + minimumValue.getBlue() * (1 - ratio));
//            Color stepColor = new Color(red, green, blue);
//            prop.get().add( )
//        }
//        
//        
//        // only linear currently
//        int intervals = breakpoints - 1;
//        double valueStep = (maximumValue - minimumValue) / intervals;
//        double scaleStep = (upperBound - lowerBound) / intervals;
//        double currentValue = minimumValue;
//        double currentLower = lowerBound;
//
//        for (int i = 0; i < intervals; i++) {
//            Double round = Numbers.roundToDigits( currentValue, annotation.digits() );
//            prop.get().add( ff.and( 
//                    ff.greaterOrEqual( ff.property( propertyName ), ff.literal( currentLower ) ),
//                    ff.less( ff.property( propertyName ), ff.literal( currentLower+scaleStep ) ) ),
//                    Numbers.cast( round, site().targetType() ) );
//            currentValue += valueStep;
//            currentLower += scaleStep;
//        }
//        
    }

    
    protected void updateButton() {
        if (propertyName != null && minimumValue != null && maximumValue != null) {
            btn.setText( i18n.get( "rechoose", propertyName ) );
            btn.setForeground( defaultFg );
            btn.setBackground( StylePlugin.okColor() );
        }
        else {
            btn.setText( i18n.get( "choose" ) );
            btn.setForeground( StylePlugin.errorColor() );
            btn.setBackground( StylePlugin.okColor() );
        }
    }
    
    
    @Override
    public boolean isValid() {
        return lowerBound != UNINITIALIZED && upperBound != UNINITIALIZED 
                && minimumValue != null && maximumValue != null 
                && breakpoints > 0;
    }
    
    
    /**
     * The input dialog of {@link ColorGradient2FilterEditor}.
     */
    public class Dialog {

        private ComboViewer propCombo;
        
        private Spinner     stepsSpinner;

        private Button      mappedMaximumBtn;

        private Button      mappedMinimumBtn;

        private Spinner     upperBoundSpinner;

        private Spinner     lowerBoundSpinner;

        private Label       scaleLine;

        public String title() {
            return i18n.get( "dialogTitle" );
        }


        protected void updateBoundsFromProperty( PropertyDescriptor propDescriptor ) {
            try {
                upperBound = Double.MIN_VALUE;
                lowerBound = Double.MAX_VALUE;
                site().featureStore.get().getFeatures().accepts( feature -> {
                    Number value = (Number)feature.getProperty( propDescriptor.getName() ).getValue();
                    upperBound = Math.max( upperBound, value.doubleValue() );
                    lowerBound = Math.min( lowerBound, value.doubleValue() );
                }, new NullProgressListener() );
            }
            catch (IOException e) {
                StatusDispatcher.handleError( "Unable to read property values.", e );
            }
        }
        
        
        public void createContents( Composite parent ) {
            //double factorX = Math.pow( 10, digits );

            lowerBoundSpinner = new Spinner( parent, SWT.BORDER );
            upperBoundSpinner = new Spinner( parent, SWT.BORDER );
            scaleLine = new Label( parent, SWT.LEFT );
            
            propCombo = new ComboViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY | SWT.DROP_DOWN );
            propCombo.setLabelProvider( new LabelProvider() {
                @Override public String getText( Object elm ) {
                    return ((PropertyDescriptor)elm).getName().getLocalPart();
                }
            });
            propCombo.addFilter( new ViewerFilter() {
                @Override public boolean select( Viewer viewer, Object parentElm, Object elm ) {
                    return Number.class.isAssignableFrom( ((PropertyDescriptor)elm).getType().getBinding() );
                }
            });
            propCombo.addSelectionChangedListener( ev -> {
                PropertyDescriptor sel = UIUtils.selection( propCombo.getSelection() ).first( PropertyDescriptor.class ).get();
                updateBoundsFromProperty( sel );
                propertyName = sel.getName().getLocalPart();
                // XXX digit/increment for given value range
                lowerBoundSpinner.setMinimum( (int)lowerBound );
                lowerBoundSpinner.setMaximum( (int)upperBound );
                lowerBoundSpinner.setSelection( (int)lowerBound );
                upperBoundSpinner.setMinimum( (int)lowerBound );
                upperBoundSpinner.setMaximum( (int)upperBound );
                upperBoundSpinner.setSelection( (int)upperBound );
                scaleLine.setText( i18n.get( "currentValues", df.format( lowerBound ), df.format( upperBound ) ) );
            });
            propCombo.setContentProvider( ArrayContentProvider.getInstance() );
            propCombo.setInput( site().featureType.get().getDescriptors() );
            if (propertyName != null) {
                PropertyDescriptor propDescriptor = site().featureType.get().getDescriptor( propertyName );
                if (propDescriptor != null) {
                    propertyName = propDescriptor.getName().getLocalPart();
                    propCombo.setSelection( new StructuredSelection( propDescriptor ) );
                }
            }
            
            lowerBoundSpinner.setToolTipText( "First interval covers everything up to this lower bounds\nand maps to the given value" );
            lowerBoundSpinner.setSelection( (int)lowerBound );
            lowerBoundSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = lowerBoundSpinner.getSelection();
                lowerBound = selection;
                upperBoundSpinner.setMinimum( selection );
            }));

            upperBoundSpinner.setToolTipText( "Last interval covers everything bigger than this upper bounds\nand and maps to the given value" );
            upperBoundSpinner.setSelection( (int)upperBound );
            upperBoundSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = upperBoundSpinner.getSelection();
                upperBound = selection;
                lowerBoundSpinner.setMaximum( selection );
            }));
            upperBoundSpinner.setMinimum( (int)lowerBound );
            lowerBoundSpinner.setMaximum( (int)upperBound );
            
            mappedMinimumBtn = new Button( parent, SWT.PUSH );
            mappedMinimumBtn.setBackground( UIUtils.getColor( minimumValue ) );
            mappedMinimumBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
                ColorChooser cc = new ColorChooser( UIUtils.getRGB( minimumValue ) );
                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    RGB rgb = cc.getRGB();
                    minimumValue = new Color( rgb.red, rgb.green, rgb.blue );
                    mappedMinimumBtn.setBackground( UIUtils.getColor( rgb ) );
                    return true;
                } );
            }));

            mappedMaximumBtn = new Button( parent, SWT.PUSH );
            mappedMaximumBtn.setBackground( UIUtils.getColor( maximumValue ) );
            mappedMaximumBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
                ColorChooser cc = new ColorChooser( UIUtils.getRGB( maximumValue ) );
                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    RGB rgb = cc.getRGB();
                    maximumValue = new Color( rgb.red, rgb.green, rgb.blue );
                    mappedMaximumBtn.setBackground( UIUtils.getColor( rgb ) );
                    return true;
                } );
            }));

            stepsSpinner = new Spinner( parent, SWT.BORDER );
            stepsSpinner.setToolTipText( "For X breakpoints X+1 intervals are generated" );
            stepsSpinner.setDigits( 0 );
            stepsSpinner.setMinimum( 2 );
            stepsSpinner.setMaximum( 100 );
            stepsSpinner.setSelection( breakpoints );
            stepsSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                breakpoints = stepsSpinner.getSelection();
            }));

            // labels / layout ****************************
            int labelWidth = 100;
            int spinnerWidth = 120;
            parent.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );

            Label l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "selectProperty" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( 3 ).left( 0 );
            FormDataFactory.on( propCombo.getControl() ).top( 0 ).left( l, -10 ).right( 100, -10 );
            
            scaleLine.setFont( UIUtils.italic( scaleLine.getFont() ) );
            if (propertyName != null) {
                scaleLine.setText( i18n.get( "currentValues", lowerBound, upperBound ) );
            }
            FormDataFactory.on( scaleLine ).left( l, -10 ).top( l, -10 ).right( 100, -10 ).noBottom();
            
            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "lowerBound" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( scaleLine, 3 ).left( 0 );
            FormDataFactory.on( lowerBoundSpinner ).width( spinnerWidth ).top( scaleLine ).left( l, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "mapsTo" ) );
            FormDataFactory.on( l ).top( scaleLine, 3 ).left( lowerBoundSpinner );
            FormDataFactory.on( mappedMinimumBtn ).width( spinnerWidth/2 ).top( scaleLine, -2 ).left( l, -10 ).right( 100, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "upperBound" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( lowerBoundSpinner, 3 ).left( 0 );
            FormDataFactory.on( upperBoundSpinner ).width( spinnerWidth ).top( lowerBoundSpinner, 0 ).left( l, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "mapsTo" ) );
            FormDataFactory.on( l ).top( lowerBoundSpinner, 3 ).left( upperBoundSpinner );
            FormDataFactory.on( mappedMaximumBtn ).width( spinnerWidth/2 ).top( lowerBoundSpinner, -2 ).left( l, -10 ).right( 100, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "steps" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( upperBoundSpinner, 3 ).left( 0 );
            FormDataFactory.on( stepsSpinner ).width( spinnerWidth ).top( upperBoundSpinner, 0 ).left( l, -10 );
        }

    }

}
