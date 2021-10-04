package com.enonic.xp.web.jetty.impl;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.handler.ErrorHandler;

public class LastResortErrorHandler
    extends ErrorHandler
{
    @Override
    protected void writeErrorPage( final HttpServletRequest request, final Writer writer, final int code, final String message,
                                   final boolean showStacks )
        throws IOException
    {
        String text = code + " - " + HttpStatus.getMessage( code );
        writer.write( "<!DOCTYPE html>\n<html>\n<head>\n<title>" );
        writer.write( text );
        writer.write( "</title>\n</head>\n<body>" );
        writer.write( text );
        writer.write( "</body>\n</html>" );
    }
}
