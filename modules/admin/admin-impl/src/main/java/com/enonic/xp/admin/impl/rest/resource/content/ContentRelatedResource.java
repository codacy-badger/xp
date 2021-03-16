package com.enonic.xp.admin.impl.rest.resource.content;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.admin.impl.json.content.page.PageDescriptorJson;
import com.enonic.xp.admin.impl.json.content.page.PageDescriptorListJson;
import com.enonic.xp.admin.impl.json.content.page.region.LayoutDescriptorJson;
import com.enonic.xp.admin.impl.json.content.page.region.LayoutDescriptorsJson;
import com.enonic.xp.admin.impl.json.content.page.region.PartDescriptorJson;
import com.enonic.xp.admin.impl.json.content.page.region.PartDescriptorsJson;
import com.enonic.xp.admin.impl.json.schema.content.ContentTypeSummaryJson;
import com.enonic.xp.admin.impl.json.schema.content.ContentTypeSummaryListJson;
import com.enonic.xp.admin.impl.rest.resource.schema.content.ContentTypeIconResolver;
import com.enonic.xp.admin.impl.rest.resource.schema.content.ContentTypeIconUrlResolver;
import com.enonic.xp.admin.impl.rest.resource.schema.content.LocaleMessageResolver;
import com.enonic.xp.admin.impl.rest.resource.schema.mixin.InlineMixinResolver;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationKeys;
import com.enonic.xp.app.ApplicationWildcardResolver;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.inputtype.InputTypeProperty;
import com.enonic.xp.jaxrs.JaxRsComponent;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.region.ComponentDescriptor;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.schema.content.ContentType;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.schema.content.ContentTypeService;
import com.enonic.xp.schema.content.GetContentTypeParams;
import com.enonic.xp.schema.mixin.MixinService;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.site.Site;
import com.enonic.xp.site.SiteConfigs;
import com.enonic.xp.support.AbstractImmutableEntityList;

import static com.enonic.xp.admin.impl.rest.resource.ResourceConstants.CMS_PATH;
import static com.enonic.xp.admin.impl.rest.resource.ResourceConstants.REST_ROOT;

@Path(REST_ROOT + "{content:(" + CMS_PATH + "/schema)}/filter")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({RoleKeys.ADMIN_LOGIN_ID, RoleKeys.ADMIN_ID})
@Component(immediate = true, property = "group=admin")
public class ContentRelatedResource
    implements JaxRsComponent
{
    private ContentTypeService contentTypeService;

    private ContentService contentService;

    private ContentTypeIconUrlResolver contentTypeIconUrlResolver;

    private LocaleService localeService;

    private LayoutDescriptorService layoutDescriptorService;

    private PartDescriptorService partDescriptorService;

    private PageDescriptorService pageDescriptorService;

    private MixinService mixinService;

    @GET
    @Path("contentTypes")
    public ContentTypeSummaryListJson contentTypes( @QueryParam("contentId") final String contentId )
    {
        final Content content;
        if ( contentId != null )
        {
            content = this.contentService.getById( ContentId.from( contentId ) );
        }
        else
        {
            content = this.contentService.getByPath( ContentPath.ROOT );
        }

        final Stream<ContentType> stream;
        if ( content.getPath().equals( ContentPath.ROOT ) )
        {
            stream = Stream.of( ContentTypeName.folder(), ContentTypeName.site(), ContentTypeName.shortcut() ).map(
                GetContentTypeParams::from ).map( contentTypeService::getByName );
        }
        else if ( content.getType().isTemplateFolder() )
        {
            stream = Stream.of( contentTypeService.getByName( GetContentTypeParams.from( ContentTypeName.pageTemplate() ) ) );
        }
        else
        {
            final ContentType contentType = this.contentTypeService.getByName( GetContentTypeParams.from( content.getType() ) );
            if ( !contentType.allowChildContent() )
            {
                return new ContentTypeSummaryListJson( List.of() );
            }

            final ApplicationKeys siteApps = getNearestSiteApps( content.getId() );

            stream = Stream.concat(
                Stream.concat( Stream.of( ContentTypeName.folder(), ContentTypeName.site(), ContentTypeName.shortcut() ),
                               content.getType().isSite() ? Stream.of( ContentTypeName.templateFolder() ) : Stream.empty() ),
                contentTypeService.getNames( siteApps ).stream() ).filter( allowChildContentTypeFilter( contentType ) ).map(
                GetContentTypeParams::from ).map( contentTypeService::getByName ).filter( Objects::nonNull ).filter(
                Predicate.not( ContentType::isAbstract ) ).filter(
                type -> type.getSchemaConfig().getValue( "allowNewContent", Boolean.class, Boolean.TRUE ) );
        }

        return new ContentTypeSummaryListJson( stream.map( type -> new ContentTypeSummaryJson( type, this.contentTypeIconUrlResolver,
                                                                                               new LocaleMessageResolver( localeService,
                                                                                                                          type.getName().getApplicationKey() ) ) ).collect(
            Collectors.toUnmodifiableList() ) );
    }

    @GET
    @Path("layouts")
    public LayoutDescriptorsJson layouts( @QueryParam("contentId") final String contentId )
    {
        return new LayoutDescriptorsJson( filteredComponentsStream( contentId, this.layoutDescriptorService::getByApplications ).map(
            descriptor -> new LayoutDescriptorJson( descriptor, new LocaleMessageResolver( localeService, descriptor.getApplicationKey() ),
                                                    new InlineMixinResolver( mixinService ) ) ).collect(
            Collectors.toUnmodifiableList() ) );
    }

    @GET
    @Path("parts")
    public PartDescriptorsJson parts( @QueryParam("contentId") final String contentId )
    {
        return new PartDescriptorsJson( filteredComponentsStream( contentId, this.partDescriptorService::getByApplications ).map(
            descriptor -> new PartDescriptorJson( descriptor, new LocaleMessageResolver( localeService, descriptor.getApplicationKey() ),
                                                  new InlineMixinResolver( mixinService ) ) ).collect( Collectors.toUnmodifiableList() ) );
    }

    @GET
    @Path("pages")
    public PageDescriptorListJson pages( @QueryParam("contentId") final String contentId )
    {
        return new PageDescriptorListJson( filteredComponentsStream( contentId, this.pageDescriptorService::getByApplications ).map(
            descriptor -> new PageDescriptorJson( descriptor, new LocaleMessageResolver( localeService, descriptor.getApplicationKey() ),
                                                  new InlineMixinResolver( mixinService ) ) ).collect( Collectors.toUnmodifiableList() ) );
    }

    private <T extends ComponentDescriptor> Stream<T> filteredComponentsStream( final String contentId,
                                                                                final Function<ApplicationKeys, AbstractImmutableEntityList<T>> supplier )
    {
        final Content content = this.contentService.getById( ContentId.from( contentId ) );

        final ContentType contentType = this.contentTypeService.getByName( GetContentTypeParams.from( content.getType() ) );

        final ApplicationKeys siteApps = getNearestSiteApps( content.getId() );

        return supplier.apply( siteApps ).stream().filter(
            descriptor -> allowOnContentTypeFilter( descriptor ).test( contentType.getName() ) );
    }

    private static Predicate<ContentTypeName> allowChildContentTypeFilter( final ContentType contentType )
    {
        final List<String> allowChildContentType =
            readConfigValues( contentType.getSchemaConfig().getProperties( "allowChildContentType" ) );

        return allowContentTypeFilter( contentType.getName().getApplicationKey(), allowChildContentType );
    }

    private static Predicate<ContentTypeName> allowOnContentTypeFilter( final ComponentDescriptor descriptor )
    {
        final List<String> allowOnContentType = readConfigValues( descriptor.getSchemaConfig().getProperties( "allowOnContentType" ) );

        return allowContentTypeFilter( descriptor.getKey().getApplicationKey(), allowOnContentType );
    }

    private static Predicate<ContentTypeName> allowContentTypeFilter( final ApplicationKey applicationKey, final List<String> wildcards )
    {
        return wildcards.isEmpty()
            ? x -> true
            : ApplicationWildcardResolver.predicate( applicationKey, wildcards, ContentTypeName::toString );
    }

    private static List<String> readConfigValues( final Set<InputTypeProperty> config )
    {
        return config.stream().map( InputTypeProperty::getValue ).collect( Collectors.toList() );
    }

    private ApplicationKeys getNearestSiteApps( final ContentId contentId )
    {
        return Optional.ofNullable( contentService.getNearestSite( contentId ) ).map( Site::getSiteConfigs ).map(
            SiteConfigs::getApplicationKeys ).map( ApplicationKeys::from ).orElse( ApplicationKeys.empty() );
    }

    @Reference
    public void setLocaleService( final LocaleService localeService )
    {
        this.localeService = localeService;
    }

    @Reference
    public void setMixinService( final MixinService mixinService )
    {
        this.mixinService = mixinService;
    }

    @Reference
    public void setContentTypeService( final ContentTypeService contentTypeService )
    {
        this.contentTypeService = contentTypeService;
        this.contentTypeIconUrlResolver = new ContentTypeIconUrlResolver( new ContentTypeIconResolver( contentTypeService ) );
    }

    @Reference
    public void setLayoutDescriptorService( final LayoutDescriptorService layoutDescriptorService )
    {
        this.layoutDescriptorService = layoutDescriptorService;
    }

    @Reference
    public void setPartDescriptorService( final PartDescriptorService partDescriptorService )
    {
        this.partDescriptorService = partDescriptorService;
    }

    @Reference
    public void setContentService( final ContentService contentService )
    {
        this.contentService = contentService;
    }

    @Reference
    public void setPageDescriptorService( final PageDescriptorService pageDescriptorService )
    {
        this.pageDescriptorService = pageDescriptorService;
    }
}
