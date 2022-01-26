package com.enonic.xp.core.impl.form;

import java.util.stream.StreamSupport;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.PropertyPath;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.form.FieldSet;
import com.enonic.xp.form.Form;
import com.enonic.xp.form.FormDefaultValuesProcessor;
import com.enonic.xp.form.FormItem;
import com.enonic.xp.form.FormItems;
import com.enonic.xp.form.FormOptionSetOption;
import com.enonic.xp.form.Input;
import com.enonic.xp.form.Occurrences;
import com.enonic.xp.inputtype.InputTypeResolver;

import static com.enonic.xp.form.FormItemType.FORM_ITEM_SET;
import static com.enonic.xp.form.FormItemType.FORM_OPTION_SET;
import static com.enonic.xp.form.FormItemType.FORM_OPTION_SET_OPTION;
import static com.enonic.xp.form.FormItemType.INPUT;
import static com.enonic.xp.form.FormItemType.LAYOUT;

@Component(immediate = true)
public final class FormDefaultValuesProcessorImpl
    implements FormDefaultValuesProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( FormDefaultValuesProcessorImpl.class );

    private final InputTypeResolver inputTypeResolver;

    @Activate
    public FormDefaultValuesProcessorImpl( @Reference final InputTypeResolver inputTypeResolver )
    {
        this.inputTypeResolver = inputTypeResolver;
    }

    @Override
    public void setDefaultValues( final Form form, final PropertyTree data )
    {
        processFormItems( form.getFormItems(), data.getRoot() );
    }

    private void processFormItems( final Iterable<FormItem> formItems, final PropertySet dataSet )
    {
        StreamSupport.stream( formItems.spliterator(), false ).forEach( formItem -> {
            if ( formItem.getType() == INPUT )
            {
                Input input = formItem.toInput();
                if ( input.getDefaultValue() != null )
                {
                    try
                    {
                        final Value defaultValue = inputTypeResolver.resolve( input.getInputType() ).createDefaultValue( input );

                        final PropertyPath propertyPath = PropertyPath.from( input.getName() );

                        if ( defaultValue != null && dataSet.getProperty( propertyPath ) == null )
                        {
                            if ( input.getOccurrences().getMinimum() > 0 )
                            {
                                for ( int i = 0; i < input.getOccurrences().getMinimum(); i++ )
                                {
                                    dataSet.setProperty( input.getName(), i, defaultValue );
                                }
                            }
                            else
                            {
                                dataSet.setProperty( input.getName(), defaultValue );
                            }
                        }
                    }
                    catch ( IllegalArgumentException ex )
                    {
                        LOG.warn(
                            "Invalid default value for " + input.getInputType() + " input type with name '" + input.getName() + "': '" +
                                input.getDefaultValue().getRootValue() + "'" + ( ex.getMessage() == null ? "" : " - " + ex.getMessage() ) );
                    }
                }
            }
            else if ( formItem.getType() == FORM_ITEM_SET )
            {
                processFormItems( formItem.getName(), formItem.toFormItemSet().getFormItems(), dataSet,
                                  formItem.toFormItemSet().getOccurrences().getMinimum() );
            }
            else if ( formItem.getType() == LAYOUT && formItem.toLayout() instanceof FieldSet )
            {
                processFormItems( (FieldSet) formItem.toLayout(), dataSet );
            }
            else if ( formItem.getType() == FORM_OPTION_SET_OPTION )
            {
                FormOptionSetOption option = formItem.toFormOptionSetOption();
                if ( option.isDefaultOption() )
                {
                    dataSet.setProperty( "_selected", ValueFactory.newString( formItem.getName() ) );
                    if ( dataSet.getProperty( formItem.getName() ) == null )
                    {
                        Property property = dataSet.setProperty( formItem.getName(), ValueFactory.newPropertySet( new PropertySet() ) );
                        processFormItems( option.getFormItems(), property.getSet() );
                    }
                }
            }
            else if ( formItem.getType() == FORM_OPTION_SET )
            {
                processFormItems( formItem.getName(), formItem.toFormOptionSet().getFormItems(), dataSet,
                                  formItem.toFormOptionSet().getOccurrences().getMinimum() );
            }
        } );
    }

    private void processFormItems( String formItemName, FormItems formItems, PropertySet dataSet, int minOccurrences )
    {
        if ( minOccurrences > 0 )
        {
            for ( int i = 0; i < minOccurrences; i++ )
            {
                setProperty( formItemName, formItems, i, dataSet );
            }
        }
    }

    private void setProperty( String formItemName, FormItems formItems, int index, PropertySet dataSet )
    {
        if ( dataSet.getProperty( formItemName, index ) == null )
        {
            Property property = dataSet.setProperty( formItemName, index, ValueFactory.newPropertySet( new PropertySet() ) );
            if ( property != null )
            {
                if ( existsDefaultValuesOrMinOccurrencesGreaterThanZero( formItems ) )
                {
                    processFormItems( formItems, property.getSet() );
                }
            }
        }
    }

    private boolean existsDefaultValuesOrMinOccurrencesGreaterThanZero( FormItems formItems )
    {
        return StreamSupport.stream( formItems.spliterator(), false ).anyMatch( formItem -> {
            if ( formItem.getType() == INPUT )
            {
                Input input = formItem.toInput();
                return input.getOccurrences().getMinimum() > 0 || input.getDefaultValue() != null;
            }
            else if ( formItem.getType() == FORM_ITEM_SET )
            {
                final Occurrences occurrences = formItem.toFormItemSet().getOccurrences();
                if ( occurrences.getMinimum() == 0 )
                {
                    return false;
                }
                return occurrences.getMinimum() > 0 || existsDefaultValuesOrMinOccurrencesGreaterThanZero( formItem.toFormItemSet().getFormItems() );
            }
            else if ( formItem.getType() == LAYOUT && formItem.toLayout() instanceof FieldSet )
            {
                return existsDefaultValuesOrMinOccurrencesGreaterThanZero( ( (FieldSet) formItem.toLayout() ).getFormItems() );
            }
            else if ( formItem.getType() == FORM_OPTION_SET_OPTION )
            {
                FormOptionSetOption option = formItem.toFormOptionSetOption();
                return option.isDefaultOption();
            }
            else if ( formItem.getType() == FORM_OPTION_SET )
            {
                final Occurrences occurrences = formItem.toFormOptionSet().getOccurrences();
                if ( occurrences.getMinimum() == 0 )
                {
                    return false;
                }
                return occurrences.getMinimum() > 0 || existsDefaultValuesOrMinOccurrencesGreaterThanZero( formItem.toFormOptionSet().getFormItems() );
            }
            return false;
        } );
    }
}
