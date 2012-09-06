package com.enonic.wem.web.json.result.account;

import org.codehaus.jackson.node.ObjectNode;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.enonic.wem.api.account.Account;
import com.enonic.wem.api.account.RoleAccount;
import com.enonic.wem.api.account.UserAccount;
import com.enonic.wem.web.json.result.JsonResult;

public abstract class AccountJsonResult
    extends JsonResult
{
    public AccountJsonResult()
    {
        super( true );
    }

    protected void serializeAccount( ObjectNode json, Account account )
    {
        if ( json == null || account == null )
        {
            return;
        }
        json.put( "key", account.getKey().toString() );
        json.put( "type", account.getKey().getType().toString().toLowerCase() );
        json.put( "name", account.getKey().getLocalName() );
        json.put( "userStore", account.getKey().getUserStore() );
        json.put( "qualifiedName", account.getKey().getQualifiedName() );
        json.put( "builtIn", account.getKey().isBuiltIn() );
        json.put( "displayName", account.getDisplayName() );
        json.put( "modifiedTime", account.getModifiedTime().toString() );
        json.put( "createdTime", account.getCreatedTime().toString() );
        json.put( "editable", account.isEditable() );
        json.put( "deleted", account.isDeleted() );
        json.put( "image_url", getImageUrl( account ) );

        if ( account instanceof UserAccount )
        {
            serializeUser( json, (UserAccount) account );
        }
    }

    private void serializeUser( final ObjectNode json, final UserAccount account )
    {
        json.put( "email", account.getEmail() );
    }

    protected String getImageUrl( final Account account )
    {
        if ( account instanceof UserAccount )
        {
            return getImageUrl( (UserAccount) account );
        }

        if ( account instanceof RoleAccount )
        {
            return buildImageUrl( "default/role" );
        }

        return buildImageUrl( "default/group" );
    }

    private String getImageUrl( final UserAccount account )
    {
        if ( account.getKey().isAnonymous() )
        {
            return buildImageUrl( "default/anonymous" );
        }

        if ( account.getKey().isSuperUser() )
        {
            return buildImageUrl( "default/admin" );
        }

        if ( account.getImage() != null )
        {
            return buildImageUrl( account.getKey().toString() );
        }
        else
        {
            return buildImageUrl( "default/user" );
        }
    }

    private String buildImageUrl( final String path )
    {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path( "admin/rest/binary/account/image/" ).path(
            path ).build().toString();
    }
}
