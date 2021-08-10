package com.enonic.xp.core.impl.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import com.enonic.xp.archive.ArchiveConstants;
import com.enonic.xp.archive.ArchivedContainerId;
import com.enonic.xp.archive.ArchivedContainerLayer;
import com.enonic.xp.archive.ListContentsParams;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentQuery;
import com.enonic.xp.content.FindContentIdsByQueryResult;
import com.enonic.xp.content.FindContentPathsByQueryResult;
import com.enonic.xp.query.expr.CompareExpr;
import com.enonic.xp.query.expr.FieldExpr;
import com.enonic.xp.query.expr.QueryExpr;
import com.enonic.xp.query.expr.ValueExpr;

final class ListArchivedContentCommand
    extends AbstractArchiveCommand
{
    private static final Pattern ARCHIVED_CONTENT_PATTERN =
        Pattern.compile( "^(?:/" + ArchiveConstants.ARCHIVE_ROOT_NAME + "/)([a-zA-Z0-9_\\-.:]+)/(?:[^/]+)$" );

    private final ListContentsParams params;

    private ListArchivedContentCommand( final Builder builder )
    {
        super( builder );
        this.params = builder.params;
    }

    public static Builder create()
    {
        return new Builder();
    }

    List<ArchivedContainerLayer> execute()
    {
        final Map<String, Set<ContentId>> archived =
            params.getParent() != null ? fetchContainerLayer( params.getParent() ) : fetchAllContainersRootLayers();

        return archived.entrySet().stream().map( entry -> {
            final Content container = contentService.getById( ContentId.from( entry.getKey() ) );

            return ArchivedContainerLayer.create()
                .id( ArchivedContainerId.from( entry.getKey() ) )
                .addContentIds( entry.getValue() )
                .archiveTime( container.getCreatedTime() )
                .parent( params.getParent() != null ? params.getParent() : null )
                .build();
        } ).collect( Collectors.toList() );
    }

    private Map<String, Set<ContentId>> fetchAllContainersRootLayers()
    {
        final Map<String, Set<ContentId>> archived = new HashMap<>();

        final FindContentPathsByQueryResult result = contentService.findPaths( ContentQuery.create()
                                                                           .queryExpr( QueryExpr.from(
                                                                               CompareExpr.like( FieldExpr.from( "_path" ),
                                                                                                 ValueExpr.string( "/" +
                                                                                                                       ArchiveConstants.ARCHIVE_ROOT_NAME +
                                                                                                                       "/*/*/" ) ) ) )
                                                                           .size( -1 )
                                                                           .build() );

        for ( final ContentPath path : result.getContentPaths() )
        {
                final String containerId = path.getElement( 1 );

                final Set<ContentId> contentsInContainer = archived.computeIfAbsent( containerId, id -> new HashSet() );
                contentsInContainer.add( contentService.getByPath( path ).getId() );
        }
        return archived;
    }

    private Map<String, Set<ContentId>> fetchContainerLayer( final ContentId parent )
    {
        final Map<String, Set<ContentId>> archived = new HashMap<>();

        final ContentPath parentPath = contentService.getById( parent ).getParentPath().asRelative();

        final FindContentIdsByQueryResult result = contentService.find( ContentQuery.create()
                                                                           .queryExpr( QueryExpr.from(
                                                                               CompareExpr.eq( FieldExpr.from( "_parentPath" ),
                                                                                               ValueExpr.string( "/" + parentPath ) ) ) )
                                                                           .size( -1 )
                                                                           .build() );

        final String containerId = parentPath.getElement( 1 );

        for ( final ContentId contentId : result.getContentIds() )
        {
            final Set<ContentId> contentsInContainer = archived.computeIfAbsent( containerId, id -> new HashSet() );
            contentsInContainer.add( contentId );
        }

        return archived;
    }

    public static class Builder
        extends AbstractArchiveCommand.Builder<Builder>
    {
        private ListContentsParams params;

        private Builder()
        {
        }

        public Builder params( final ListContentsParams params )
        {
            this.params = params;
            return this;
        }

        protected void validate()
        {
            super.validate();
            Preconditions.checkNotNull( this.params, "Params must be set" );
        }

        public ListArchivedContentCommand build()
        {
            validate();
            return new ListArchivedContentCommand( this );
        }
    }
}
