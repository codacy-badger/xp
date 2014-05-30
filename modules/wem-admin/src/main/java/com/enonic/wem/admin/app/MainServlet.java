package com.enonic.wem.admin.app;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

@Singleton
public final class MainServlet
    extends HttpServlet
{
    private AppHtmlHandler appHtmlHandler;

    private ResourceHandler resourceHandler;

    @Inject
    protected ResourceLocator resourceLocator;

    @Override
    public void init()
        throws ServletException
    {
        super.init();
        this.appHtmlHandler = new AppHtmlHandler();
        this.resourceHandler = new ResourceHandler( getServletContext(), this.resourceLocator );
    }

    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse res )
        throws ServletException, IOException
    {
        final String path = req.getRequestURI().substring( req.getContextPath().length() );
        if ( path.equals( "/admin" ) || path.equals( "/admin/" ) )
        {
            this.appHtmlHandler.handle( req, res );
            return;
        }

        this.resourceHandler.handle( req, res );
    }
}
