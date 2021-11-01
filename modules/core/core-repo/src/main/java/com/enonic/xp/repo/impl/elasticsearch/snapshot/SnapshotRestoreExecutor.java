package com.enonic.xp.repo.impl.elasticsearch.snapshot;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequestBuilder;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.snapshots.RestoreInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.node.RestoreResult;
import com.enonic.xp.repo.impl.index.IndexServiceInternal;

public class SnapshotRestoreExecutor
    extends AbstractSnapshotExecutor
{
    private static final Logger LOG = LoggerFactory.getLogger( SnapshotRestoreExecutor.class );

    private final IndexServiceInternal indexServiceInternal;

    private final String[] indicesToClose;

    private final String[] indicesToRestore;

    private SnapshotRestoreExecutor( final Builder builder )
    {
        super( builder );
        this.indexServiceInternal = builder.indexServiceInternal;
        this.indicesToClose = builder.indicesToClose.toArray( String[]::new );
        this.indicesToRestore = builder.indicesToRestore.toArray( String[]::new );
    }

    public RestoreResult execute()
    {
        indexServiceInternal.closeIndices( indicesToClose );
        try
        {
            final RestoreInfo restoreInfo = executeRestoreRequest( indicesToRestore );
            return RestoreResultFactory.create( restoreInfo, null );
        }
        catch ( ElasticsearchException e )
        {
            return RestoreResult.create()
                .repositoryId( null )
                .indices( List.of( indicesToRestore ) )
                .failed( true )
                .name( snapshotName )
                .message( "Could not restore snapshot: " + e + " for indices: " + Arrays.asList( indicesToClose ) )
                .build();
        }
        finally
        {
            indexServiceInternal.openIndices( indicesToClose );
        }
    }

    public RestoreInfo executeRestoreRequest( final String... indices )
    {
        LOG.info( "Restoring indices {}", Arrays.toString( indices ) );

        final RestoreSnapshotRequestBuilder restoreSnapshotRequestBuilder =
            new RestoreSnapshotRequestBuilder( this.client.admin().cluster(), RestoreSnapshotAction.INSTANCE ).setRestoreGlobalState(
                    false )
                .setIndices( indices )
                .setRepository( snapshotRepositoryName )
                .setSnapshot( snapshotName )
                .setWaitForCompletion( true );

        final RestoreSnapshotResponse response =
            this.client.admin().cluster().restoreSnapshot( restoreSnapshotRequestBuilder.request() ).actionGet();
        LOG.info( "Restored indices {} with status {}", Arrays.toString( indices ), response.status() );
        return response.getRestoreInfo();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static final class Builder
        extends AbstractSnapshotExecutor.Builder<Builder>
    {
        private IndexServiceInternal indexServiceInternal;

        private Collection<String> indicesToClose;

        private Collection<String> indicesToRestore;

        public Builder indexServiceInternal( final IndexServiceInternal indexServiceInternal )
        {
            this.indexServiceInternal = indexServiceInternal;
            return this;
        }

        public Builder indicesToClose( final Collection<String> indices )
        {
            this.indicesToClose = indices;
            return this;
        }

        public Builder indicesToRestore( final Collection<String> indices )
        {
            this.indicesToRestore = indices;
            return this;
        }

        public SnapshotRestoreExecutor build()
        {
            return new SnapshotRestoreExecutor( this );
        }
    }
}
