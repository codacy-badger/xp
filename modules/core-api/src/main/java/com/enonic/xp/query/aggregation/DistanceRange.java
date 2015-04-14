package com.enonic.xp.query.aggregation;

import com.google.common.annotations.Beta;

@Beta
public class DistanceRange
    extends Range
{
    private final Double from;

    private final Double to;

    private DistanceRange( final Builder builder )
    {
        super( builder );
        from = builder.from;
        to = builder.to;
    }

    public Double getFrom()
    {
        return from;
    }

    public Double getTo()
    {
        return to;
    }

    public static Builder create()
    {
        return new Builder();
    }


    public static final class Builder
        extends Range.Builder<Builder>
    {
        private Double from;

        private Double to;

        private Builder()
        {
        }

        public Builder from( Double from )
        {
            this.from = from;
            return this;
        }

        public Builder to( Double to )
        {
            this.to = to;
            return this;
        }

        public DistanceRange build()
        {
            return new DistanceRange( this );
        }
    }
}
