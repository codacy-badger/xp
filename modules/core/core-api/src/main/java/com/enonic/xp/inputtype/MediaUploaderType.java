package com.enonic.xp.inputtype;


import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueTypes;

final class MediaUploaderType
    extends InputTypeBase
{
    public static final MediaUploaderType INSTANCE = new MediaUploaderType( create().name( InputTypeName.MEDIA_UPLOADER ) );

    private MediaUploaderType( final Builder builder )
    {
        super( builder );
    }

    @Override
    public Value createValue( final Value value, final InputTypeConfig config )
    {
        return ValueFactory.newPropertySet( value.asData() );
    }

    @Override
    public void validate( final Property property, final InputTypeConfig config )
    {
        if ( ContentPropertyNames.MEDIA_ATTACHMENT.equals( property.getName() ) )
        {
            validateType( property, ValueTypes.STRING );
        }
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {

        @Override
        public MediaUploaderType build()
        {
            return new MediaUploaderType( this );
        }
    }
}
