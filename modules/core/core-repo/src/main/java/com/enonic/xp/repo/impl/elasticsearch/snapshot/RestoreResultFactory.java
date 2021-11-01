package com.enonic.xp.repo.impl.elasticsearch.snapshot;

import org.elasticsearch.snapshots.RestoreInfo;

import com.enonic.xp.node.RestoreResult;
import com.enonic.xp.repository.RepositoryId;

class RestoreResultFactory
{
    static RestoreResult create( final RestoreInfo restoreInfo, final RepositoryId respositoryId )
    {
        if ( restoreInfo.failedShards() > 0 )
        {
            return RestoreResult.create().
                failed( true ).
                name( restoreInfo.name() ).
                indices( restoreInfo.indices() ).
                message( "Restore failed, " + restoreInfo.failedShards() + " of " + restoreInfo.totalShards() +
                             " shards failed" ).
                build();
        }

        return RestoreResult.create().
            repositoryId( respositoryId ).
            name( restoreInfo.name() ).
            indices( restoreInfo.indices() ).
            message( "Restore successful, " + restoreInfo.successfulShards() + " shards restored" ).
            build();
    }

}
