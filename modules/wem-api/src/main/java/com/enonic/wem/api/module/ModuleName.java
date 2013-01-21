package com.enonic.wem.api.module;


public final class ModuleName
{
    public static final ModuleName SYSTEM = new ModuleName( "System" );

    private final String name;

    public ModuleName( final String name )
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals( final Object o )
    {
        return ( o instanceof ModuleName ) && ( (ModuleName) o ).name.equals( this.name );
    }

    public static ModuleName from( String value )
    {
        return new ModuleName( value );
    }
}
