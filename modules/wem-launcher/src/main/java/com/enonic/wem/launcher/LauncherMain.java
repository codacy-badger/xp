package com.enonic.wem.launcher;

public final class LauncherMain
{
    public static void main( final String... args )
        throws Exception
    {
        System.setProperty( "wem.home", "/Users/srs/development/cms-homes/wem-home" );
        new Launcher().launch();
    }
}
