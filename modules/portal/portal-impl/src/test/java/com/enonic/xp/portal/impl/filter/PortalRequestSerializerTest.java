package com.enonic.xp.portal.impl.filter;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.script.ScriptEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.RenderMode;
import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.script.impl.util.JavascriptHelperFactory;
import com.enonic.xp.script.impl.util.NashornHelper;
import com.enonic.xp.script.impl.value.ScriptValueFactory;
import com.enonic.xp.script.impl.value.ScriptValueFactoryImpl;
import com.enonic.xp.web.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PortalRequestSerializerTest
{
    private ScriptValueFactory factory;

    private ScriptEngine engine;


    @BeforeEach
    public void setup()
    {
        this.engine = NashornHelper.getScriptEngine( getClass().getClassLoader() );

        final JavascriptHelperFactory factory = new JavascriptHelperFactory( this.engine );
        this.factory = new ScriptValueFactoryImpl( factory.create() );
    }

    @Test
    public void serializeComplete()
        throws Exception
    {
        final PortalRequest sourceRequest = new PortalRequest();
        final String jsonRequest = readResource( "PortalRequestSerializer_request.json" );
        final Object obj = execute( "var result = " + jsonRequest + "; result;" );
        final ScriptValue value = this.factory.newValue( obj );

        PortalRequestSerializer reqSerializer = new PortalRequestSerializer( sourceRequest, value );

        final PortalRequest portalRequest = reqSerializer.serialize();

        assertNotNull( portalRequest );
        assertEquals( HttpMethod.POST, portalRequest.getMethod() );
        assertEquals( "https", portalRequest.getScheme() );
        assertEquals( "myhost", portalRequest.getHost() );
        assertEquals( 1234, portalRequest.getPort() );
        assertEquals( "/some/path", portalRequest.getPath() );
        assertEquals( "https://myhost:1234/some/path/url", portalRequest.getUrl() );
        assertEquals( "127.0.0.42", portalRequest.getRemoteAddress() );
        assertEquals( RenderMode.EDIT, portalRequest.getMode() );
        assertEquals( true, portalRequest.isValidTicket() );
        assertEquals( Branch.from( "draft" ), portalRequest.getBranch() );
        assertEquals( "application/json", portalRequest.getContentType() );
        assertEquals( "POST BODY", portalRequest.getBody() );

        assertEquals( 2, portalRequest.getHeaders().size() );
        assertEquals( "header-value", portalRequest.getHeaders().get( "header" ) );
        assertEquals( "header-value2", portalRequest.getHeaders().get( "header2" ) );

        assertEquals( 2, portalRequest.getCookies().size() );
        assertEquals( "cookie-value", portalRequest.getCookies().get( "cookie" ) );
        assertEquals( "cookie-value2", portalRequest.getCookies().get( "cookie2" ) );

        assertEquals( 2, portalRequest.getParams().size() );
        assertEquals( "param-value", portalRequest.getParams().get( "param" ).iterator().next() );
        assertEquals( "param-value2", portalRequest.getParams().get( "param2" ).iterator().next() );
    }

    @Test
    public void serializeNoHeadersNoParams()
        throws Exception
    {
        final PortalRequest sourceRequest = new PortalRequest();
        final String jsonRequest = readResource( "PortalRequestSerializer_request2.json" );
        final Object obj = execute( "var result = " + jsonRequest + "; result;" );
        final ScriptValue value = this.factory.newValue( obj );

        PortalRequestSerializer reqSerializer = new PortalRequestSerializer( sourceRequest, value );

        final PortalRequest portalRequest = reqSerializer.serialize();

        assertNotNull( portalRequest );
        assertEquals( HttpMethod.POST, portalRequest.getMethod() );
        assertEquals( "https", portalRequest.getScheme() );
        assertEquals( "myhost", portalRequest.getHost() );
        assertEquals( 1234, portalRequest.getPort() );
        assertEquals( "/some/path", portalRequest.getPath() );
        assertEquals( "https://myhost:1234/some/path/url", portalRequest.getUrl() );
        assertEquals( "127.0.0.42", portalRequest.getRemoteAddress() );
        assertEquals( RenderMode.EDIT, portalRequest.getMode() );
        assertEquals( true, portalRequest.isValidTicket() );
        assertEquals( Branch.from( "draft" ), portalRequest.getBranch() );
        assertEquals( "application/json", portalRequest.getContentType() );

        assertEquals( 0, portalRequest.getHeaders().size() );
        assertEquals( 0, portalRequest.getCookies().size() );
        assertEquals( 0, portalRequest.getParams().size() );
    }

    @Test
    public void serializeNonObject()
        throws Exception
    {
        final PortalRequest sourceRequest = new PortalRequest();
        final Object obj = execute( "var result = 'response'; result;" );
        final ScriptValue value = this.factory.newValue( obj );

        PortalRequestSerializer reqSerializer = new PortalRequestSerializer( sourceRequest, value );

        final PortalRequest portalRequest = reqSerializer.serialize();

        assertNotNull( portalRequest );
    }

    @Test
    public void serializeMultiParams()
        throws Exception
    {
        final PortalRequest sourceRequest = new PortalRequest();
        final String jsonRequest = readResource( "PortalRequestSerializer_request3.json" );
        final Object obj = execute( "var result = " + jsonRequest + "; result;" );
        final ScriptValue value = this.factory.newValue( obj );

        PortalRequestSerializer reqSerializer = new PortalRequestSerializer( sourceRequest, value );

        final PortalRequest portalRequest = reqSerializer.serialize();

        assertNotNull( portalRequest );
        assertEquals( Map.of( "key1", "value1", "key2", 42 ), portalRequest.getBody() );

        assertEquals( 0, portalRequest.getHeaders().size() );
        assertEquals( 0, portalRequest.getCookies().size() );
        assertEquals( 4, portalRequest.getParams().size() );
        assertEquals( "param-value", portalRequest.getParams().get( "param" ).iterator().next() );
        final List<String> param2Values = new ArrayList<>( portalRequest.getParams().get( "param2" ) );
        assertEquals( "param-value2-a", param2Values.get( 0 ) );
        assertEquals( "param-value2-b", param2Values.get( 1 ) );
        assertEquals( "param-value2-c", param2Values.get( 2 ) );
    }

    @Test
    public void serialize_no_params_duplication()
        throws Exception
    {
        final PortalRequest sourceRequest = new PortalRequest();
        sourceRequest.getParams().put( "a", "oldA" );

        sourceRequest.getParams().put( "b", "oldB" );
        sourceRequest.getParams().put( "b", "oldB" );

        sourceRequest.getParams().put( "d", "oldD" );
        sourceRequest.getParams().put( "e", "oldE" );
        sourceRequest.getParams().put( "f", "oldF" );

        final String jsonRequest = readResource( "PortalRequestSerializer_request4.json" );
        final Object obj = execute( "var result = " + jsonRequest + "; result;" );
        final ScriptValue value = this.factory.newValue( obj );

        PortalRequestSerializer reqSerializer = new PortalRequestSerializer( sourceRequest, value );

        final PortalRequest portalRequest = reqSerializer.serialize();

        assertThat( portalRequest.getParams().get( "a" ) ).containsExactly( "newA" );
        assertThat( portalRequest.getParams().get( "b" ) ).containsExactly( "newB", "1" );
        assertThat( portalRequest.getParams().get( "c" ) ).containsExactly( "newC" );
        assertThat( portalRequest.getParams().get( "d" ) ).containsExactly( "oldD" );
        assertFalse( portalRequest.getParams().containsKey( "e" ) );
        assertFalse( portalRequest.getParams().containsKey( "f" ) );
    }

    private Object execute( final String script )
        throws Exception
    {
        return this.engine.eval( script );
    }

    private String readResource( final String resourceName )
        throws Exception
    {
        try (InputStream stream = getClass().getResourceAsStream( resourceName ))
        {
            return new String( Objects.requireNonNull( stream ).readAllBytes(), StandardCharsets.UTF_8 );
        }
    }
}
