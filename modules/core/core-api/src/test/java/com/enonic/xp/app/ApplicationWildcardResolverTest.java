package com.enonic.xp.app;


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationWildcardResolverTest
{
    private ApplicationWildcardResolver applicationWildcardResolver;

    @BeforeEach
    public void init()
    {
        this.applicationWildcardResolver = new ApplicationWildcardResolver();
    }

    @Test
    public void test_has_any_wildcard()
    {
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "*test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "test@!*%$sw" ) );
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "test w* test" ) );
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "test test*" ) );

        assertFalse( this.applicationWildcardResolver.hasAnyWildcard( "test$%^&" ) );
    }

    @Test
    public void test_string_has_wildcard()
    {
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "${app}:test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "${app}:  test$%^&" ) );

        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "*test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "test@!*%$sw" ) );

        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "${ap}:test*$%^&" ) );
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "*${app}:  test$%^&" ) );
    }

    @Test
    public void test_has_app_wildcard()
    {
        assertTrue( this.applicationWildcardResolver.startWithAppWildcard( "${app}:test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.startWithAppWildcard( "${app}:  test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.startWithAppWildcard( "${app}:${app} test$%^&" ) );

        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "{app}:test$%^&" ) );
        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "$(app):test$%^&" ) );
        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "${ap}:test$%^&" ) );
        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "S${app}:test$%^&" ) );
    }

    @Test
    public void test_resolve_app_wildcard()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "app.myapp1" );
        assertEquals( "app.myapp1:folder", this.applicationWildcardResolver.resolveAppWildcard( "${app}:folder", applicationKey ) );
        assertEquals( "${app}${app}:folder", this.applicationWildcardResolver.resolveAppWildcard( "${app}${app}:folder", applicationKey ) );
        assertEquals( "${app}folder", this.applicationWildcardResolver.resolveAppWildcard( "${app}folder", applicationKey ) );
    }

    @Test
    public void resolveWildcards_exact()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "my.app" );

        final Set<String> result = List.of( "base:folder", "my.app:folder", "my.app:quote", "my.other.app:quote" ).
            stream().
            filter( ApplicationWildcardResolver.predicate( applicationKey, "base:folder" ) ).
            collect( Collectors.toCollection( LinkedHashSet::new ) );
        assertThat( result ).containsExactly( "base:folder" );
    }

    @Test
    public void resolveWildcards_exact_from_my_app()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "my.app" );

        final Set<String> result = List.of( "base:folder", "my.app:folder", "my.other.app:quote" ).
            stream().
            filter( ApplicationWildcardResolver.predicate( applicationKey, "${app}:folder" ) ).
            collect( Collectors.toCollection( LinkedHashSet::new ) );
        assertThat( result ).containsExactly( "my.app:folder" );
    }

    @Test
    public void resolveWildcards_all()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "my.app" );

        final Set<String> result = List.of( "base:folder", "my.app:folder", "my.app:quote", "my.other.app:quote" ).
            stream().
            filter( ApplicationWildcardResolver.predicate( applicationKey, "*" ) ).
            collect( Collectors.toCollection( LinkedHashSet::new ) );
        assertThat( result ).containsExactly( "base:folder", "my.app:folder", "my.app:quote", "my.other.app:quote" );
    }

    @Test
    public void resolveWildcards_from_any_app()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "my.app" );

        final Set<String> result = List.of( "base:folder", "my.app:folder", "my.app:quote", "my.other.app:quote" ).
            stream().
            filter( ApplicationWildcardResolver.predicate( applicationKey, "*:quote" ) ).
            collect( Collectors.toCollection( LinkedHashSet::new ) );
        assertThat( result ).containsExactly( "my.app:quote", "my.other.app:quote" );
    }

    @Test
    public void resolveWildcards_from_my_app()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "my.app" );

        final Set<String> result = List.of( "base:folder", "my.app:folder", "my.app:quote", "my.other.app:quote" ).
            stream().
            filter( ApplicationWildcardResolver.predicate( applicationKey, "${app}:*" ) ).
            collect( Collectors.toCollection( LinkedHashSet::new ) );
        assertThat( result ).containsExactly( "my.app:folder", "my.app:quote" );
    }

    @Test
    public void resolveWildcards_except()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "my.app" );

        final Set<String> result = List.of( "base:folder", "my.app:folder", "my.app:quote", "my.other.app:quote" ).
            stream().
            filter( ApplicationWildcardResolver.predicate( applicationKey, "^(?!base:folder$).*" ) ).
            collect( Collectors.toCollection( LinkedHashSet::new ) );
        assertThat( result ).containsExactly( "my.app:folder", "my.app:quote", "my.other.app:quote" );
    }

    @Test
    public void resolveWildcards_startsWith()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "my.app" );

        final Set<String> result = List.of( "base:folder", "my.app:folder", "my.app:quote", "my.other.app:quote" ).
            stream().
            filter( ApplicationWildcardResolver.predicate( applicationKey, "*:q*" ) ).
            collect( Collectors.toCollection( LinkedHashSet::new ) );
        assertThat( result ).containsExactly( "my.app:quote", "my.other.app:quote" );
    }
}
