package com.enonic.xp.portal.impl.handler.render;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.net.MediaType;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentNotFoundException;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.Page;
import com.enonic.xp.page.PageRegions;
import com.enonic.xp.page.PageTemplateKey;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.region.FragmentComponent;
import com.enonic.xp.region.PartComponent;
import com.enonic.xp.region.Region;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.site.Site;
import com.enonic.xp.web.HttpMethod;
import com.enonic.xp.web.HttpStatus;
import com.enonic.xp.web.WebException;
import com.enonic.xp.web.WebResponse;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentHandlerTest
    extends RenderBaseHandlerTest
{
    private ComponentHandler handler;

    @BeforeEach
    public final void setup()
        throws Exception
    {
        this.handler =
            new ComponentHandler( this.contentService, this.rendererDelegate, this.pageDescriptorService, this.pageTemplateService,
                                  this.postProcessor );

        this.request.setMethod( HttpMethod.GET );
        this.request.setContentPath( ContentPath.from( "/site/somepath/content" ) );
        this.request.setEndpointPath( "/_/component/main" );
    }

    @Test
    public void testOrder()
    {
        assertEquals( 0, this.handler.getOrder() );
    }

    @Test
    public void testMatch()
    {
        this.request.setEndpointPath( null );
        assertFalse( this.handler.canHandle( this.request ) );

        this.request.setEndpointPath( "/_/other/main/1" );
        assertFalse( this.handler.canHandle( this.request ) );

        this.request.setEndpointPath( "/component/main/1" );
        assertFalse( this.handler.canHandle( this.request ) );

        this.request.setEndpointPath( "/_/component/main/1" );
        assertTrue( this.handler.canHandle( this.request ) );
    }

    @Test
    public void testOptions()
        throws Exception
    {
        setupContentAndSite();
        setupTemplates();

        final PortalResponse portalResponse = PortalResponse.create().status( HttpStatus.METHOD_NOT_ALLOWED ).build();
        setRendererResult( portalResponse );

        Mockito.when( this.postProcessor.processResponseInstructions( Mockito.any(), Mockito.any() ) ).thenReturn( portalResponse );

        this.request.setMethod( HttpMethod.OPTIONS );
        this.request.setEndpointPath( "/_/component/main-region/0" );

        final WebResponse res = this.handler.handle( this.request, PortalResponse.create().build(), null );
        assertNotNull( res );
        assertEquals( HttpStatus.OK, res.getStatus() );
        assertEquals( "GET,POST,HEAD,OPTIONS,PUT,DELETE,TRACE", res.getHeaders().get( "Allow" ) );
    }

    @Test
    public void testComponentFound()
        throws Exception
    {
        setupContentAndSite();
        setupTemplates();

        final PortalResponse portalResponse =
            PortalResponse.create().body( "component rendered" ).header( "some-header", "some-value" ).status( HttpStatus.OK ).build();

        Mockito.when( this.postProcessor.processResponseInstructions( Mockito.any(), Mockito.any() ) ).thenReturn( portalResponse );

        setRendererResult( portalResponse );

        this.request.setEndpointPath( "/_/component/main-region/0" );

        final WebResponse res = this.handler.handle( this.request, PortalResponse.create().build(), null );
        assertNotNull( res );
        assertEquals( HttpStatus.OK, res.getStatus() );
        assertEquals( MediaType.PLAIN_TEXT_UTF_8, res.getContentType() );
        assertEquals( "some-value", res.getHeaders().get( "some-header" ) );
        assertEquals( "component rendered", res.getBody() );
    }

    @Test
    public void getComponentPageNotFound()
        throws Exception
    {
        setupNonPageContent();

        this.request.setEndpointPath( "/_/component/main-region/0" );

        final WebException e =
            assertThrows( WebException.class, () -> this.handler.handle( this.request, PortalResponse.create().build(), null ) );
        assertAll( () -> assertEquals( HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus() ),
                   () -> assertEquals( "No template found for content", e.getMessage() ));
    }

    @Test
    public void getComponentNotFound()
        throws Exception
    {
        setupContentAndSite();
        setupTemplates();

        this.request.setEndpointPath( "/_/component/main-region/666" );

        final WebException e =
            assertThrows( WebException.class, () -> this.handler.handle( this.request, PortalResponse.create().build(), null ) );
        assertEquals( HttpStatus.NOT_FOUND, e.getStatus() );
        assertEquals( "Page component for [/main-region/666] not found", e.getMessage() );
    }

    @Test
    public void getContentNotFound()
    {
        this.request.setEndpointPath( "/_/component/main-region/666" );

        final ContentPath path = ContentPath.from( "/site/somepath/content" );
        Mockito.when( this.contentService.getByPath( path ) ).thenThrow( new ContentNotFoundException( path, Branch.from( "draft" ) ) );
        this.request.setContentPath( path );

        final WebException e =
            assertThrows( WebException.class, () -> this.handler.handle( this.request, PortalResponse.create().build(), null ) );
        assertEquals( HttpStatus.NOT_FOUND, e.getStatus() );
        assertEquals( "Page [/site/somepath/content] not found", e.getMessage() );
    }

    @Test
    public void getSiteNotFound()
    {
        setupContent();
        this.request.setEndpointPath( "/_/component/main-region/666" );

        final ContentPath path = ContentPath.from( "/site/somepath/content" );
        Mockito.when( this.contentService.findNearestSiteByPath( path ) ).thenReturn( null );

        this.request.setContentPath( path );

        final WebException e =
            assertThrows( WebException.class, () -> this.handler.handle( this.request, PortalResponse.create().build(), null ) );
        assertEquals( HttpStatus.NOT_FOUND, e.getStatus() );
        assertEquals( "Site for [/site/somepath/content] not found", e.getMessage() );
    }


    @Test
    public void testComponentFragment()
        throws Exception
    {
        setupSite();
        setupContentFragment();
        setupTemplates();

        final PortalResponse portalResponse =
            PortalResponse.create().body( "component rendered" ).header( "some-header", "some-value" ).status( HttpStatus.OK ).build();

        Mockito.when( this.postProcessor.processResponseInstructions( Mockito.any(), Mockito.any() ) ).thenReturn( portalResponse );

        setRendererResult( portalResponse );

        this.request.setEndpointPath( "/_/component/main-region/0" );

        final WebResponse res = this.handler.handle( this.request, PortalResponse.create().build(), null );
        assertNotNull( res );
        assertEquals( HttpStatus.OK, res.getStatus() );
        assertEquals( MediaType.PLAIN_TEXT_UTF_8, res.getContentType() );
        assertEquals( "some-value", res.getHeaders().get( "some-header" ) );
        assertEquals( "component rendered", res.getBody() );
    }

    private void setupSite()
    {
        final Site site = createSite( "id", "site", "myapplication:contenttypename" );
        Mockito.when( this.contentService.getNearestSite( Mockito.isA( ContentId.class ) ) ).thenReturn( site );
        Mockito.when( this.contentService.findNearestSiteByPath( Mockito.isA( ContentPath.class ) ) ).thenReturn( site );
    }

    private void setupContentFragment()
    {
        final Content content = createPageWithFragment( "id", "site/somepath/content", "myapplication:ctype", true );
        final Content fragment = createFragmentContent();

        Mockito.when( this.contentService.getByPath( ContentPath.from( "site/somepath/content" ).asAbsolute() ) ).thenReturn( content );

        Mockito.when( this.contentService.getById( content.getId() ) ).thenReturn( content );

        Mockito.when( this.contentService.getByPath( fragment.getPath() ) ).thenReturn( fragment );

        Mockito.when( this.contentService.getById( fragment.getId() ) ).thenReturn( fragment );

    }

    private Content createPageWithFragment( final String id, final String path, final String contentTypeName, final boolean withPage )
    {
        PropertyTree rootDataSet = new PropertyTree();
        rootDataSet.addString( "property1", "value1" );

        final Content.Builder content = Content.create()
            .id( ContentId.from( id ) )
            .path( ContentPath.from( path ) )
            .owner( PrincipalKey.from( "user:myStore:me" ) )
            .displayName( "My Content" )
            .modifier( PrincipalKey.from( "user:system:admin" ) )
            .type( ContentTypeName.from( contentTypeName ) );

        if ( withPage )
        {
            PageRegions pageRegions = PageRegions.create()
                .add( Region.create()
                          .name( "main-region" )
                          .add( FragmentComponent.create().fragment( ContentId.from( "fragmentId" ) ).build() )
                          .build() )
                .build();

            Page page = Page.create().template( PageTemplateKey.from( "my-page" ) ).regions( pageRegions ).config( rootDataSet ).build();
            content.page( page );
        }
        return content.build();
    }

    private Content createFragmentContent()
    {
        PropertyTree rootDataSet = new PropertyTree();

        final Content.Builder content = Content.create()
            .id( ContentId.from( "fragmentId" ) )
            .path( ContentPath.from( "site/somepath/fragment" ) )
            .owner( PrincipalKey.from( "user:myStore:me" ) )
            .displayName( "My Content" )
            .modifier( PrincipalKey.from( "user:system:admin" ) )
            .type( ContentTypeName.fragment() );

        Page page = Page.create()
            .template( PageTemplateKey.from( "my-page" ) )
            .fragment( PartComponent.create().descriptor( "myapp:mypart" ).build() )
            .config( rootDataSet )
            .build();
        content.page( page );
        return content.build();
    }

    private Site createSite( final String id, final String path, final String contentTypeName )
    {
        PropertyTree rootDataSet = new PropertyTree();
        rootDataSet.addString( "property1", "value1" );

        Page page = Page.create().template( PageTemplateKey.from( "my-page" ) ).config( rootDataSet ).build();

        return Site.create()
            .id( ContentId.from( id ) )
            .path( ContentPath.from( path ) )
            .owner( PrincipalKey.from( "user:myStore:me" ) )
            .displayName( "My Content" )
            .modifier( PrincipalKey.from( "user:system:admin" ) )
            .type( ContentTypeName.from( contentTypeName ) )
            .page( page )
            .build();
    }
}
