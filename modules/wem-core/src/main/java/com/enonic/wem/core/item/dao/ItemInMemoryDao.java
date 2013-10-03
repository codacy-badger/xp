package com.enonic.wem.core.item.dao;


import java.util.LinkedHashMap;

import com.google.common.base.Preconditions;

import com.enonic.wem.api.item.Item;
import com.enonic.wem.api.item.ItemId;
import com.enonic.wem.api.item.ItemPath;

public class ItemInMemoryDao
    implements ItemDao
{
    private final ItemIdByPath itemIdByPath;

    private final ItemByItemId itemByItemId;

    public ItemInMemoryDao()
    {
        itemByItemId = new ItemByItemId( new LinkedHashMap<ItemId, Item>() );
        itemIdByPath = new ItemIdByPath( new LinkedHashMap<ItemPath, ItemId>() );
    }

    @Override
    public Item storeNew( final Item item )
    {
        Preconditions.checkArgument( item.id() == null, "New Item to store cannot have an ItemId: " + item.id() );
        final ItemPath path = item.path();
        Preconditions.checkNotNull( path, "Path of Item must be specified " );
        Preconditions.checkArgument( path.isAbsolute(), "Path to Item must be absolute: " + path.toString() );
        final ItemPath parentPath = path.getParentPath();

        if ( !itemIdByPath.pathHasItem( parentPath ) )
        {
            throw new NoItemAtPathFound( parentPath );
        }

        final Item itemWithId = Item.newItem( item ).id( new ItemId() ).build();

        itemByItemId.storeNew( itemWithId );
        itemIdByPath.put( itemWithId.path(), itemWithId.id() );
        return itemWithId;
    }

    @Override
    public Item updateExisting( final Item item )
    {
        itemByItemId.updateExisting( item );
        return item;
    }

    public Item getItemById( final ItemId id )
    {
        return this.itemByItemId.get( id );
    }

    public Item getItemByPath( final ItemPath path )
        throws NoItemFoundException
    {
        Preconditions.checkArgument( path.isAbsolute(), "path must be absolute: " + path.toString() );

        return this.itemByItemId.get( itemIdByPath.get( path ) );
    }
}
