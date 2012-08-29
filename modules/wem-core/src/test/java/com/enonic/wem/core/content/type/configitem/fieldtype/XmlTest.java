package com.enonic.wem.core.content.type.configitem.fieldtype;


import org.junit.Test;

import com.enonic.wem.core.content.type.configitem.BreaksRequiredContractException;

import static com.enonic.wem.core.content.data.Data.newData;

public class XmlTest
{
    @Test(expected = BreaksRequiredContractException.class)
    public void checkBreaksRequiredContract_throws_exception_when_value_is_null()
    {
        new Xml().checkBreaksRequiredContract( newData().value( null ).build() );
    }

    @Test(expected = BreaksRequiredContractException.class)
    public void checkBreaksRequiredContract_throws_exception_when_value_is_empty_string()
    {
        new Xml().checkBreaksRequiredContract( newData().value( "" ).build() );
    }

    @Test(expected = BreaksRequiredContractException.class)
    public void checkBreaksRequiredContract_throws_exception_when_value_is_blank_string()
    {
        new Xml().checkBreaksRequiredContract( newData().value( "  " ).build() );
    }
}
