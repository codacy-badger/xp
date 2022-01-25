package com.enonic.xp.impl.task.distributed;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.core.internal.osgi.OsgiSupport;

public final class OffloadedTaskCallable
    implements Callable<Void>, Serializable
{
    private static final Logger LOG = LoggerFactory.getLogger( OffloadedTaskCallable.class );

    private static final long serialVersionUID = 0;

    private final DescribedTask task;

    public OffloadedTaskCallable( final DescribedTask task )
    {
        this.task = task;
    }

    @Override
    public Void call()
    {
        LOG.info( "Task received for local execution {} {}", task.getName(), task.getTaskId() );

        return OsgiSupport.withService( TaskManager.class, "(local=true)", taskExecutor -> {
            LOG.info( "Task will be submitted locally {} {}" , task.getName(), task.getTaskId() );
            taskExecutor.submitTask( task );
            LOG.info( "Task submitted locally {} {}", task.getName(), task.getTaskId() );
            return null;
        } );
    }
}
