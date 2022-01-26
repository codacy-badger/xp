package com.enonic.xp.inputtype;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueType;
import com.enonic.xp.form.Input;
import com.enonic.xp.schema.BaseSchema;

@PublicApi
public abstract class InputTypeBase
    extends BaseSchema<InputTypeName>
    implements InputType
{
    protected InputTypeBase( final Builder<?> builder )
    {
        super( builder );
    }

    private static boolean inSet( final ValueType check, final ValueType... types )
    {
        for ( final ValueType type : types )
        {
            if ( type.equals( check ) )
            {
                return true;
            }
        }

        return false;
    }

//    @Override
//    public final InputTypeName getName()
//    {
//        return name;
//    }

    @Override
    public final String toString()
    {
        return getName().toString();
    }

    @Override
    public Value createValue( final String value, final InputTypeConfig config )
    {
        return createValue( ValueFactory.newString( value ), config );
    }

    @Override
    public abstract Value createValue( Value value, InputTypeConfig config );

    @Override
    public Value createDefaultValue( final Input input )
    {
        return null;
    }

    protected final void validateType( final Property property, final ValueType expectedType )
    {
        final ValueType actualType = property.getType();
        if ( !actualType.equals( expectedType ) )
        {
            throw InputTypeValidationException.invalidType( property, actualType, expectedType );
        }
    }

    protected final void validateType( final Property property, final ValueType... expectedTypes )
    {
        final ValueType actualType = property.getType();
        if ( !inSet( actualType, expectedTypes ) )
        {
            throw InputTypeValidationException.invalidType( property, expectedTypes );
        }
    }

    protected final void validateValue( final Property property, final boolean flag, final String message )
    {
        if ( !flag )
        {
            throw InputTypeValidationException.invalidValue( property, message );
        }
    }

    @Override
    public abstract void validate( Property property, InputTypeConfig config );

    public abstract static class Builder<T extends Builder<T>>
        extends BaseSchema.Builder<T, InputTypeName>
    {
//        protected InputTypeName name;

        protected Builder()
        {
            super();
        }

//        public T name( final InputTypeName name )
//        {
//            this.name = name;
//            return (T)this;
//        }

        //        protected void validate()
//        {
//            Preconditions.checkNotNull( name, "name must be set" );
//        }
//
        public abstract InputTypeBase build();
    }
}
