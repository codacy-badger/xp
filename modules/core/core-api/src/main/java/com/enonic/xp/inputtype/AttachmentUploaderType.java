package com.enonic.xp.inputtype;


import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;

final class AttachmentUploaderType
    extends InputTypeBase
{
    public static final AttachmentUploaderType INSTANCE = new AttachmentUploaderType( create().name( InputTypeName.ATTACHMENT_UPLOADER ) );

    private AttachmentUploaderType( final Builder builder )
    {
        super( builder );
    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    public Value createValue( final Value value, final InputTypeConfig config )
    {
        return ValueFactory.newString( value.asString() );
    }

    @Override
    public void validate( final Property property, final InputTypeConfig config )
    {
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {

        @Override
        public AttachmentUploaderType build()
        {
            return new AttachmentUploaderType( this );
        }
    }
}
