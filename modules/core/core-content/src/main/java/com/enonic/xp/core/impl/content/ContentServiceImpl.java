package com.enonic.xp.core.impl.content;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.ByteSource;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.archive.ArchiveContentParams;
import com.enonic.xp.archive.ArchiveContentsResult;
import com.enonic.xp.archive.RestoreContentParams;
import com.enonic.xp.archive.RestoreContentsResult;
import com.enonic.xp.branch.Branches;
import com.enonic.xp.content.ActiveContentVersionEntry;
import com.enonic.xp.content.ApplyContentPermissionsParams;
import com.enonic.xp.content.ApplyContentPermissionsResult;
import com.enonic.xp.content.CompareContentParams;
import com.enonic.xp.content.CompareContentResult;
import com.enonic.xp.content.CompareContentResults;
import com.enonic.xp.content.CompareContentsParams;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentAccessException;
import com.enonic.xp.content.ContentDependencies;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentIds;
import com.enonic.xp.content.ContentNotFoundException;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentPaths;
import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.content.ContentPublishInfo;
import com.enonic.xp.content.ContentQuery;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.content.ContentValidator;
import com.enonic.xp.content.ContentValidityParams;
import com.enonic.xp.content.ContentValidityResult;
import com.enonic.xp.content.ContentVersion;
import com.enonic.xp.content.ContentVersionId;
import com.enonic.xp.content.Contents;
import com.enonic.xp.content.CreateContentParams;
import com.enonic.xp.content.CreateMediaParams;
import com.enonic.xp.content.DeleteContentParams;
import com.enonic.xp.content.DeleteContentsResult;
import com.enonic.xp.content.DuplicateContentParams;
import com.enonic.xp.content.DuplicateContentsResult;
import com.enonic.xp.content.FindContentByParentParams;
import com.enonic.xp.content.FindContentByParentResult;
import com.enonic.xp.content.FindContentByQueryParams;
import com.enonic.xp.content.FindContentByQueryResult;
import com.enonic.xp.content.FindContentIdsByParentResult;
import com.enonic.xp.content.FindContentIdsByQueryResult;
import com.enonic.xp.content.FindContentPathsByQueryParams;
import com.enonic.xp.content.FindContentPathsByQueryResult;
import com.enonic.xp.content.FindContentVersionsParams;
import com.enonic.xp.content.FindContentVersionsResult;
import com.enonic.xp.content.GetActiveContentVersionParams;
import com.enonic.xp.content.GetActiveContentVersionsParams;
import com.enonic.xp.content.GetActiveContentVersionsResult;
import com.enonic.xp.content.GetContentByIdsParams;
import com.enonic.xp.content.GetPublishStatusResult;
import com.enonic.xp.content.GetPublishStatusesParams;
import com.enonic.xp.content.GetPublishStatusesResult;
import com.enonic.xp.content.HasUnpublishedChildrenParams;
import com.enonic.xp.content.ImportContentParams;
import com.enonic.xp.content.ImportContentResult;
import com.enonic.xp.content.MoveContentParams;
import com.enonic.xp.content.MoveContentsResult;
import com.enonic.xp.content.PublishContentResult;
import com.enonic.xp.content.PublishStatus;
import com.enonic.xp.content.PushContentParams;
import com.enonic.xp.content.RenameContentParams;
import com.enonic.xp.content.ReorderChildContentsParams;
import com.enonic.xp.content.ReorderChildContentsResult;
import com.enonic.xp.content.ReorderChildParams;
import com.enonic.xp.content.ReprocessContentParams;
import com.enonic.xp.content.ResolvePublishDependenciesParams;
import com.enonic.xp.content.ResolveRequiredDependenciesParams;
import com.enonic.xp.content.SetActiveContentVersionResult;
import com.enonic.xp.content.SetContentChildOrderParams;
import com.enonic.xp.content.UndoPendingDeleteContentParams;
import com.enonic.xp.content.UnpublishContentParams;
import com.enonic.xp.content.UnpublishContentsResult;
import com.enonic.xp.content.UpdateContentParams;
import com.enonic.xp.content.UpdateMediaParams;
import com.enonic.xp.content.processor.ContentProcessor;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.core.impl.content.serializer.ContentDataSerializer;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.event.EventPublisher;
import com.enonic.xp.form.FormDefaultValuesProcessor;
import com.enonic.xp.media.MediaInfoService;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeAccessException;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.node.NodeVersionId;
import com.enonic.xp.node.RefreshMode;
import com.enonic.xp.node.ReorderChildNodeParams;
import com.enonic.xp.node.ReorderChildNodesParams;
import com.enonic.xp.node.ReorderChildNodesResult;
import com.enonic.xp.node.SetNodeChildOrderParams;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.project.ProjectService;
import com.enonic.xp.query.parser.QueryParser;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.schema.content.ContentTypeService;
import com.enonic.xp.schema.xdata.XDataService;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.site.CreateSiteParams;
import com.enonic.xp.site.Site;
import com.enonic.xp.site.SiteConfigsDataSerializer;
import com.enonic.xp.site.SiteService;
import com.enonic.xp.trace.Trace;
import com.enonic.xp.trace.Tracer;
import com.enonic.xp.util.BinaryReference;

@Component(configurationPid = "com.enonic.xp.content")
public class ContentServiceImpl
    implements ContentService
{
    public static final String TEMPLATES_FOLDER_NAME = "_templates";

    private static final Logger LOG = LoggerFactory.getLogger( ContentServiceImpl.class );

    private static final String TEMPLATES_FOLDER_DISPLAY_NAME = "Templates";

    private static final SiteConfigsDataSerializer SITE_CONFIGS_DATA_SERIALIZER = new SiteConfigsDataSerializer();

    private ContentTypeService contentTypeService;

    private final NodeService nodeService;

    private EventPublisher eventPublisher;

    private MediaInfoService mediaInfoService;

    private XDataService xDataService;

    private SiteService siteService;

    private final ContentNodeTranslator translator;

    private final List<ContentProcessor> contentProcessors = new CopyOnWriteArrayList<>();

    private final List<ContentValidator> contentValidators = new CopyOnWriteArrayList<>();

    private FormDefaultValuesProcessor formDefaultValuesProcessor;

    private final PageDescriptorService pageDescriptorService;

    private final PartDescriptorService partDescriptorService;

    private final LayoutDescriptorService layoutDescriptorService;

    private final ContentDataSerializer contentDataSerializer;

    private ContentAuditLogSupport contentAuditLogSupport;

    private volatile ContentConfig config;

    @Activate
    public ContentServiceImpl( @Reference final NodeService nodeService, @Reference final PageDescriptorService pageDescriptorService,
                               @Reference final PartDescriptorService partDescriptorService,
                               @Reference final LayoutDescriptorService layoutDescriptorService )
    {
        this.nodeService = nodeService;
        this.pageDescriptorService = pageDescriptorService;
        this.partDescriptorService = partDescriptorService;
        this.layoutDescriptorService = layoutDescriptorService;

        this.contentDataSerializer = ContentDataSerializer.create().
            layoutDescriptorService( layoutDescriptorService ).
            pageDescriptorService( pageDescriptorService ).
            partDescriptorService( partDescriptorService ).
            build();

        this.translator = new ContentNodeTranslator( nodeService, contentDataSerializer );
    }

    @Activate
    @Modified
    public void initialize( final ContentConfig config )
    {
        this.config = config;
    }

    @Override
    public Site create( final CreateSiteParams params )
    {

        final PropertyTree data = new PropertyTree();
        data.setString( "description", params.getDescription() );

        SITE_CONFIGS_DATA_SERIALIZER.toProperties( params.getSiteConfigs(), data.getRoot() );

        final CreateContentParams createContentParams = CreateContentParams.create().
            type( ContentTypeName.site() ).
            parent( params.getParentContentPath() ).
            name( params.getName() ).
            displayName( params.getDisplayName() ).
            contentData( data ).
            requireValid( params.isRequireValid() ).
            build();

        final Site site = (Site) CreateContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            siteService( this.siteService ).
            xDataService( this.xDataService ).
            contentProcessors( this.contentProcessors ).
            contentValidators( this.contentValidators ).
            formDefaultValuesProcessor( this.formDefaultValuesProcessor ).
            pageDescriptorService( this.pageDescriptorService ).
            partDescriptorService( this.partDescriptorService ).
            layoutDescriptorService( this.layoutDescriptorService ).
            contentDataSerializer( this.contentDataSerializer ).
            allowUnsafeAttachmentNames( config.attachments_allowUnsafeNames() ).
            params( createContentParams ).
            build().
            execute();

        this.create( CreateContentParams.create().
            owner( site.getOwner() ).
            displayName( TEMPLATES_FOLDER_DISPLAY_NAME ).
            name( TEMPLATES_FOLDER_NAME ).
            inheritPermissions( true ).
            parent( site.getPath() ).
            type( ContentTypeName.templateFolder() ).
            requireValid( true ).
            contentData( new PropertyTree() ).
            build() );

        contentAuditLogSupport.createSite( params, site );

        return site;
    }

    @Override
    public Content create( final CreateContentParams params )
    {

        final Content content = CreateContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            siteService( this.siteService ).
            xDataService( this.xDataService ).
            contentProcessors( this.contentProcessors ).
            contentValidators( this.contentValidators ).
            formDefaultValuesProcessor( this.formDefaultValuesProcessor ).
            pageDescriptorService( this.pageDescriptorService ).
            partDescriptorService( this.partDescriptorService ).
            layoutDescriptorService( this.layoutDescriptorService ).
            contentDataSerializer( this.contentDataSerializer ).
            allowUnsafeAttachmentNames( config.attachments_allowUnsafeNames() ).
            params( params ).
            build().
            execute();

        if ( content instanceof Site )
        {
            this.create( CreateContentParams.create().
                owner( content.getOwner() ).
                displayName( TEMPLATES_FOLDER_DISPLAY_NAME ).
                name( TEMPLATES_FOLDER_NAME ).
                inheritPermissions( true ).
                parent( content.getPath() ).
                type( ContentTypeName.templateFolder() ).
                requireValid( true ).
                contentData( new PropertyTree() ).
                build() );

            return this.doGetById( content.getId() );
        }

        contentAuditLogSupport.createContent( params, content );

        return content;
    }

    @Override
    public Content create( final CreateMediaParams params )
    {
        final Content content = CreateMediaCommand.create().
            params( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            mediaInfoService( this.mediaInfoService ).
            siteService( this.siteService ).
            xDataService( this.xDataService ).
            contentProcessors( this.contentProcessors ).
            contentValidators( this.contentValidators ).
            formDefaultValuesProcessor( this.formDefaultValuesProcessor ).
            pageDescriptorService( this.pageDescriptorService ).
            partDescriptorService( this.partDescriptorService ).
            layoutDescriptorService( this.layoutDescriptorService ).
            contentDataSerializer( this.contentDataSerializer ).
            allowUnsafeAttachmentNames( config.attachments_allowUnsafeNames() ).
            build().
            execute();

        contentAuditLogSupport.createMedia( params, content );

        return content;
    }

    @Override
    public Content update( final UpdateContentParams params )
    {
        final Content content = UpdateContentCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            siteService( this.siteService ).
            xDataService( this.xDataService ).
            contentProcessors( this.contentProcessors ).
            contentValidators( this.contentValidators ).
            pageDescriptorService( this.pageDescriptorService ).
            partDescriptorService( this.partDescriptorService ).
            layoutDescriptorService( this.layoutDescriptorService ).
            contentDataSerializer( this.contentDataSerializer ).
            allowUnsafeAttachmentNames( config.attachments_allowUnsafeNames() ).
            build().
            execute();

        contentAuditLogSupport.update( params, content );

        return content;
    }

    @Override
    public Content update( final UpdateMediaParams params )
    {
        final Content content = UpdateMediaCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            mediaInfoService( this.mediaInfoService ).
            pageDescriptorService( this.pageDescriptorService ).
            partDescriptorService( this.partDescriptorService ).
            layoutDescriptorService( this.layoutDescriptorService ).
            siteService( this.siteService ).
            xDataService( this.xDataService ).
            contentProcessors( this.contentProcessors ).
            contentValidators( this.contentValidators ).
            contentDataSerializer( this.contentDataSerializer ).
            allowUnsafeAttachmentNames( config.attachments_allowUnsafeNames() ).
            build().
            execute();

        contentAuditLogSupport.update( params, content );

        return content;
    }

    @Override
    public DeleteContentsResult deleteWithoutFetch( final DeleteContentParams params )
    {
        final DeleteContentsResult result = DeleteContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            params( params ).
            build().
            execute();

        contentAuditLogSupport.delete( params, result );

        return result;
    }

    @Override
    public int undoPendingDelete( final UndoPendingDeleteContentParams params )
    {
        final Contents affectedContents = UndoPendingDeleteContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            params( params ).
            build().
            execute();

        contentAuditLogSupport.undoPendingDelete( params, affectedContents );

        return affectedContents.getSize();
    }

    @Override
    public PublishContentResult publish( final PushContentParams params )
    {
        final PublishContentResult result = PublishContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            contentIds( params.getContentIds() ).
            excludedContentIds( params.getExcludedContentIds() ).
            target( params.getTarget() ).
            contentPublishInfo( params.getContentPublishInfo() ).
            excludeChildrenIds( getExcludeChildrenIds( params ) ).
            includeDependencies( params.isIncludeDependencies() ).
            pushListener( params.getPublishContentListener() ).
            deleteListener( params.getDeleteContentListener() ).
            message( params.getMessage() ).
            build().
            execute();

        contentAuditLogSupport.publish( params, result );

        return result;
    }

    private ContentIds getExcludeChildrenIds( final PushContentParams params )
    {
        if ( params.getExcludeChildrenIds().isNotEmpty() )
        {
            return params.getExcludeChildrenIds();
        }
        if ( params.isIncludeChildren() )
        {
            return ContentIds.empty();
        }
        return params.getContentIds();
    }

    @Override
    public CompareContentResults resolvePublishDependencies( ResolvePublishDependenciesParams params )
    {
        return ResolveContentsToBePublishedCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            contentIds( params.getContentIds() ).
            excludedContentIds( params.getExcludedContentIds() ).
            excludeChildrenIds( params.getExcludeChildrenIds() ).
            target( params.getTarget() ).
            build().
            execute();
    }

    @Override
    public ContentIds resolveRequiredDependencies( ResolveRequiredDependenciesParams params )
    {
        return ResolveRequiredDependenciesCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            contentIds( params.getContentIds() ).
            target( params.getTarget() ).
            build().
            execute();
    }

    @Override
    public boolean isValidContent( ContentIds contentIds )
    {
        return getInvalidContent( contentIds ).isEmpty();
    }

    @Override
    public ContentIds getInvalidContent( ContentIds contentIds )
    {
        ContentValidityParams params = ContentValidityParams.create().contentIds( contentIds ).build();
        return getContentValidity( params ).getNotValidContentIds();
    }

    @Override
    public ContentValidityResult getContentValidity( final ContentValidityParams params )
    {
        return CheckContentValidityCommand.create().
            translator( this.translator ).
            nodeService( this.nodeService ).
            eventPublisher( this.eventPublisher ).
            contentTypeService( this.contentTypeService ).
            contentIds( params.getContentIds() ).
            build().
            execute();
    }

    @Override
    public UnpublishContentsResult unpublishContent( final UnpublishContentParams params )
    {
        final UnpublishContentsResult result = UnpublishContentCommand.create().
            params( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();

        contentAuditLogSupport.unpublishContent( params, result );

        return result;
    }

    @Override
    public Content getById( final ContentId contentId )
    {
        final Trace trace = Tracer.newTrace( "content.getById" );
        if ( trace == null )
        {
            return doGetById( contentId );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "id", contentId );
            final Content content = doGetById( contentId );
            trace.put( "path", content.getPath() );
            return content;
        } );
    }

    private Content doGetById( final ContentId contentId )
    {
        final Content content = executeGetById( contentId );
        if ( content == null )
        {
            throw new ContentNotFoundException( contentId, ContextAccessor.current().getBranch() );
        }
        return content;
    }

    private Content executeGetById( final ContentId contentId )
    {
        return GetContentByIdCommand.create( contentId )
            .nodeService( this.nodeService )
            .contentTypeService( this.contentTypeService )
            .translator( this.translator )
            .eventPublisher( this.eventPublisher )
            .build()
            .execute();
    }

    @Override
    public Site getNearestSite( final ContentId contentId )
    {
        final Trace trace = Tracer.newTrace( "content.getNearestSite" );
        if ( trace == null )
        {
            return doGetNearestSite( contentId );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "id", contentId );
            final Site site = doGetNearestSite( contentId );
            if ( site != null )
            {
                trace.put( "path", site.getPath() );
            }
            return site;
        } );
    }

    private Site doGetNearestSite( final ContentId contentId )
    {
        return GetNearestSiteCommand.create().
                contentId( contentId ).
                nodeService( this.nodeService ).
                contentTypeService( this.contentTypeService ).
                translator( this.translator ).
                eventPublisher( this.eventPublisher ).
                build().
                execute();
    }

    @Override
    public Site findNearestSiteByPath( final ContentPath contentPath )
    {
        final Trace trace = Tracer.newTrace( "content.findNearestSiteByPath" );
        if ( trace == null )
        {
            return (Site) doFindNearestByPath( contentPath, Content::isSite );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "contentPath", contentPath );
            final Site site = (Site) doFindNearestByPath( contentPath, Content::isSite );
            if ( site != null )
            {
                trace.put( "path", site.getPath() );
            }
            return site;
        } );
    }

    private Content doFindNearestByPath( final ContentPath contentPath, final Predicate<Content> predicate )
    {
        final Content content = executeGetByPath( contentPath );
        if ( content != null && predicate.test( content ) )
        {
            return content;
        }

        //Resolves the closest content, starting from the root.
        Content foundContent = null;
        ContentPath nextContentPath = ContentPath.ROOT;
        for ( int contentPathIndex = 0; contentPathIndex < contentPath.elementCount(); contentPathIndex++ )
        {
            final ContentPath currentContentPath = ContentPath.from( nextContentPath, contentPath.getElement( contentPathIndex ) );

            final Content childContent = executeGetByPath( currentContentPath );
            if ( childContent == null )
            {
                break;
            }
            if ( predicate.test( childContent ) )
            {
                foundContent = childContent;
            }
            nextContentPath = currentContentPath;
        }

        return foundContent;
    }

    @Override
    public Contents getByIds( final GetContentByIdsParams params )
    {
        final Trace trace = Tracer.newTrace( "content.getByIds" );
        if ( trace == null )
        {
            return doGetByIds( params );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "id", params.getIds() );
            return doGetByIds( params );
        } );
    }

    private Contents doGetByIds( final GetContentByIdsParams params )
    {
        return GetContentByIdsCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public Content getByPath( final ContentPath path )
    {
        final Trace trace = Tracer.newTrace( "content.getByPath" );
        if ( trace == null )
        {
            return doGetByPath( path );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "path", path );
            final Content content = doGetByPath( path );
            trace.put( "id", content.getId() );
            return content;
        } );
    }

    private Content doGetByPath( final ContentPath path )
    {
        final Content content = executeGetByPath( path );
        if ( content == null )
        {
            throw new ContentNotFoundException( path, ContextAccessor.current().getBranch() );
        }
        return content;
    }

    private Content executeGetByPath( final ContentPath path )
    {
        return GetContentByPathCommand.create( path )
            .nodeService( this.nodeService )
            .contentTypeService( this.contentTypeService )
            .translator( this.translator )
            .eventPublisher( this.eventPublisher )
            .build()
            .execute();
    }

    @Override
    public AccessControlList getPermissionsById( ContentId contentId )
    {
        Content content = doGetById( contentId );
        if ( content.getPermissions() != null )
        {
            return content.getPermissions();
        }
        return AccessControlList.empty();
    }

    @Override
    public Contents getByPaths( final ContentPaths paths )
    {
        final Trace trace = Tracer.newTrace( "content.getByPaths" );
        if ( trace == null )
        {
            return doGetByPaths( paths );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "path", paths );
            return doGetByPaths( paths );
        } );
    }

    private Contents doGetByPaths( final ContentPaths paths )
    {
        return GetContentByPathsCommand.create( paths ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public FindContentByParentResult findByParent( final FindContentByParentParams params )
    {
        final Trace trace = Tracer.newTrace( "content.findByParent" );
        if ( trace == null )
        {
            return doFindByParent( params );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "query", params.getParentPath() != null ? params.getParentPath() : params.getParentId() );
            trace.put( "from", params.getFrom() );
            trace.put( "size", params.getSize() );
            final FindContentByParentResult result = doFindByParent( params );
            trace.put( "hits", result.getTotalHits() );
            return result;
        } );
    }

    private FindContentByParentResult doFindByParent( final FindContentByParentParams params )
    {
        return FindContentByParentCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public FindContentIdsByParentResult findIdsByParent( final FindContentByParentParams params )
    {
        return FindContentIdsByParentCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public DuplicateContentsResult duplicate( final DuplicateContentParams params )
    {
        final DuplicateContentsResult result = DuplicateContentCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            duplicateListener( params.getDuplicateContentListener() ).
            build().
            execute();

        contentAuditLogSupport.duplicate( params, result );

        return result;
    }

    @Override
    public MoveContentsResult move( final MoveContentParams params )
    {
        final MoveContentsResult result = MoveContentCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            moveListener( params.getMoveContentListener() ).
            build().
            execute();

        contentAuditLogSupport.move( params, result );

        return result;
    }

    @Override
    public ArchiveContentsResult archive( final ArchiveContentParams params )
    {
        final ArchiveContentsResult result = ArchiveContentCommand.create( params )
            .nodeService( nodeService )
            .translator( translator )
            .eventPublisher( eventPublisher )
            .contentTypeService( contentTypeService )
            .archiveListener( params.getArchiveContentListener() )
            .build()
            .execute();

        contentAuditLogSupport.archive( params, result );

        return result;
    }

    @Override
    public RestoreContentsResult restore( final RestoreContentParams params )
    {
        final RestoreContentsResult result = RestoreContentCommand.create( params )
            .nodeService( nodeService )
            .translator( translator )
            .eventPublisher( eventPublisher )
            .contentTypeService( contentTypeService )
            .restoreListener( params.getRestoreContentListener() )
            .build()
            .execute();

        contentAuditLogSupport.restore( params, result );

        return result;
    }

    @Override
    public Content rename( final RenameContentParams params )
    {
        final Content content = RenameContentCommand.create( params ).
            nodeService( this.nodeService ).
            xDataService( this.xDataService ).
            siteService( this.siteService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            contentProcessors( this.contentProcessors ).
            contentValidators( this.contentValidators ).
            pageDescriptorService( this.pageDescriptorService ).
            partDescriptorService( this.partDescriptorService ).
            layoutDescriptorService( this.layoutDescriptorService ).
            contentDataSerializer( this.contentDataSerializer ).
            build().
            execute();

        contentAuditLogSupport.rename( params, content );

        return content;
    }

    @Override
    public FindContentByQueryResult find( final FindContentByQueryParams params )
    {
        return FindContentByQueryCommand.create().
            params( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public FindContentIdsByQueryResult find( final ContentQuery query )
    {
        final Trace trace = Tracer.newTrace( "content.find" );
        if ( trace == null )
        {
            return doFind( query );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "query", query.getQueryExpr() != null ? query.getQueryExpr().toString() : "" );
            trace.put( "from", query.getFrom() );
            trace.put( "size", query.getSize() );
            final FindContentIdsByQueryResult result = doFind( query );
            trace.put( "hits", result.getTotalHits() );
            return result;
        } );
    }

    private FindContentIdsByQueryResult doFind( final ContentQuery query )
    {
        return FindContentIdsByQueryCommand.create().
            query( query ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public Contents findByApplicationKey( final ApplicationKey key )
    {
        final ContentQuery query = ContentQuery.create().
            queryExpr( QueryParser.parse(
                String.join( ".", ContentPropertyNames.DATA, ContentPropertyNames.SITECONFIG, ContentPropertyNames.APPLICATION_KEY ) + "=" +
                    "'" + key + "'" ) ).
            size( -1 ).
            build();

        return this.getByIds( new GetContentByIdsParams( this.find( query ).getContentIds() ) );

    }

    @Override
    public ContentPaths findContentPaths( ContentQuery query )
    {
        return FindContentPathsByQueryCommand.create().
            params( new FindContentPathsByQueryParams( query ) ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute().
            getContentPaths();
    }

    @Override
    public FindContentPathsByQueryResult findPaths( ContentQuery query )
    {
        return FindContentPathsByQueryCommand.create().
            params( new FindContentPathsByQueryParams( query ) ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public CompareContentResult compare( final CompareContentParams params )
    {
        return CompareContentCommand.create().
            nodeService( this.nodeService ).
            contentId( params.getContentId() ).
            target( params.getTarget() ).
            build().
            execute();
    }

    @Override
    public CompareContentResults compare( final CompareContentsParams params )
    {
        return CompareContentsCommand.create().
            nodeService( this.nodeService ).
            contentIds( params.getContentIds() ).
            target( params.getTarget() ).
            build().
            execute();
    }

    @Override
    public GetPublishStatusesResult getPublishStatuses( final GetPublishStatusesParams params )
    {
        final GetContentByIdsParams getContentByIdsParams = new GetContentByIdsParams( params.getContentIds() );
        final Instant now = Instant.now();

        final Contents contents = ContextBuilder.from( ContextAccessor.current() ).
            branch( params.getTarget() ).
            attribute( "ignorePublishTimes", Boolean.TRUE ).
            build().
            callWith( () -> this.getByIds( getContentByIdsParams ) );

        final GetPublishStatusesResult.Builder getPublishStatusesResult = GetPublishStatusesResult.create();

        contents.stream().
            map( content -> {

                final ContentPublishInfo publishInfo = content.getPublishInfo();
                if ( publishInfo != null )
                {
                    if ( publishInfo.getTo() != null && publishInfo.getTo().compareTo( now ) < 0 )
                    {
                        return new GetPublishStatusResult( content.getId(), PublishStatus.EXPIRED );
                    }

                    if ( publishInfo.getFrom() != null && publishInfo.getFrom().compareTo( now ) > 0 )
                    {
                        return new GetPublishStatusResult( content.getId(), PublishStatus.PENDING );
                    }
                }
                return new GetPublishStatusResult( content.getId(), PublishStatus.ONLINE );
            } ).
            forEach( getPublishStatusesResult::add );

        return getPublishStatusesResult.build();
    }

    @Override
    public FindContentVersionsResult getVersions( final FindContentVersionsParams params )
    {
        return FindContentVersionsCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            contentId( params.getContentId() ).
            from( params.getFrom() ).
            size( params.getSize() ).
            build().
            execute();
    }

    @Override
    public GetActiveContentVersionsResult getActiveVersions( final GetActiveContentVersionsParams params )
    {
        return GetActiveContentVersionsCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            contentId( params.getContentId() ).
            branches( params.getBranches() ).
            build().
            execute();
    }

    @Override
    public ContentVersion getActiveVersion( final GetActiveContentVersionParams params )
    {
        final GetActiveContentVersionsResult activeVersions = getActiveVersions( GetActiveContentVersionsParams.create().
            branches( Branches.from( params.getBranch() ) ).
            contentId( params.getContentId() ).
            build() );

        final UnmodifiableIterator<ActiveContentVersionEntry> activeVersionIterator = activeVersions.getActiveContentVersions().iterator();
        return activeVersionIterator.hasNext() ? activeVersionIterator.next().getContentVersion() : null;
    }

    @Override
    public SetActiveContentVersionResult setActiveContentVersion( final ContentId contentId, final ContentVersionId versionId )
    {
        nodeService.setActiveVersion( NodeId.from( contentId.toString() ), NodeVersionId.from( versionId.toString() ) );

        contentAuditLogSupport.setActiveContentVersion( contentId, versionId );

        return new SetActiveContentVersionResult( contentId, versionId );
    }

    @Override
    public Content setChildOrder( final SetContentChildOrderParams params )
    {
        try
        {
            final SetNodeChildOrderParams.Builder builder = SetNodeChildOrderParams.create().
                nodeId( NodeId.from( params.getContentId() ) ).
                childOrder( params.getChildOrder() );

            if ( params.stopInherit() )
            {
                builder.processor( new SetContentChildOrderProcessor() );
            }

            final Node node = nodeService.setChildOrder( builder.build() );

            final Content content = translator.fromNode( node, true );

            contentAuditLogSupport.setChildOrder( params, content );

            return content;
        }
        catch ( NodeAccessException e )
        {
            throw new ContentAccessException( e );
        }
    }

    @Override
    public ReorderChildContentsResult reorderChildren( final ReorderChildContentsParams params )
    {

        final ReorderChildNodesParams.Builder builder = ReorderChildNodesParams.create();

        for ( final ReorderChildParams param : params )
        {
            builder.add( ReorderChildNodeParams.create().
                nodeId( NodeId.from( param.getContentToMove() ) ).
                moveBefore( param.getContentToMoveBefore() == null ? null : NodeId.from( param.getContentToMoveBefore() ) ).
                build() );
        }

        if ( params.stopInherit() )
        {
            builder.processor( new SetContentChildOrderProcessor() );
        }

        final ReorderChildNodesResult reorderChildNodesResult = this.nodeService.reorderChildren( builder.build() );

        this.nodeService.refresh( RefreshMode.SEARCH );

        final ReorderChildContentsResult result = new ReorderChildContentsResult( reorderChildNodesResult.getSize() );

        contentAuditLogSupport.reorderChildren( params, result );

        return result;
    }

    @Override
    public Boolean hasUnpublishedChildren( final HasUnpublishedChildrenParams params )
    {
        return nodeService.hasUnpublishedChildren( NodeId.from( params.getContentId() ), params.getTarget() );
    }

    @Override
    public ApplyContentPermissionsResult applyPermissions( final ApplyContentPermissionsParams params )
    {
        final ApplyContentPermissionsResult result = ApplyContentPermissionsCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();

        contentAuditLogSupport.applyPermissions( params, result );

        return result;
    }

    @Override
    public AccessControlList getRootPermissions()
    {
        final ContentPath rootContentPath = ContentPath.ROOT;
        final NodePath rootNodePath = ContentNodeHelper.translateContentPathToNodePath( rootContentPath );
        final Node rootNode = nodeService.getByPath( rootNodePath );
        return rootNode != null ? rootNode.getPermissions() : AccessControlList.empty();
    }

    @Override
    public ContentDependencies getDependencies( final ContentId id )
    {
        final ContentDependenciesResolver contentDependenciesResolver = new ContentDependenciesResolver( this );

        return contentDependenciesResolver.resolve( id );
    }

    @Override
    public ContentIds getOutboundDependencies( final ContentId id )
    {
        final ContentOutboundDependenciesIdsResolver contentOutboundDependenciesIdsResolver =
            new ContentOutboundDependenciesIdsResolver( this, contentDataSerializer );

        return contentOutboundDependenciesIdsResolver.resolve( id );
    }

    @Override
    public boolean contentExists( final ContentId contentId )
    {
        return ContentExistsCommand.create( contentId ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public boolean contentExists( final ContentPath contentPath )
    {
        return ContentExistsCommand.create( contentPath ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public ByteSource getBinary( final ContentId contentId, final BinaryReference binaryReference )
    {
        return GetBinaryCommand.create( contentId, binaryReference ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public ByteSource getBinary( final ContentId contentId, final ContentVersionId contentVersionId, final BinaryReference binaryReference )
    {
        return GetBinaryByVersionCommand.create( contentId, contentVersionId, binaryReference ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public String getBinaryKey( final ContentId contentId, final BinaryReference binaryReference )
    {
        return GetBinaryKeyCommand.create( contentId, binaryReference ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Override
    public Content reprocess( final ContentId contentId )
    {
        final Content content = ReprocessContentCommand.create( ReprocessContentParams.create().contentId( contentId ).build() ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            mediaInfoService( this.mediaInfoService ).
            pageDescriptorService( this.pageDescriptorService ).
            partDescriptorService( this.partDescriptorService ).
            layoutDescriptorService( this.layoutDescriptorService ).
            contentDataSerializer( this.contentDataSerializer ).
            siteService( this.siteService ).
            xDataService( this.xDataService ).
            contentProcessors( this.contentProcessors ).
            contentValidators( this.contentValidators ).
            build().
            execute();

        contentAuditLogSupport.reprocess( content );

        return content;
    }

    @Override
    public Content getByIdAndVersionId( final ContentId contentId, final ContentVersionId versionId )
    {
        final Trace trace = Tracer.newTrace( "content.getByIdAndVersionId" );
        if ( trace == null )
        {
            return doGetByIdAndVersionId( contentId, versionId );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "contentId", contentId );
            trace.put( "versionId", versionId );
            final Content content = doGetByIdAndVersionId( contentId, versionId );
            if ( content != null )
            {
                trace.put( "path", content.getPath() );
            }
            return content;
        } );
    }

    @Override
    public Content getByPathAndVersionId( final ContentPath contentPath, final ContentVersionId versionId )
    {
        final Trace trace = Tracer.newTrace( "content.getByPathAndVersionId" );
        if ( trace == null )
        {
            return doGetByPathAndVersionId( contentPath, versionId );
        }

        return Tracer.trace( trace, () -> {
            trace.put( "path", contentPath );
            trace.put( "versionId", versionId );
            final Content content = doGetByPathAndVersionId( contentPath, versionId );
            if ( content != null )
            {
                trace.put( "contentId", content.getId() );
            }
            return content;
        } );
    }

    @Override
    public ImportContentResult importContent( final ImportContentParams params )
    {
        return ImportContentCommand.create().
            params( params ).
            nodeService( nodeService ).
            contentTypeService( contentTypeService ).
            contentDataSerializer( contentDataSerializer ).
            eventPublisher( eventPublisher ).
            translator( translator ).
            build().
            execute();
    }

    @Override
    @Deprecated
    public InputStream getBinaryInputStream( final ContentId contentId, final BinaryReference binaryReference )
    {
        try
        {
            return nodeService.getBinary( NodeId.from( contentId.toString() ), binaryReference ).openStream();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    private Content doGetByIdAndVersionId( final ContentId contentId, final ContentVersionId versionId )
    {
        return GetContentByIdAndVersionIdCommand.create().
            contentId( contentId ).
            versionId( versionId ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    private Content doGetByPathAndVersionId( final ContentPath contentPath, final ContentVersionId versionId )
    {
        return GetContentByPathAndVersionIdCommand.create().
            contentPath( contentPath ).
            versionId( versionId ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            translator( this.translator ).
            eventPublisher( this.eventPublisher ).
            build().
            execute();
    }

    @Reference
    public void setContentTypeService( final ContentTypeService contentTypeService )
    {
        this.contentTypeService = contentTypeService;
    }

    @Reference
    public void setEventPublisher( final EventPublisher eventPublisher )
    {
        this.eventPublisher = eventPublisher;
    }

    @Reference
    public void setMediaInfoService( final MediaInfoService mediaInfoService )
    {
        this.mediaInfoService = mediaInfoService;
    }

    @Reference
    public void setxDataService( final XDataService xDataService )
    {
        this.xDataService = xDataService;
    }

    @Reference
    public void setSiteService( final SiteService siteService )
    {
        this.siteService = siteService;
    }

    @SuppressWarnings("unused")
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addContentProcessor( final ContentProcessor contentProcessor )
    {
        this.contentProcessors.add( contentProcessor );
    }

    public void removeContentProcessor( final ContentProcessor contentProcessor )
    {
        this.contentProcessors.remove( contentProcessor );
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addContentValidator( final ContentValidator contentValidator )
    {
        this.contentValidators.add( contentValidator );
    }

    public void removeContentValidator( final ContentValidator contentValidator )
    {
        this.contentValidators.remove( contentValidator );
    }

    @Reference
    public void setFormDefaultValuesProcessor( final FormDefaultValuesProcessor formDefaultValuesProcessor )
    {
        this.formDefaultValuesProcessor = formDefaultValuesProcessor;
    }

    @Reference
    public void setContentAuditLogSupport( final ContentAuditLogSupport contentAuditLogSupport )
    {
        this.contentAuditLogSupport = contentAuditLogSupport;
    }

    @Reference
    public void setProjectService( final ProjectService projectService )
    {
        //Many starters depend on ContentService available only when default cms repo is fully initialized.
        // Starting from 7.3 Initialization happens in ProjectService, so we need a dependency.
    }

}
