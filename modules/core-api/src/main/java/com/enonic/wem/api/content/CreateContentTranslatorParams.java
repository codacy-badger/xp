package com.enonic.wem.api.content;

import java.time.Instant;
import java.util.List;

import com.google.common.base.Preconditions;

import com.enonic.wem.api.content.attachment.CreateAttachments;
import com.enonic.wem.api.data.PropertyTree;
import com.enonic.wem.api.index.ChildOrder;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.api.security.PrincipalKey;
import com.enonic.wem.api.security.acl.AccessControlList;

public class CreateContentTranslatorParams
{
    private final PropertyTree data;

    private final Metadatas metadata;

    private final ContentTypeName type;

    private final PrincipalKey owner;

    private final PrincipalKey creator;

    private final PrincipalKey modifier;

    private final Instant createdTime;

    private final Instant modifiedTime;

    private final String displayName;

    private final ContentName name;

    private final ContentPath parentContentPath;

    private final boolean valid;

    private final CreateAttachments createAttachments;

    private final AccessControlList permissions;

    private final boolean inheritPermissions;

    private final ChildOrder childOrder;

    private CreateContentTranslatorParams( Builder builder )
    {
        final Instant now = Instant.now();

        this.data = builder.data;
        this.metadata = builder.metadata;
        this.type = builder.type;
        this.owner = builder.owner;
        this.creator = builder.creator;
        this.modifier = builder.creator;
        this.createdTime = now;
        this.modifiedTime = now;
        this.displayName = builder.displayName;
        this.name = builder.name;
        this.parentContentPath = builder.parent;
        this.valid = builder.valid;
        this.permissions = builder.permissions;
        this.inheritPermissions = builder.inheritPermissions;
        this.createAttachments = builder.createAttachments;
        this.childOrder = builder.childOrder;
    }

    public static Builder create( final CreateContentParams source )
    {
        return new Builder( source );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public PropertyTree getData()
    {
        return data;
    }

    public Metadatas getMetadata()
    {
        return metadata;
    }

    public ContentTypeName getType()
    {
        return type;
    }

    public PrincipalKey getOwner()
    {
        return owner;
    }

    public PrincipalKey getCreator()
    {
        return creator;
    }

    public PrincipalKey getModifier()
    {
        return modifier;
    }

    public Instant getCreatedTime()
    {
        return createdTime;
    }

    public Instant getModifiedTime()
    {
        return modifiedTime;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public ContentName getName()
    {
        return name;
    }

    public ContentPath getParent()
    {
        return parentContentPath;
    }

    public boolean isValid()
    {
        return valid;
    }

    public CreateAttachments getCreateAttachments()
    {
        return createAttachments;
    }

    public AccessControlList getPermissions()
    {
        return permissions;
    }

    public boolean isInheritPermissions()
    {
        return inheritPermissions;
    }

    public ChildOrder getChildOrder()
    {
        return childOrder;
    }

    public static final class Builder
    {
        private PropertyTree data;

        private Metadatas metadata;

        private ContentTypeName type;

        private PrincipalKey owner;

        private PrincipalKey creator;

        private String displayName;

        private ContentName name;

        private ContentPath parent;

        private boolean valid;

        private AccessControlList permissions;

        private boolean inheritPermissions;

        private CreateAttachments createAttachments = CreateAttachments.empty();

        private ChildOrder childOrder;

        private Builder()
        {
        }

        private Builder( final CreateContentParams params )
        {
            this.data = params.getData();
            this.metadata = params.getMetadata();
            this.type = params.getType();
            this.owner = params.getOwner();
            this.displayName = params.getDisplayName();
            this.name = params.getName();
            this.parent = params.getParent();
            this.permissions = params.getPermissions();
            this.createAttachments = params.getCreateAttachments();
            this.inheritPermissions = params.isInheritPermissions();
            this.childOrder = params.getChildOrder();
        }

        public Builder contentData( final PropertyTree data )
        {
            this.data = data;
            return this;
        }

        public Builder metadata( final Metadatas metadata )
        {
            this.metadata = metadata;
            return this;
        }

        public Builder type( final ContentTypeName type )
        {
            this.type = type;
            return this;
        }

        public Builder owner( final PrincipalKey owner )
        {
            this.owner = owner;
            return this;
        }

        public Builder creator( final PrincipalKey creator )
        {
            this.creator = creator;
            return this;
        }

        public Builder displayName( final String displayName )
        {
            this.displayName = displayName;
            return this;
        }

        public Builder name( final ContentName name )
        {
            this.name = name;
            return this;
        }

        public Builder name( final String name )
        {
            this.name = ContentName.from( name );
            return this;
        }

        public Builder childOrder( final ChildOrder childOrder )
        {
            this.childOrder = childOrder;
            return this;
        }

        public Builder parent( final ContentPath parentContentPath )
        {
            this.parent = parentContentPath;
            return this;
        }

        public Builder valid( final boolean valid )
        {
            this.valid = valid;
            return this;
        }

        public Builder permissions( final AccessControlList permissions )
        {
            this.permissions = permissions;
            return this;
        }

        public Builder inheritPermissions( final boolean inheritPermissions )
        {
            this.inheritPermissions = inheritPermissions;
            return this;
        }

        public Builder createAttachments( final CreateAttachments createAttachments )
        {
            this.createAttachments = createAttachments;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( parent, "parentContentPath cannot be null" );
            Preconditions.checkArgument( parent.isAbsolute(), "parentContentPath must be absolute: " + parent );
            Preconditions.checkNotNull( data, "data cannot be null" );
            Preconditions.checkNotNull( displayName, "displayName cannot be null" );
            Preconditions.checkNotNull( createAttachments, "createAttachments cannot be null" );
            Preconditions.checkNotNull( type, "type cannot be null" );
            Preconditions.checkNotNull( creator, "creator cannot be null" );
            Preconditions.checkNotNull( name, "name cannom be null" );
            Preconditions.checkNotNull( valid, "valid cannot be null" );
            Preconditions.checkNotNull( childOrder, "childOrder cannot be null" );
        }

        public CreateContentTranslatorParams build()
        {
            this.validate();
            return new CreateContentTranslatorParams( this );
        }
    }


}
