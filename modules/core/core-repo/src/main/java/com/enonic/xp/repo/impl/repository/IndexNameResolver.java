package com.enonic.xp.repo.impl.repository;

import java.util.List;
import java.util.stream.Collectors;

import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryIds;

public class IndexNameResolver
{
    private static final String SEARCH_INDEX_PREFIX = "search";

    private static final String STORAGE_INDEX_PREFIX = "storage";

    private static final String DIVIDER = "-";

    public static String resolveStorageIndexName( final RepositoryId repositoryId )
    {
        return STORAGE_INDEX_PREFIX + DIVIDER + repositoryId.toString();
    }

    public static String resolveSearchIndexName( final RepositoryId repositoryId )
    {
        return SEARCH_INDEX_PREFIX + DIVIDER + repositoryId.toString();
    }

    public static List<String> resolveIndexNames( final RepositoryId repositoryId )
    {
        return List.of( IndexNameResolver.resolveStorageIndexName( repositoryId ),
                          IndexNameResolver.resolveSearchIndexName( repositoryId ) );
    }

    public static List<String> resolveIndexNames( final RepositoryIds repositoryIds )
    {
        return repositoryIds.stream().
            flatMap( repositoryId -> IndexNameResolver.resolveIndexNames( repositoryId ).stream() ).
            collect( Collectors.toUnmodifiableList() );
    }
}
