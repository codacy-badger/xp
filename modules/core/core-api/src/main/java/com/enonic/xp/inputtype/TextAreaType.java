package com.enonic.xp.inputtype;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueTypes;
import com.enonic.xp.form.Input;

import static com.google.common.base.Strings.isNullOrEmpty;

final class TextAreaType
    extends TextInputTypeBase
{
    public static final TextAreaType INSTANCE = new TextAreaType( create().name( InputTypeName.TEXT_AREA ) );

    public TextAreaType( final Builder builder )
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
        super.validate( property, config );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
        extends TextInputTypeBase.Builder<Builder>
    {

        @Override
        public TextAreaType build()
        {
            return new TextAreaType( this );
        }
    }
}
