package com.enonic.wem.core.index;

public enum Index
{
    WEM( "wem" ),
    NODB( "nodb" );

    private final String name;

    private Index( final String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
