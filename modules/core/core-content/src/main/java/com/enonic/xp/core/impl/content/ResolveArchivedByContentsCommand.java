package com.enonic.xp.core.impl.content;

import java.util.List;

import com.google.common.base.Preconditions;

import com.enonic.xp.archive.ArchivedContainer;
import com.enonic.xp.archive.ResolveArchivedParams;

final class ResolveArchivedByContentsCommand
    extends AbstractArchiveCommand
{
    private final ResolveArchivedParams params;

    private ResolveArchivedByContentsCommand( final Builder builder )
    {
        super( builder );
        this.params = builder.params;
    }

    public static Builder create()
    {
        return new Builder();
    }

    List<ArchivedContainer> execute()
    {
//        final Contents nodesToRemove =
//            contentService.getByIds( NodeIds.from( params.getContents().stream().map( ContentId::toString ).collect( Collectors.toList() ) ) );
//
//        final List<NodeId> containerIdsToRemove = nodesToRemove.getPaths()
//            .stream()
//            .map( path -> path.asAbsolute().getElementAsString( 1 ) )
//            .distinct()
//            .map( NodeId::from )
//            .collect( Collectors.toList() );
//
//        return containerIdsToRemove.stream().map( containerId -> {
//            final FindNodesByQueryResult result = nodeService.findByQuery( NodeQuery.create()
//                                                                               .query( QueryExpr.from(
//                                                                                   CompareExpr.like( FieldExpr.from( "_path" ),
//                                                                                                   ValueExpr.string( "/" +
//                                                                                                                         ArchiveConstants.ARCHIVE_ROOT_NAME +
//                                                                                                                         "/" + containerId +
//                                                                                                                         "/*" ) ) ) )
//                                                                               .withPath( true )
//                                                                               .size( -1 )
//                                                                               .addOrderBy( FieldOrderExpr.create( "_path",
//                                                                                                                   OrderExpr.Direction.ASC ) )
//                                                                               .build() );
//
//            return ArchivedContainer.create()
//                .id( ArchivedContainerId.from( containerId.toString() ) )
//                .addContentIds( result.getNodeIds().getAsStrings().stream().map( ContentId::from ).collect( Collectors.toList() ) )
//                .build();
//        } ).collect( Collectors.toList() );
        return null;
    }

    public static class Builder
        extends AbstractArchiveCommand.Builder<Builder>
    {
        private ResolveArchivedParams params;

        private Builder()
        {
        }

        public Builder params( final ResolveArchivedParams params )
        {
            this.params = params;
            return this;
        }

        protected void validate()
        {
            super.validate();
            Preconditions.checkNotNull( this.params, "Params must be set" );
        }

        public ResolveArchivedByContentsCommand build()
        {
            validate();
            return new ResolveArchivedByContentsCommand( this );
        }
    }
}
