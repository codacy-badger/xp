package com.enonic.xp.app;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ApplicationWildcardResolver
{
    public static final String APP_WILDCARD = "${app}";

    private static final String APP_WILDCARD_PREFIX = APP_WILDCARD + ":";

    public static final String ANY_WILDCARD = "*";

    public boolean stringHasWildcard( final String contentTypeName )
    {
        return contentTypeName.contains( ANY_WILDCARD ) || contentTypeName.startsWith( APP_WILDCARD_PREFIX );
    }

    @Deprecated
    public boolean startWithAppWildcard( String s )
    {
        return s.startsWith( APP_WILDCARD_PREFIX );
    }

    @Deprecated
    public boolean hasAnyWildcard( String s )
    {
        return s.contains( ANY_WILDCARD );
    }

    @Deprecated
    public String resolveAppWildcard( final String nameToResolve, final ApplicationKey applicationKey )
    {
        return resolveAppPlaceholder( nameToResolve, applicationKey );
    }

    public static Predicate<String> predicate( final ApplicationKey applicationKey, final String wildcard )
    {
        return predicate( applicationKey, wildcard, Object::toString );
    }

    public static <T> Predicate<T> predicate( final ApplicationKey applicationKey, final String wildcard,
                                              Function<T, String> toNameFunction )
    {
        final String resolvedAppName = resolveAppPlaceholder( wildcard, applicationKey );
        // Confusing pattern. Left as-is for backwards compatibility.
        final Predicate<String> predicate = resolvedAppName.contains( ANY_WILDCARD )
            ? Pattern.compile( resolvedAppName.replace( "*", ".*" ) ).asMatchPredicate()
            : resolvedAppName::equals;

        return t -> predicate.test( toNameFunction.apply( t ) );
    }

    public static <T> Predicate<T> predicate( final ApplicationKey applicationKey, final Collection<String> wildcards,
                                              Function<T, String> toNameFunction )
    {
        return wildcards.
            stream().
            map( wildcard -> ApplicationWildcardResolver.predicate( applicationKey, wildcard, toNameFunction ) ).
            reduce( Predicate::or ).
            orElse( s -> false );
    }

    private static String resolveAppPlaceholder( final String nameToResolve, final ApplicationKey applicationKey )
    {
        return nameToResolve.startsWith( APP_WILDCARD_PREFIX )
            ? applicationKey + nameToResolve.substring( APP_WILDCARD.length() )
            : nameToResolve;
    }
}
