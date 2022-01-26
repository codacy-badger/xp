package com.enonic.xp.inputtype;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueTypes;

final class ContentSelectorType
    extends InputTypeBase
{
    public static final ContentSelectorType INSTANCE = new ContentSelectorType( create().name( InputTypeName.CONTENT_SELECTOR ) );

    public ContentSelectorType( final Builder builder )
    {
        super( builder );
    }

    @Override
    public Value createValue( final Value value, final InputTypeConfig config )
    {
        return ValueFactory.newReference( value.asReference() );
    }

    @Override
    public void validate( final Property property, final InputTypeConfig config )
    {
        validateType( property, ValueTypes.REFERENCE );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {

        @Override
        public ContentSelectorType build()
        {
            return new ContentSelectorType( this );
        }
    }
}

