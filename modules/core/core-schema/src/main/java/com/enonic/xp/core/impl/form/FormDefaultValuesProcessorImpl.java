package com.enonic.xp.core.impl.form;

import java.util.stream.StreamSupport;

import org.osgi.service.component.annotations.Component;
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
import com.enonic.xp.form.FormOptionSetOption;
import com.enonic.xp.form.Input;
import com.enonic.xp.form.Occurrences;
import com.enonic.xp.inputtype.InputTypes;

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

    private static final Occurrences DEFAULT_OCCURRENCES = Occurrences.create( 1, 1 );

    @Override
    public void setDefaultValues( final Form form, final PropertyTree data )
    {
        processFormItems( form.getFormItems(), data, PropertyPath.from( "" ), DEFAULT_OCCURRENCES );
    }

    private void processFormItems( final Iterable<FormItem> formItems, final PropertyTree data, final PropertyPath parentPath,
                                   final Occurrences parentOccurrences )
    {
        StreamSupport.stream( formItems.spliterator(), false ).forEach( formItem -> {
            if ( formItem.getType() == INPUT )
            {
                Input input = formItem.toInput();
                if ( input.getDefaultValue() != null )
                {
                    try
                    {
                        final Value defaultValue = InputTypes.BUILTIN.resolve( input.getInputType() ).
                            createDefaultValue( input );

                        final PropertyPath propertyPath = PropertyPath.from( parentPath, input.getName() );

                        if ( defaultValue != null && data.getProperty( propertyPath ) == null )
                        {
                            int minOccurs = input.getOccurrences().getMinimum();
                            if ( parentOccurrences.getMinimum() > minOccurs )
                            {
                                minOccurs = parentOccurrences.getMinimum();
                            }

                            if ( minOccurs > 1 )
                            {
                                for ( int i = 0; i < minOccurs; i++ )
                                {
                                    if ( parentOccurrences.getMinimum() > 1 )
                                    {
                                        final Property property = data.
                                            getRoot().addProperty( parentPath.toString(),
                                                                   ValueFactory.newPropertySet( new PropertySet() ) );
                                        if ( property != null )
                                        {
                                            property.getSet().setProperty( input.getName(), defaultValue );
                                        }
                                    }
                                    else
                                    {
                                        data.setProperty( PropertyPath.from( parentPath, input.getName() ).toString(), i, defaultValue );
                                    }
                                }
                            }
                            else
                            {
                                data.setProperty( PropertyPath.from( parentPath, input.getName() ), defaultValue );
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
                processFormItems( formItem.toFormItemSet().getFormItems(), data, PropertyPath.from( parentPath, formItem.getName() ),
                                  formItem.toFormItemSet().getOccurrences() );
            }
            else if ( formItem.getType() == LAYOUT && formItem.toLayout() instanceof FieldSet )
            {
                processFormItems( (FieldSet) formItem.toLayout(), data, parentPath, DEFAULT_OCCURRENCES );
            }
            else if ( formItem.getType() == FORM_OPTION_SET_OPTION )
            {
                FormOptionSetOption option = formItem.toFormOptionSetOption();
                if ( option.isDefaultOption() )
                {
                    processFormItems( option.getFormItems(), data, PropertyPath.from( parentPath, formItem.getName() ), DEFAULT_OCCURRENCES );
                }
            }
            else if ( formItem.getType() == FORM_OPTION_SET )
            {
                processFormItems( formItem.toFormOptionSet().getFormItems(), data, PropertyPath.from( parentPath, formItem.getName() ),
                                  formItem.toFormOptionSet().getOccurrences() );
            }
        } );
    }
}
