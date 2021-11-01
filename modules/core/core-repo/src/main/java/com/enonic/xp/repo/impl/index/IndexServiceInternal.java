package com.enonic.xp.repo.impl.index;

import java.util.Map;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.index.IndexType;
import com.enonic.xp.repository.IndexSettings;
import com.enonic.xp.repository.RepositoryId;

public interface IndexServiceInternal
{
    void createIndex( CreateIndexRequest request );

    void updateIndices( UpdateIndexSettings settings, String... indices );

    void deleteIndices( String... indices );

    boolean indicesExists( String... indices );

    void closeIndices( String... indices );

    void openIndices( String... indices );

    boolean waitForYellowStatus( String... indices );

    IndexSettings getIndexSettings( RepositoryId repositoryId, IndexType indexType );

    Map<String, Object> getIndexMapping( RepositoryId repositoryId, Branch branch, IndexType indexType );

    void refresh( String... indexNames );

    boolean isMaster();
}

