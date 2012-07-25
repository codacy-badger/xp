package com.enonic.wem.core.content.data;

/**
 * Entry is the base class for Value and SubTypeEntries.
 */
public abstract class Entry
{
    public abstract ValuePath getPath();

    public String getName()
    {
        return getPath().getLastElement().getName();
    }

}
