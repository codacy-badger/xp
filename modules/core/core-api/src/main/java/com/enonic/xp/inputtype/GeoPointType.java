package com.enonic.xp.inputtype;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueTypes;
import com.enonic.xp.form.Input;
import com.enonic.xp.util.GeoPoint;

final class GeoPointType
    extends InputTypeBase
{
    public static final GeoPointType INSTANCE = new GeoPointType( create().name( InputTypeName.GEO_POINT ) );

    public GeoPointType( final Builder builder )
    {
        super( builder );
    }

    @Override
    public Value createValue( final Value value, final InputTypeConfig config )
    {
        return ValueFactory.newGeoPoint( value.asGeoPoint() );
    }

    @Override
    public Value createDefaultValue( final Input input )
    {
        final String rootValue = input.getDefaultValue().getRootValue();
        if ( rootValue != null )
        {
            return ValueFactory.newGeoPoint( GeoPoint.from( rootValue ) );
        }
        return super.createDefaultValue( input );
    }

    @Override
    public void validate( final Property property, final InputTypeConfig config )
    {
        validateType( property, ValueTypes.GEO_POINT );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {

        @Override
        public GeoPointType build()
        {
            return new GeoPointType( this );
        }
    }
}
