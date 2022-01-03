package com.enonic.xp.web.session.impl;

import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.DefaultSessionCacheFactory;
import org.eclipse.jetty.server.session.FileSessionDataStoreFactory;
import org.eclipse.jetty.server.session.NullSessionDataStoreFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(enabled = false)
public class NullSessionStoreFactoryActivator
    extends AbstractSessionStoreFactoryActivator
{
    private final WebSessionStoreConfigService webSessionStoreConfigService;

    @Activate
    public NullSessionStoreFactoryActivator( final BundleContext bundleContext,
                                             @Reference final WebSessionStoreConfigService webSessionStoreConfigService )
    {
        super( bundleContext );
        this.webSessionStoreConfigService = webSessionStoreConfigService;
    }

    @Activate
    public void activate()
    {
        final NullSessionDataStoreFactory sessionDataStoreFactory = new NullSessionDataStoreFactory();
        FileSessionDataStoreFactory
        final DefaultSessionCacheFactory sessionCacheFactory = new DefaultSessionCacheFactory();
        sessionCacheFactory.setEvictionPolicy( DefaultSessionCache.NEVER_EVICT );
        sessionCacheFactory.setSaveOnCreate( webSessionStoreConfigService.isSaveOnCreate() );
        sessionCacheFactory.setFlushOnResponseCommit( webSessionStoreConfigService.isFlushOnResponseCommit() );

        registerServices( sessionDataStoreFactory, sessionCacheFactory );
    }

    @Deactivate
    public void deactivate()
    {
        unregisterServices();
    }
}
