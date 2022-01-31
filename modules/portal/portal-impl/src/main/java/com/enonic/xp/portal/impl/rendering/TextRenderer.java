package com.enonic.xp.portal.impl.rendering;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;

import com.enonic.xp.core.internal.HtmlHelper;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.RenderMode;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.portal.url.ProcessHtmlParams;
import com.enonic.xp.region.TextComponent;
import com.enonic.xp.region.TextComponentType;

import static com.enonic.xp.portal.impl.rendering.RenderingConstants.PORTAL_COMPONENT_ATTRIBUTE;

@Component(immediate = true, service = Renderer.class)
public final class TextRenderer
    implements Renderer<TextComponent>
{

    PortalUrlService service;

    private static final String EMPTY_COMPONENT_EDIT_MODE_HTML =
        "<div " + PORTAL_COMPONENT_ATTRIBUTE + "=\"{0}\"><section></section></div>";

    private static final String EMPTY_COMPONENT_PREVIEW_MODE_HTML = "<section " + PORTAL_COMPONENT_ATTRIBUTE + "=\"{0}\"></section>";

    private static final String COMPONENT_EDIT_MODE_HTML = "<div " + PORTAL_COMPONENT_ATTRIBUTE + "=\"{0}\"><section>{1}</section></div>";

    private static final String COMPONENT_PREVIEW_MODE_HTML = "<section " + PORTAL_COMPONENT_ATTRIBUTE + "=\"{0}\">{1}</section>";

    private static final Pattern EMPTY_FIGCAPTION_PATTERN = Pattern.compile( "<figcaption.*?></figcaption>" );

    @Override
    public Class<TextComponent> getType()
    {
        return TextComponent.class;
    }

    @Override
    public PortalResponse render( final TextComponent textComponent, final PortalRequest portalRequest )
    {
        final RenderMode renderMode = getRenderingMode( portalRequest );
        final PortalResponse.Builder portalResponseBuilder = PortalResponse.create();

        portalResponseBuilder.contentType( MediaType.create( "text", "html" ) ).postProcess( false );

        final String text = textComponent.getText();
        if ( text.isEmpty() )
        {
            renderEmptyTextComponent( textComponent, portalRequest, portalResponseBuilder );
        }
        else
        {
            if ( text.startsWith( "[" ) ) // TODO need proper way to identify AST
            {
                final String template = renderMode == RenderMode.EDIT ? COMPONENT_EDIT_MODE_HTML : COMPONENT_PREVIEW_MODE_HTML;

                final String renderedHtml = astRender( text );
                portalResponseBuilder.body( MessageFormat.format( template, TextComponentType.INSTANCE, renderedHtml ) );
                return portalResponseBuilder.build();
            }

            switch ( renderMode )
            {
                case EDIT:
                    portalResponseBuilder.body( MessageFormat.format( COMPONENT_EDIT_MODE_HTML, TextComponentType.INSTANCE, text ) );
                    break;

                case LIVE:
                case PREVIEW:
                case INLINE:
                default:
                    ProcessHtmlParams params = new ProcessHtmlParams().portalRequest( portalRequest ).value( text );
                    final String processedHtml = removeEmptyFigCaptionTags( service.processHtml( params ) );
                    portalResponseBuilder.body(
                        MessageFormat.format( COMPONENT_PREVIEW_MODE_HTML, TextComponentType.INSTANCE, processedHtml ) );
                    break;
            }
        }

        return portalResponseBuilder.build();
    }

    private void renderEmptyTextComponent( final TextComponent textComponent, final PortalRequest portalRequest,
                                           final PortalResponse.Builder portalResponseBuilder )
    {
        final RenderMode renderMode = getRenderingMode( portalRequest );
        switch ( renderMode )
        {
            case EDIT:
                portalResponseBuilder.body( MessageFormat.format( EMPTY_COMPONENT_EDIT_MODE_HTML, textComponent.getType().toString() ) );
                break;

            case PREVIEW:
            case INLINE:
                portalResponseBuilder.body( MessageFormat.format( EMPTY_COMPONENT_PREVIEW_MODE_HTML, textComponent.getType().toString() ) );
                break;

            case LIVE:
                portalResponseBuilder.body( "" );
                break;
        }
    }

    private RenderMode getRenderingMode( final PortalRequest portalRequest )
    {
        return portalRequest == null ? RenderMode.LIVE : portalRequest.getMode();
    }

    private String astRender( String value )
    {
        StringBuilder sb = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode;
        try
        {
            jsonNode = mapper.readTree( value );
        }
        catch ( JsonProcessingException e )
        {
            throw new IllegalStateException( e );
        }

        astChildrenRender( jsonNode, sb );
        return sb.toString();
    }

    void astChildrenRender( JsonNode arrayNode, StringBuilder sb )
    {
        if ( !arrayNode.isArray() )
        {
            throw new IllegalStateException( "Expecting an array" );
        }
        for ( JsonNode element : arrayNode )
        {
            if ( element.hasNonNull( "type" ) )
            {
                switch ( element.get( "type" ).asText() )
                {
                    case "paragraph":
                        sb.append( "<p>" );
                        astChildrenRender( element.get( "children" ), sb );
                        sb.append( "</p>" );
                        break;
                    case "quote":
                        sb.append( "<blockquote>" );
                        astChildrenRender( element.get( "children" ), sb );
                        sb.append( "</blockquote>" );
                        break;
                    case "link":
                        sb.append( "<a " ).append( HtmlHelper.escapedHtmlAttribute( "href", element.get( "url" ).asText() ) ).append( ">" );
                        astChildrenRender( element.get( "children" ), sb );
                        sb.append( "</a>" );
                        break;
                    case "image":
                        sb.append( "<img " ).append( HtmlHelper.escapedHtmlAttribute( "src", element.get( "url" ).asText() ) ).append( ">" );
                        astChildrenRender( element.get( "children" ), sb );
                        sb.append( "</img>" );
                        break;
                }
            }
            else if ( element.hasNonNull( "text" ) )
            {
                sb.append( HtmlHelper.escapeHtml( element.get( "text" ).asText() ) );
            }
        }
    }

    private String removeEmptyFigCaptionTags( final String text )
    {
        final Matcher matcher = EMPTY_FIGCAPTION_PATTERN.matcher( text );

        return matcher.replaceAll( "" );
    }

    @Reference
    public void setPortalUrlService( final PortalUrlService service )
    {
        this.service = service;
    }
}
