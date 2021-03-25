package com.enonic.xp.impl.scheduler;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.enonic.xp.impl.scheduler.distributed.SchedulableTask;

@Component(immediate = true)
@Local
public final class LocalSystemScheduler
    implements SystemScheduler
{
    private final ScheduledExecutorService simpleExecutor;

    private final Map<String, ScheduledFuture<?>> scheduledFutures;

    @Activate
    public LocalSystemScheduler()
    {
        simpleExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledFutures = new ConcurrentHashMap<>();
    }

    @Override
    public Map<String, ScheduledFuture<?>> getAllFutures()
    {
        return Map.copyOf( scheduledFutures );
    }

    @Override
    public Set<String> disposeAllDone()
    {
        final Set<String> result = scheduledFutures.entrySet().
            stream().
            map( entry -> {
                if ( entry.getValue().isDone() )
                {
                    return entry.getKey();
                }
                return null;
            } ).
            filter( Objects::nonNull ).
            collect( Collectors.toSet() );

        result.forEach( this::dispose );

        return result;
    }

    @Override
    public boolean dispose( final String name )
    {
        final ScheduledFuture<?> future = scheduledFutures.remove( name );
        if ( future != null )
        {
            if ( !future.isDone() )
            {
                future.cancel( false );
            }
            return true;
        }
        return false;
    }

    public ScheduledFuture<?> schedule( final SchedulableTask task, final long delay, final TimeUnit unit )
    {
        if ( scheduledFutures.containsKey( task.getName() ) )
        {
            throw new IllegalArgumentException( String.format( "[%s] task is scheduled already.", task.getName() ) );
        }
        final ScheduledFuture<?> future = simpleExecutor.schedule( task, delay, unit );
        scheduledFutures.put( task.getName(), future );

        return future;
    }

    public ScheduledFuture<?> scheduleAtFixedRate( final SchedulableTask task, final long initialDelay, final long period, final TimeUnit unit )
    {
        if ( scheduledFutures.containsKey( task.getName() ) )
        {
            throw new IllegalArgumentException( String.format( "[%s] task is scheduled already.", task.getName() ) );
        }
        final ScheduledFuture<?> future = simpleExecutor.scheduleAtFixedRate( task, initialDelay, period, unit );
        scheduledFutures.put( task.getName(), future );

        return future;
    }

}
