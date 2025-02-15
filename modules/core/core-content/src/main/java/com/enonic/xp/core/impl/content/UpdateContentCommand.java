package com.enonic.xp.core.impl.content;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import com.enonic.xp.content.AttachmentValidationError;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentAccessException;
import com.enonic.xp.content.ContentDataValidationException;
import com.enonic.xp.content.ContentEditor;
import com.enonic.xp.content.ContentInheritType;
import com.enonic.xp.content.ContentPublishInfo;
import com.enonic.xp.content.EditableContent;
import com.enonic.xp.content.EditableSite;
import com.enonic.xp.content.Media;
import com.enonic.xp.content.UpdateContentParams;
import com.enonic.xp.content.UpdateContentTranslatorParams;
import com.enonic.xp.content.ValidationErrors;
import com.enonic.xp.content.processor.ContentProcessor;
import com.enonic.xp.content.processor.ProcessUpdateParams;
import com.enonic.xp.content.processor.ProcessUpdateResult;
import com.enonic.xp.core.impl.content.serializer.ContentDataSerializer;
import com.enonic.xp.core.impl.content.validate.InputValidator;
import com.enonic.xp.icon.Thumbnail;
import com.enonic.xp.inputtype.InputTypes;
import com.enonic.xp.media.MediaInfo;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeAccessException;
import com.enonic.xp.node.NodeCommitEntry;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.schema.content.ContentType;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.schema.content.GetContentTypeParams;
import com.enonic.xp.site.Site;
import com.enonic.xp.util.BinaryReferences;

final class UpdateContentCommand
    extends AbstractCreatingOrUpdatingContentCommand
{
    private static final Logger LOG = LoggerFactory.getLogger( UpdateContentCommand.class );

    private final UpdateContentParams params;

    private final MediaInfo mediaInfo;

    private final PageDescriptorService pageDescriptorService;

    private final PartDescriptorService partDescriptorService;

    private final LayoutDescriptorService layoutDescriptorService;

    private final ContentDataSerializer contentDataSerializer;

    private UpdateContentCommand( final Builder builder )
    {
        super( builder );
        this.params = builder.params;
        this.mediaInfo = builder.mediaInfo;
        this.pageDescriptorService = builder.pageDescriptorService;
        this.partDescriptorService = builder.partDescriptorService;
        this.layoutDescriptorService = builder.layoutDescriptorService;
        this.contentDataSerializer = builder.contentDataSerializer;
    }

    public static Builder create( final UpdateContentParams params )
    {
        return new Builder( params );
    }

    public static Builder create( final AbstractCreatingOrUpdatingContentCommand source )
    {
        return new Builder( source );
    }

    Content execute()
    {
        params.validate();
        validateCreateAttachments( params.getCreateAttachments() );

        try
        {
            return doExecute();
        }
        catch ( NodeAccessException e )
        {
            throw new ContentAccessException( e );
        }
    }

    private Content doExecute()
    {
        final Content contentBeforeChange = getContent( params.getContentId() );

        Content editedContent = editContent( params.getEditor(), contentBeforeChange );

        if ( params.stopInherit() )
        {
            if ( editedContent.getInherit().contains( ContentInheritType.CONTENT ) )
            {
                nodeService.commit( NodeCommitEntry.create().message( "Base inherited version" ).build(),
                                    NodeIds.from( params.getContentId().toString() ) );
                editedContent.getInherit().remove( ContentInheritType.CONTENT );
            }
            editedContent.getInherit().remove( ContentInheritType.NAME );
        }

        final BinaryReferences removeAttachments = Objects.requireNonNullElseGet( params.getRemoveAttachments(), BinaryReferences::empty );

        if ( contentBeforeChange.equals( editedContent ) && params.getCreateAttachments() == null && removeAttachments.isEmpty() &&
            !this.params.isClearAttachments() )
        {
            return contentBeforeChange;
        }

        editedContent = processContent( contentBeforeChange, editedContent );

        validateBlockingChecks( editedContent );

        final ValidationErrors.Builder validationErrorsBuilder = ValidationErrors.create();

        if ( !params.isClearAttachments() && contentBeforeChange.getValidationErrors() != null )
        {
            contentBeforeChange.getValidationErrors()
                .stream()
                .filter( validationError -> validationError instanceof AttachmentValidationError )
                .map( validationError -> (AttachmentValidationError) validationError )
                .filter( validationError -> !removeAttachments.contains( validationError.getAttachment() ) )
                .forEach( validationErrorsBuilder::add );
        }

        final ValidationErrors validationErrors = ValidateContentDataCommand.create()
            .contentId( editedContent.getId() )
            .data( editedContent.getData() )
            .extraDatas( editedContent.getAllExtraData() )
            .contentTypeName( editedContent.getType() )
            .contentName( editedContent.getName() )
            .displayName( editedContent.getDisplayName() )
            .createAttachments( params.getCreateAttachments() )
            .contentValidators( this.contentValidators )
            .contentTypeService( this.contentTypeService )
            .validationErrorsBuilder( validationErrorsBuilder )
            .build()
            .execute();

        if ( params.isRequireValid() )
        {
            validationErrors.stream().findFirst().ifPresent( validationError -> {
                throw new ContentDataValidationException( validationError.getMessage() );
            } );
        }

        editedContent = Content.create( editedContent ).valid( !validationErrors.hasErrors() ).validationErrors( validationErrors ).build();
        editedContent = attachThumbnail( editedContent );
        editedContent = setModifiedTime( editedContent );

        final UpdateContentTranslatorParams updateContentTranslatorParams = UpdateContentTranslatorParams.create()
            .editedContent( editedContent )
            .createAttachments( this.params.getCreateAttachments() )
            .removeAttachments( this.params.getRemoveAttachments() )
            .clearAttachments( this.params.isClearAttachments() )
            .modifier( getCurrentUser().getKey() )
            .build();

        final UpdateNodeParams updateNodeParams = UpdateNodeParamsFactory.create( updateContentTranslatorParams )
            .contentTypeService( this.contentTypeService )
            .xDataService( this.xDataService )
            .pageDescriptorService( this.pageDescriptorService )
            .partDescriptorService( this.partDescriptorService )
            .layoutDescriptorService( this.layoutDescriptorService )
            .contentDataSerializer( this.contentDataSerializer )
            .siteService( this.siteService )
            .build()
            .produce();

        final Node editedNode = this.nodeService.update( updateNodeParams );
        return translator.fromNode( editedNode, true );
    }

    private Content processContent( final Content originalContent, Content editedContent )
    {
        final ContentType contentType = this.contentTypeService.getByName( GetContentTypeParams.from( editedContent.getType() ) );

        editedContent = runContentProcessors( originalContent, editedContent, contentType );

        return editedContent;
    }

    private Content runContentProcessors( final Content originalContent, Content editedContent, final ContentType contentType )
    {
        for ( final ContentProcessor contentProcessor : this.contentProcessors )
        {
            if ( contentProcessor.supports( contentType ) )
            {
                final ProcessUpdateResult result = contentProcessor.processUpdate( ProcessUpdateParams.create()
                                                                                       .contentType( contentType )
                                                                                       .mediaInfo( mediaInfo )
                                                                                       .createAttachments( params.getCreateAttachments() )
                                                                                       .originalContent( originalContent )
                                                                                       .editedContent( editedContent )
                                                                                       .modifier( getCurrentUser() )
                                                                                       .build() );

                editedContent = updateContentWithProcessedData( editedContent, result );
            }
        }

        return editedContent;
    }

    private Content updateContentWithProcessedData( Content editedContent, final ProcessUpdateResult processUpdateResult )
    {
        if ( processUpdateResult != null )
        {
            if ( processUpdateResult.getEditor() != null )
            {
                editedContent = editContent( processUpdateResult.getEditor(), editedContent );
            }
            this.params.createAttachments( processUpdateResult.getCreateAttachments() );
        }
        return editedContent;
    }

    private Content attachThumbnail( final Content editedContent )
    {
        if ( !editedContent.hasThumbnail() )
        {
            final Thumbnail mediaThumbnail = resolveMediaThumbnail( editedContent );
            if ( mediaThumbnail != null )
            {
                return Content.create( editedContent ).thumbnail( mediaThumbnail ).build();
            }
        }
        return editedContent;
    }

    private Content editContent( final ContentEditor editor, final Content original )
    {
        final EditableContent editableContent = original.isSite() ? new EditableSite( (Site) original ) : new EditableContent( original );
        if ( editor != null )
        {
            editor.edit( editableContent );
        }
        return editableContent.build();
    }

    private Content setModifiedTime( final Content content )
    {
        return Content.create( content ).modifiedTime( Instant.now() ).build();
    }

    private void validateBlockingChecks( final Content editedContent )
    {
        validatePropertyTree( editedContent );

        final ContentPublishInfo publishInfo = editedContent.getPublishInfo();
        if ( publishInfo != null )
        {
            final Instant publishToInstant = publishInfo.getTo();
            if ( publishToInstant != null )
            {
                final Instant publishFromInstant = publishInfo.getFrom();
                Preconditions.checkArgument( publishFromInstant != null, "'Publish from' must be set if 'Publish from' is set." );
                Preconditions.checkArgument( publishToInstant.compareTo( publishFromInstant ) >= 0,
                                             "'Publish to' must be set after 'Publish from'." );
            }
        }

        if ( editedContent.getType().isImageMedia() )
        {
            validateImageMediaProperties( editedContent );
        }
    }

    private void validateImageMediaProperties( final Content editedContent )
    {
        if ( !( editedContent instanceof Media ) )
        {
            return;
        }
        final Media mediaContent = (Media) editedContent;

        try
        {
            // validate focal point values
            mediaContent.getFocalPoint();
        }
        catch ( IllegalArgumentException e )
        {
            throw new IllegalArgumentException( "Invalid property for content: " + e.getMessage(), e );
        }
    }

    private void validatePropertyTree( final Content editedContent )
    {
        final ContentType contentType =
            contentTypeService.getByName( new GetContentTypeParams().contentTypeName( editedContent.getType() ) );
        try
        {
            InputValidator.create()
                .form( contentType.getForm() )
                .inputTypeResolver( InputTypes.BUILTIN )
                .build()
                .validate( editedContent.getData() );
        }
        catch ( final Exception e )
        {
            throw new IllegalArgumentException( "Invalid property for content: " + editedContent.getPath(), e );
        }
    }

    private Thumbnail resolveMediaThumbnail( final Content content )
    {
        final ContentType contentType = getContentType( content.getType() );

        if ( contentType.getSuperType() == null )
        {
            return null;
        }

        return null;
    }

    private ContentType getContentType( final ContentTypeName contentTypeName )
    {
        return contentTypeService.getByName( new GetContentTypeParams().contentTypeName( contentTypeName ) );
    }

    public static class Builder
        extends AbstractCreatingOrUpdatingContentCommand.Builder<Builder>
    {
        private UpdateContentParams params;

        private MediaInfo mediaInfo;

        private PageDescriptorService pageDescriptorService;

        private PartDescriptorService partDescriptorService;

        private LayoutDescriptorService layoutDescriptorService;

        private ContentDataSerializer contentDataSerializer;

        Builder( final UpdateContentParams params )
        {
            this.params = params;
        }

        Builder( final AbstractCreatingOrUpdatingContentCommand source )
        {
            super( source );
        }

        Builder params( final UpdateContentParams value )
        {
            this.params = value;
            return this;
        }

        Builder mediaInfo( final MediaInfo value )
        {
            this.mediaInfo = value;
            return this;
        }

        Builder pageDescriptorService( final PageDescriptorService value )
        {
            this.pageDescriptorService = value;
            return this;
        }

        Builder partDescriptorService( final PartDescriptorService value )
        {
            this.partDescriptorService = value;
            return this;
        }

        Builder layoutDescriptorService( final LayoutDescriptorService value )
        {
            this.layoutDescriptorService = value;
            return this;
        }

        Builder contentDataSerializer( final ContentDataSerializer value )
        {
            this.contentDataSerializer = value;
            return this;
        }

        @Override
        void validate()
        {
            super.validate();
            Preconditions.checkNotNull( params );
        }

        public UpdateContentCommand build()
        {
            validate();
            return new UpdateContentCommand( this );
        }

    }

}
