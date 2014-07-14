package com.enonic.wem.api.xml.serializer;

import com.enonic.wem.api.xml.model.XmlContentType;
import com.enonic.wem.api.xml.model.XmlForm;
import com.enonic.wem.api.xml.model.XmlMixin;
import com.enonic.wem.api.xml.model.XmlModule;
import com.enonic.wem.api.xml.model.XmlPageDescriptor;
import com.enonic.wem.api.xml.model.XmlPartDescriptor;

public final class XmlSerializers2
{
    private final static XmlFormSerializer FORM = new XmlFormSerializer();

    private final static XmlModuleSerializer MODULE = new XmlModuleSerializer();

    private final static XmlMixinSerializer MIXIN = new XmlMixinSerializer();

    private final static XmlPageDescriptorSerializer PAGE_DESCRIPTOR = new XmlPageDescriptorSerializer();

    private final static XmlPartDescriptorSerializer PART_DESCRIPTOR = new XmlPartDescriptorSerializer();

    private final static XmlContentTypeSerializer CONTENT_TYPE = new XmlContentTypeSerializer();

    public static XmlSerializer2<XmlForm> form()
    {
        return FORM;
    }

    public static XmlSerializer2<XmlModule> module()
    {
        return MODULE;
    }

    public static XmlSerializer2<XmlMixin> mixin()
    {
        return MIXIN;
    }

    public static XmlSerializer2<XmlPageDescriptor> pageDescriptor()
    {
        return PAGE_DESCRIPTOR;
    }

    public static XmlSerializer2<XmlPartDescriptor> partDescriptor()
    {
        return PART_DESCRIPTOR;
    }

    public static XmlSerializer2<XmlContentType> contentType()
    {
        return CONTENT_TYPE;
    }
}
