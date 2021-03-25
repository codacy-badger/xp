package com.enonic.xp.impl.scheduler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.enonic.xp.impl.scheduler.distributed.SchedulableTask;

public interface SchedulerExecutorService
{
    boolean dispose(final String name);

    Set<String> disposeAllDone();

    Map<String, ScheduledFuture<?>> getAllFutures();

    ScheduledFuture<?> schedule( SchedulableTask command, long delay, TimeUnit unit );

    ScheduledFuture<?> scheduleAtFixedRate( SchedulableTask command, long initialDelay, long period,
                                            TimeUnit unit);



}
