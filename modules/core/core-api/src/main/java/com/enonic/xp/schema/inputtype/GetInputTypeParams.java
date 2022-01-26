package com.enonic.xp.schema.inputtype;

import com.google.common.base.Preconditions;

import com.enonic.xp.inputtype.InputTypeName;

public class GetInputTypeParams
{
    private final InputTypeName name;

    public GetInputTypeParams( final Builder builder )
    {
        this.name = builder.name;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public InputTypeName getName()
    {
        return name;
    }

    public static class Builder
    {
        private InputTypeName name;

        public Builder name( final InputTypeName name )
        {
            this.name = name;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( name, "name cannot be null" );
        }

        public GetInputTypeParams build()
        {
            validate();
            return new GetInputTypeParams( this );
        }
    }
}
