package com.enonic.xp.impl.scheduler;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;

import com.enonic.xp.impl.scheduler.distributed.SchedulableTask;

@Component(immediate = true)
public final class ClusteredSystemScheduler
    implements SystemScheduler
{
    private final IScheduledExecutorService hazelcastExecutor;

    @Activate
    public ClusteredSystemScheduler( @Reference final HazelcastInstance hazelcastInstance )
    {
        hazelcastExecutor = hazelcastInstance.getScheduledExecutorService( "scheduler" );
    }

    @Override
    public Map<String, ScheduledFuture<?>> getAllFutures()
    {
        return hazelcastExecutor.getAllScheduledFutures().values().
            stream().
            flatMap( Collection::stream ).
            filter( future -> future.getHandler() != null ).
            collect( Collectors.toMap( future -> future.getHandler().getTaskName(), future -> future ) );
    }

    @Override
    public Set<String> disposeAllDone()
    {
        final Set<IScheduledFuture<?>> futures = hazelcastExecutor.getAllScheduledFutures().values().stream().
            flatMap( Collection::stream ).
            filter( Future::isDone ).
            collect( Collectors.toSet() );

        futures.forEach( IScheduledFuture::dispose );

        return futures.stream().
            filter( future -> future.getHandler() != null ).
            map( future -> future.getHandler().getTaskName() ).
            filter( Objects::nonNull ).
            collect( Collectors.toSet() );
    }

    @Override
    public boolean dispose( final String name )
    {
        return hazelcastExecutor.getAllScheduledFutures().values().stream().
            flatMap( Collection::stream ).
            filter( future -> future.getHandler() != null ).
            filter( future -> name.equals( future.getHandler().getTaskName() ) ).
            findAny().
            map( future -> {
                future.dispose();
                return true;
            } ).
            orElse( false );
    }

    public ScheduledFuture<?> schedule( final SchedulableTask task, final long delay, final TimeUnit unit )
    {
        return hazelcastExecutor.schedule( task, delay, unit );
    }

    public ScheduledFuture<?> scheduleAtFixedRate( final SchedulableTask task, final long initialDelay, final long period, final TimeUnit unit )
    {
        return hazelcastExecutor.scheduleAtFixedRate( task, initialDelay, period, unit );
    }
}
