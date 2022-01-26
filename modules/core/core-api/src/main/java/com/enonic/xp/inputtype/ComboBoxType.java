package com.enonic.xp.inputtype;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueTypes;
import com.enonic.xp.form.Input;

import static com.google.common.base.Strings.isNullOrEmpty;

final class ComboBoxType
    extends InputTypeBase
{
    public static final ComboBoxType INSTANCE = new ComboBoxType( create().name( InputTypeName.COMBO_BOX ) );

    public ComboBoxType( final Builder builder )
    {
        super( builder );
    }

    @Override
    public Value createValue( final Value value, final InputTypeConfig config )
    {
        return ValueFactory.newString( value.asString() );
    }

    @Override
    public Value createDefaultValue( final Input input )
    {
        final String defaultValue = input.getDefaultValue().getRootValue();
        if ( !isNullOrEmpty( defaultValue ) )
        {
            return ValueFactory.newString( defaultValue );
        }
        return super.createDefaultValue( input );
    }

    @Override
    public void validate( final Property property, final InputTypeConfig config )
    {
        validateType( property, ValueTypes.STRING );

        final String valueAsString = property.getString();
        final boolean flag = ( valueAsString != null ) && config.hasAttributeValue( "option", "value", valueAsString );
        validateValue( property, flag, "Value is not a valid option" );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {

        @Override
        public ComboBoxType build()
        {
            return new ComboBoxType( this );
        }
    }
}
