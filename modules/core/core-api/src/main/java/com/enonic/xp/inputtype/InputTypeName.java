package com.enonic.xp.inputtype;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.schema.BaseSchemaName;

@PublicApi
public final class InputTypeName
    extends BaseSchemaName
{
    public static final InputTypeName CHECK_BOX = InputTypeName.from( ApplicationKey.BASE, "CheckBox" );

    public static final InputTypeName COMBO_BOX = InputTypeName.from( ApplicationKey.BASE, "ComboBox" );

    public static final InputTypeName CONTENT_SELECTOR = InputTypeName.from( ApplicationKey.BASE, "ContentSelector" );

    public static final InputTypeName CUSTOM_SELECTOR = InputTypeName.from( ApplicationKey.BASE, "CustomSelector" );

    public static final InputTypeName CONTENT_TYPE_FILTER = InputTypeName.from( ApplicationKey.BASE, "ContentTypeFilter" );

    public static final InputTypeName DATE = InputTypeName.from( ApplicationKey.BASE, "Date" );

    public static final InputTypeName DATE_TIME = InputTypeName.from( ApplicationKey.BASE, "DateTime" );

    public static final InputTypeName DOUBLE = InputTypeName.from( ApplicationKey.BASE, "Double" );

    public static final InputTypeName MEDIA_UPLOADER = InputTypeName.from( ApplicationKey.BASE, "MediaUploader" );

    public static final InputTypeName ATTACHMENT_UPLOADER = InputTypeName.from( ApplicationKey.BASE, "AttachmentUploader" );

    public static final InputTypeName GEO_POINT = InputTypeName.from( ApplicationKey.BASE, "GeoPoint" );

    public static final InputTypeName HTML_AREA = InputTypeName.from( ApplicationKey.BASE, "HtmlArea" );

    public static final InputTypeName IMAGE_SELECTOR = InputTypeName.from( ApplicationKey.BASE, "ImageSelector" );

    public static final InputTypeName MEDIA_SELECTOR = InputTypeName.from( ApplicationKey.BASE, "MediaSelector" );

    public static final InputTypeName IMAGE_UPLOADER = InputTypeName.from( ApplicationKey.BASE, "ImageUploader" );

    public static final InputTypeName LONG = InputTypeName.from( ApplicationKey.BASE, "Long" );

    public static final InputTypeName RADIO_BUTTON = InputTypeName.from( ApplicationKey.BASE, "RadioButton" );

    public static final InputTypeName SITE_CONFIGURATOR = InputTypeName.from( ApplicationKey.BASE, "SiteConfigurator" );

    public static final InputTypeName TAG = InputTypeName.from( ApplicationKey.BASE, "Tag" );

    public static final InputTypeName TEXT_AREA = InputTypeName.from( ApplicationKey.BASE, "TextArea" );

    public static final InputTypeName TEXT_LINE = InputTypeName.from( ApplicationKey.BASE, "TextLine" );

    public static final InputTypeName TIME = InputTypeName.from( ApplicationKey.BASE, "Time" );

    private InputTypeName( final String name )
    {
        super( name );
    }

    private InputTypeName( final ApplicationKey applicationKey, final String localName )
    {
        super( applicationKey, localName );
    }

    public static InputTypeName from( final String name )
    {
        if ( !name.contains( ":" ) ) //TODO: weird stuff
        {
            return new InputTypeName( "base:" + name );
        }
        return new InputTypeName( name );
    }

    public static InputTypeName from( final ApplicationKey applicationKey, final String name )
    {
        return new InputTypeName( applicationKey, name );
    }
}
