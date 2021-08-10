package com.enonic.xp.core.impl.content;

import java.time.Instant;
import java.util.UUID;

import com.google.common.base.Preconditions;

import com.enonic.xp.archive.ArchiveConstants;
import com.enonic.xp.archive.ArchiveContentException;
import com.enonic.xp.archive.ArchiveContentListener;
import com.enonic.xp.archive.ArchiveContentParams;
import com.enonic.xp.archive.ArchiveContentsResult;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentAccessException;
import com.enonic.xp.content.ContentAlreadyExistsException;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.CreateContentParams;
import com.enonic.xp.content.MoveContentListener;
import com.enonic.xp.content.MoveContentParams;
import com.enonic.xp.content.MoveContentsResult;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.MoveNodeException;
import com.enonic.xp.node.NodeAccessException;
import com.enonic.xp.node.NodeAlreadyExistAtPathException;
import com.enonic.xp.schema.content.ContentTypeName;

final class ArchiveContentCommand
    extends AbstractArchiveCommand
    implements MoveContentListener
{
    private final ArchiveContentParams params;

    private final ArchiveContentListener archiveContentListener;

    private ArchiveContentCommand( final Builder builder )
    {
        super( builder );
        this.params = builder.params;
        this.archiveContentListener = builder.archiveContentListener;
    }

    public static Builder create( final ArchiveContentParams params )
    {
        return new Builder( params );
    }

    ArchiveContentsResult execute()
    {
        params.validate();

        try
        {
            return doExecute();
        }
        catch ( MoveNodeException e )
        {
            throw new ArchiveContentException( e.getMessage(), ContentPath.from( e.getPath().toString() ) );
        }
        catch ( NodeAlreadyExistAtPathException e )
        {
            throw new ContentAlreadyExistsException( ContentPath.from( e.getNode().toString() ), e.getRepositoryId(), e.getBranch() );
        }
        catch ( NodeAccessException e )
        {
            throw new ContentAccessException( e );
        }
    }

    private ArchiveContentsResult doExecute()
    {
        final Content contentToArchive = contentService.getById(  params.getContentId() );

        final Content container = contentService.create( containerParams( contentToArchive ) );

        final MoveContentParams.Builder builder = MoveContentParams.create().
            contentId(  params.getContentId()  ).
            parentContentPath( container.getPath() ).
            moveContentListener( this );

        final MoveContentsResult result = contentService.move( builder.build() );

        return ArchiveContentsResult.create().
            addArchived( result.getMovedContents() ).
            build();
    }

    private CreateContentParams containerParams( final Content contentToArchive )
    {
        final String uniqueName = UUID.randomUUID().toString();
        final String displayName = Instant.now().toString();

        final PropertyTree data = new PropertyTree();
        data.setString( "oldParentPath", contentToArchive.getParentPath().toString() );

        return CreateContentParams.create().
            parent( ArchiveConstants.ARCHIVE_ROOT_PATH ).
            name( uniqueName ).
            displayName( displayName ).
//            id( NodeId.from( uniqueName ) ).
//            type( ArchiveConstants.ARCHIVE_CONTENT_TYPE ).
            type( ContentTypeName.folder() ).
            contentData( data ).
            build();
    }

    @Override
    public void contentMoved( final int count )
    {
        if ( archiveContentListener != null )
        {
            archiveContentListener.contentArchived( count );
        }
    }

    @Override
    public void setTotal( final int count )
    {
        if ( archiveContentListener != null )
        {
            archiveContentListener.setTotal( count );
        }
    }

    public static class Builder
        extends AbstractArchiveCommand.Builder<Builder>
    {
        private final ArchiveContentParams params;

        private ArchiveContentListener archiveContentListener;

        private Builder( final ArchiveContentParams params )
        {
            this.params = params;
        }

        public Builder archiveListener( final ArchiveContentListener archiveContentListener )
        {
            this.archiveContentListener = archiveContentListener;
            return this;
        }

        @Override
        void validate()
        {
            super.validate();
            Preconditions.checkNotNull( params );
        }

        public ArchiveContentCommand build()
        {
            validate();
            return new ArchiveContentCommand( this );
        }
    }

}
