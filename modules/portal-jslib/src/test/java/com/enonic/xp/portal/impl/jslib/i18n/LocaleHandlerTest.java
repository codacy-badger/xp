package com.enonic.xp.portal.impl.jslib.i18n;

import java.util.Locale;

import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.i18n.MessageBundle;
import com.enonic.xp.module.ModuleKey;
import com.enonic.xp.portal.impl.jslib.AbstractHandlerTest;
import com.enonic.xp.portal.impl.jslib.locale.LocaleHandler;
import com.enonic.xp.portal.script.command.CommandHandler;


public class LocaleHandlerTest
    extends AbstractHandlerTest
{
    private static final String RESULT_WITH_LOCALE = "result with locale";

    private static final String RESULT_WITHOUT_LOCALE = "result without locale";

    private static final String RESULT_WITHOUT_PARAMS = "result without params";

    private LocaleService localeService;

    @Override
    protected CommandHandler createHandler()
        throws Exception
    {
        this.localeService = Mockito.mock( LocaleService.class );

        final LocaleHandler handler = new LocaleHandler();
        handler.setLocaleService( this.localeService );

        return handler;
    }

    @Test
    public void localize_with_locale()
        throws Exception
    {
        final MessageBundle bundle = Mockito.mock( MessageBundle.class );
        Mockito.when( bundle.localize( Mockito.any( String.class ), Mockito.any( Object[].class ) ) ).thenReturn( RESULT_WITH_LOCALE );

        setServiceAndRun( bundle, "localize_with_locale" );
    }

    @Test
    public void localize_without_locale()
        throws Exception
    {
        final MessageBundle bundle = Mockito.mock( MessageBundle.class );
        Mockito.when( bundle.localize( Mockito.any( String.class ), Mockito.any( Object[].class ) ) ).thenReturn( RESULT_WITHOUT_LOCALE );

        setServiceAndRun( bundle, "localize_without_locale" );
    }

    @Test
    public void localize_without_params()
        throws Exception
    {
        final MessageBundle bundle = Mockito.mock( MessageBundle.class );
        Mockito.when( bundle.localize( Mockito.any( String.class ), Mockito.any( Object[].class ) ) ).thenReturn( RESULT_WITHOUT_PARAMS );

        setServiceAndRun( bundle, "localize_without_params" );
    }

    private void setServiceAndRun( MessageBundle bundle, String exportName )
        throws Exception
    {
        Mockito.when( this.localeService.getBundle( Mockito.any( ModuleKey.class ), Mockito.any( Locale.class ) ) ).
            thenAnswer( mock -> bundle );

        execute( exportName );
    }


}
