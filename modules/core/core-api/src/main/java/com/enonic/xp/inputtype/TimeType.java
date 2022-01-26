package com.enonic.xp.inputtype;

import java.time.Instant;
import java.time.ZoneId;

import com.enonic.xp.data.Property;
import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueTypeException;
import com.enonic.xp.data.ValueTypes;
import com.enonic.xp.form.Input;

final class TimeType
    extends InputTypeBase
{
    public static final TimeType INSTANCE = new TimeType( create().name( InputTypeName.TIME ) );

    private TimeType( final Builder builder )
    {
        super( builder );
    }

    @Override
    public Value createValue( final Value value, final InputTypeConfig config )
    {
        return ValueFactory.newLocalTime( value.asLocalTime() );
    }

    @Override
    public Value createDefaultValue( final Input input )
    {
        final String defaultValue = input.getDefaultValue().getRootValue();
        if ( defaultValue != null )
        {
            try
            {
                return ValueFactory.newLocalTime( ValueTypes.LOCAL_TIME.convert( defaultValue ) );
            }
            catch ( ValueTypeException e )
            {
                final RelativeTime result = RelativeTimeParser.parse( defaultValue );

                if ( result != null )
                {
                    final Instant instant = Instant.now().plus( result.getTime() );
                    return ValueFactory.newLocalTime( instant.atZone( ZoneId.systemDefault() ).toLocalTime().withNano( 0 ) );
                }
                else
                {
                    throw new IllegalArgumentException( "Invalid Date format: " + defaultValue );
                }
            }
        }
        return super.createDefaultValue( input );
    }

    @Override
    public void validate( final Property property, final InputTypeConfig config )
    {
        validateType( property, ValueTypes.LOCAL_TIME );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
        extends InputTypeBase.Builder<Builder>
    {

        @Override
        public TimeType build()
        {
            return new TimeType( this );
        }
    }
}
