package com.enonic.wem.core.content;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.wem.api.Client;
import com.enonic.wem.api.command.Commands;
import com.enonic.wem.api.command.content.CreateContent;
import com.enonic.wem.api.command.content.CreateContentResult;
import com.enonic.wem.api.command.content.ValidateContentData;
import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentDataValidationException;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.content.CreateContentException;
import com.enonic.wem.api.content.attachment.Attachment;
import com.enonic.wem.api.content.binary.Binary;
import com.enonic.wem.api.data.Property;
import com.enonic.wem.api.data.PropertyVisitor;
import com.enonic.wem.api.data.type.ValueTypes;
import com.enonic.wem.api.exception.SystemException;
import com.enonic.wem.api.schema.content.ContentType;
import com.enonic.wem.api.schema.content.validator.DataValidationError;
import com.enonic.wem.api.schema.content.validator.DataValidationErrors;
import com.enonic.wem.api.space.SpaceName;
import com.enonic.wem.core.command.CommandHandler;
import com.enonic.wem.core.content.dao.ContentDao;
import com.enonic.wem.core.image.filter.effect.ScaleMaxFilter;
import com.enonic.wem.core.index.IndexService;
import com.enonic.wem.core.relationship.RelationshipService;
import com.enonic.wem.core.relationship.SyncRelationshipsCommand;
import com.enonic.wem.core.schema.content.dao.ContentTypeDao;

import static com.enonic.wem.api.content.attachment.Attachment.newAttachment;

public class CreateContentHandler
    extends CommandHandler<CreateContent>
{
    private static final ContentPath TEMPORARY_PARENT_PATH = ContentPath.rootOf( SpaceName.temporary() );

    private static final ContentPathNameGenerator CONTENT_PATH_NAME_GENERATOR = new ContentPathNameGenerator();

    private static final int THUMBNAIL_SIZE = 512;

    private static final String THUMBNAIL_MIME_TYPE = "image/png";

    private ContentDao contentDao;

    private ContentTypeDao contentTypeDao;

    private RelationshipService relationshipService;

    private IndexService indexService;

    private final static Logger LOG = LoggerFactory.getLogger( CreateContentHandler.class );

    @Override
    public void handle( final CreateContent command )
        throws Exception
    {
        try
        {
            final Session session = context.getJcrSession();

            final Content.Builder builder = Content.newContent();
            final String displayName = command.getDisplayName();
            final ContentPath parentContentPath = command.isTemporary() ? TEMPORARY_PARENT_PATH : command.getParentContentPath();
            final String name = command.getName();
            final ContentPath contentPath = name == null
                ? resolvePathForNewContent( parentContentPath, displayName, session )
                : ContentPath.from( parentContentPath, name );
            if ( !command.isTemporary() )
            {
                checkParentContentAllowsChildren( parentContentPath, session );
            }

            final List<Content> temporaryContents = resolveTemporaryContents( command, session );

            builder.path( contentPath );
            builder.displayName( displayName );
            builder.contentData( command.getContentData() );
            builder.type( command.getContentType() );
            builder.createdTime( DateTime.now() );
            builder.modifiedTime( DateTime.now() );
            builder.owner( command.getOwner() );
            builder.modifier( command.getOwner() );

            final Content content = builder.build();

            final Client client = context.getClient();
            validateContentData( client, content );

            final ContentId contentId = contentDao.create( content, session );
            session.save();
            addAttachments( client, contentId, command.getAttachments() );
            addMediaThumbnail( client, session, command, content, contentId );

            try
            {

                for ( Content tempContent : temporaryContents )
                {
                    final ContentPath pathToEmbeddedContent = ContentPath.createPathToEmbeddedContent( contentPath, tempContent.getName() );
                    createEmbeddedContent( tempContent, pathToEmbeddedContent, session );
                }

                relationshipService.syncRelationships( new SyncRelationshipsCommand().
                    client( client ).
                    jcrSession( session ).
                    contentType( content.getType() ).
                    contentToUpdate( contentId ).
                    contentAfterEditing( content.getContentData() ) );
                session.save();
            }
            catch ( Exception e )
            {
                // Temporary way of rollback: try delete content if any failure
                contentDao.forceDelete( contentId, session );
                session.save();
                throw e;
            }

            final Content storedContent = builder.id( contentId ).build();
            indexService.indexContent( storedContent );

            command.setResult( new CreateContentResult( contentId, contentPath ) );
        }
        catch ( final Exception e )
        {
            throw new CreateContentException( command, e );
        }
    }

    private void addMediaThumbnail( final Client client, final Session session, final CreateContent command, final Content content,
                                    final ContentId contentId )
        throws Exception
    {
        final ContentType contentType = contentTypeDao.select( content.getType(), session );
        if ( ( contentType.getSuperType() != null ) && contentType.getSuperType().isMedia() )
        {
            Attachment mediaAttachment = command.getAttachment( content.getName() );
            if ( ( mediaAttachment == null ) && ( !command.getAttachments().isEmpty() ) )
            {
                mediaAttachment = command.getAttachments().iterator().next();
            }
            if ( mediaAttachment != null )
            {
                addThumbnail( client, contentId, mediaAttachment );
            }
        }
    }

    private void checkParentContentAllowsChildren( final ContentPath parentContentPath, final Session session )
    {
        final Content content = contentDao.select( parentContentPath, session );
        if ( content != null )
        {
            final ContentType contentType = contentTypeDao.select( content.getType(), session );
            if ( !contentType.allowChildContent() )
            {
                throw new SystemException( "Content [{0}] of type [{1}] does not allow children", parentContentPath,
                                           contentType.getQualifiedName() );
            }
        }
    }

    private void createEmbeddedContent( final Content tempContent, final ContentPath pathToEmbeddedContent, final Session session )
        throws RepositoryException
    {
        contentDao.moveContent( tempContent.getId(), pathToEmbeddedContent, session );
        session.save();
    }

    private List<Content> resolveTemporaryContents( final CreateContent command, final Session session )
    {
        final List<Content> temporaryContents = new ArrayList<>();
        if ( command.getContentData() == null )
        {
            return temporaryContents;
        }
        final PropertyVisitor propertyVisitor = new PropertyVisitor()
        {
            @Override
            public void visit( final Property data )
            {
                final Content content = contentDao.select( data.getContentId(), session );
                if ( content != null )
                {
                    if ( content.isTemporary() )
                    {
                        temporaryContents.add( content );
                    }
                }
            }
        }.restrictType( ValueTypes.CONTENT_ID );
        propertyVisitor.traverse( command.getContentData() );
        return temporaryContents;
    }

    private ContentPath resolvePathForNewContent( final ContentPath parentPath, final String displayName, final Session session )
    {
        ContentPath possibleNewPath = ContentPath.from( parentPath, CONTENT_PATH_NAME_GENERATOR.generatePathName( displayName ) );
        int i = 1;
        while ( contentExists( possibleNewPath, session ) )
        {
            i++;
            possibleNewPath = ContentPath.from( parentPath, CONTENT_PATH_NAME_GENERATOR.generatePathName( displayName + "-" + i ) );
        }
        return possibleNewPath;
    }

    private boolean contentExists( final ContentPath contentPath, final Session session )
    {
        final Content content = contentDao.select( contentPath, session );
        return content != null;
    }


    private void validateContentData( final Client client, final Content content )
    {
        final ValidateContentData validateContentData = Commands.content().validate();
        validateContentData.contentType( content.getType() );
        validateContentData.contentData( content.getContentData() );
        final DataValidationErrors dataValidationErrors = client.execute( validateContentData );

        for ( DataValidationError error : dataValidationErrors )
        {
            LOG.info( "*** DataValidationError: " + error.getErrorMessage() );
        }
        if ( dataValidationErrors.hasErrors() )
        {
            throw new ContentDataValidationException( dataValidationErrors.getFirst().getErrorMessage() );
        }
    }

    private void addAttachments( final Client client, final ContentId contentId, final Collection<Attachment> attachments )
    {
        for ( Attachment attachment : attachments )
        {
            client.execute( Commands.attachment().create().contentSelector( contentId ).attachment( attachment ) );
        }
    }

    private void addThumbnail( final Client client, final ContentId contentId, final Attachment attachment )
        throws Exception
    {
        final Binary thumbnailBinary = createImageThumbnail( attachment.getBinary(), THUMBNAIL_SIZE );
        final Attachment thumbnailAttachment = newAttachment( attachment ).
            binary( thumbnailBinary ).
            name( CreateContent.THUMBNAIL_NAME ).
            mimeType( THUMBNAIL_MIME_TYPE ).
            build();
        client.execute( Commands.attachment().create().contentSelector( contentId ).attachment( thumbnailAttachment ) );
    }

    public Binary createImageThumbnail( final Binary binary, final int size )
        throws Exception
    {
        final BufferedImage image = ImageIO.read( binary.asInputStream() );
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BufferedImage scaledImage = new ScaleMaxFilter( size ).filter( image );
        ImageIO.write( scaledImage, "png", outputStream );
        return Binary.from( outputStream.toByteArray() );
    }

    @Inject
    public void setContentDao( final ContentDao contentDao )
    {
        this.contentDao = contentDao;
    }

    @Inject
    public void setContentTypeDao( final ContentTypeDao contentTypeDao )
    {
        this.contentTypeDao = contentTypeDao;
    }

    @Inject
    public void setRelationshipService( final RelationshipService relationshipService )
    {
        this.relationshipService = relationshipService;
    }

    @Inject
    public void setIndexService( final IndexService indexService )
    {
        this.indexService = indexService;
    }
}
