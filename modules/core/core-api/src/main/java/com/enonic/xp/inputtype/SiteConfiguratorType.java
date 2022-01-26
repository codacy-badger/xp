package com.enonic.xp.inputtype;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;

final class SiteConfiguratorType
    extends InputTypeBase
{
    public static final SiteConfiguratorType INSTANCE = new SiteConfiguratorType( create().name( InputTypeName.SITE_CONFIGURATOR ) );

    private SiteConfiguratorType( final Builder builder )
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
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {

        @Override
        public SiteConfiguratorType build()
        {
            return new SiteConfiguratorType( this );
        }
    }

}
