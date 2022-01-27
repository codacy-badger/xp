package com.enonic.xp.inputtype;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueType;
import com.enonic.xp.form.Input;

public final class CustomInputType
    extends InputTypeBase
{
    private ValueType<?> valueType;

    private CustomInputType( final Builder builder )
    {
        super( builder );
        this.valueType = builder.valueType;
    }

//    public static CustomInputType from( final InputTypeName name )
//    {
//        if ( ApplicationKey.BASE.equals( name.getApplicationKey() ) )
//        {
//            throw new IllegalArgumentException( "Base input type cannot be custom" );
//        }
//        else
//        {
//            return new CustomInputType( name );
//        }
//    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    public Value createDefaultValue( final Input input )
    {
//        final Boolean rootValue = Boolean.valueOf(input.getDefaultValue().getRootValue());
//        if ( rootValue != null )
//        {
//            return ValueFactory.newBoolean( rootValue );
//        }
        return super.createDefaultValue( input );
    }

    @Override
    public Value createValue( final Value value, final InputTypeConfig config )
    {
        return value;
    }

    @Override
    public void validate( final Property property, final InputTypeConfig config )
    {
        if ( valueType != null )
        {
            validateType( property, valueType );
        }
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {
        private ValueType<?> valueType;

        public Builder valueType( final ValueType<?> valueType )
        {
            this.valueType = valueType;
            return this;
        }


        @Override
        public CustomInputType build()
        {
            return new CustomInputType( this );
        }
    }
}
