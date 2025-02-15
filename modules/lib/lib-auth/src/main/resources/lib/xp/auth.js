/**
 * Built-in authentication functions.
 *
 * @example
 * var authLib = require('/lib/xp/auth');
 *
 * @module auth
 */
/* global __*/

function required(params, name) {
    var value = params[name];
    if (value === undefined) {
        throw 'Parameter \'' + name + '\' is required';
    }

    return value;
}

function nullOrValue(value) {
    if (value === undefined) {
        return null;
    }

    return value;
}

/**
 * Login a user with the specified idProvider, userName, password and scope.
 *
 * @example-ref examples/auth/login.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.user Name of user to log in.
 * @param {string} [params.idProvider] Name of id provider where the user is stored. If not specified it will try all available id providers, in alphabetical order.
 * @param {string} [params.password] Password for the user. Ignored if skipAuth is set to true, mandatory otherwise.
 * @param {('SESSION'|'REQUEST')} [params.scope=SESSION] The scope of this login. Two values are valid. SESSION logs the user in and creates a session in XP for use in future requests. REQUEST logs the user in but only for this particular request and thus does not create a session.
 * @param {boolean} [params.skipAuth=false] Skip authentication.
 * @param {number} [params.sessionTimeout] Session timeout (in seconds). By default, the value of session.timeout from com.enonic.xp.web.jetty.cfg
 * @returns {object} Information for logged-in user.
 */
exports.login = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.LoginHandler');

    bean.setUser(required(params, 'user'));

    if (params.skipAuth) {
        bean.setSkipAuth(params.skipAuth);
    } else {
        bean.setPassword(required(params, 'password'));
    }

    if (params['idProvider']) {
        bean.setIdProvider([].concat(params['idProvider']));
    }

    if (params['scope']) {
        bean.setScope(params['scope']);
    } else {
        bean.setScope('SESSION');
    }

    bean.setSessionTimeout(nullOrValue(params['sessionTimeout']));

    return __.toNativeObject(bean.login());
};

/**
 * Logout an already logged-in user.
 *
 * @example-ref examples/auth/logout.js
 */
exports.logout = function () {
    var bean = __.newBean('com.enonic.xp.lib.auth.LogoutHandler');

    bean.logout();
};

/**
 * Returns the logged-in user. If not logged-in, this will return *undefined*.
 *
 * @example-ref examples/auth/getUser.js
 *
 * @param {object} [params] JSON parameters.
 * @param {boolean} [params.includeProfile=false] Include profile.
 *
 * @returns {object} Information for logged-in user.
 */
exports.getUser = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.GetUserHandler');

    bean.setIncludeProfile(!!(params && params.includeProfile));

    return __.toNativeObject(bean.getUser());
};

/**
 * Checks if the logged-in user has the specified role.
 *
 * @example-ref examples/auth/hasRole.js
 *
 * @param {string} role Role to check for.
 * @returns {boolean} True if the user has specfied role, false otherwise.
 */
exports.hasRole = function (role) {
    var bean = __.newBean('com.enonic.xp.lib.auth.HasRoleHandler');

    bean.setRole(__.nullOrValue(role));

    return bean.hasRole();
};

/**
 * Generates a secure password.
 *
 * @example-ref examples/auth/generatePassword.js
 *
 * @returns {string} A secure generated password.
 */
exports.generatePassword = function () {
    var bean = __.newBean('com.enonic.xp.lib.auth.GeneratePasswordHandler');

    return __.toNativeObject(bean.generatePassword());
};

/**
 * Changes password for specified user.
 *
 * @example-ref examples/auth/changePassword.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.userKey Key for user to change password.
 * @param {string} params.password New password to set.
 */
exports.changePassword = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.ChangePasswordHandler');

    bean.setUserKey(required(params, 'userKey'));
    bean.setPassword(required(params, 'password'));

    bean.changePassword();
};


/**
 * Returns the principal with the specified key.
 *
 * @example-ref examples/auth/getPrincipal.js
 *
 * @param {string} principalKey Principal key to look for.
 * @returns {object} the principal specified, or null if it doesn't exist.
 */
exports.getPrincipal = function (principalKey) {
    var bean = __.newBean('com.enonic.xp.lib.auth.GetPrincipalHandler');

    bean.setPrincipalKey(__.nullOrValue(principalKey));

    return __.toNativeObject(bean.getPrincipal());
};

/**
 * Returns a list of principals the specified principal is a member of.
 *
 * @example-ref examples/auth/getMemberships.js
 *
 * @param {string} principalKey Principal key to retrieve memberships for.
 * @param {boolean} [transitive=false] Retrieve transitive memberships.
 * @returns {object[]} Returns the list of principals.
 */
exports.getMemberships = function (principalKey, transitive) {
    var bean = __.newBean('com.enonic.xp.lib.auth.GetMembershipsHandler');

    bean.setPrincipalKey(__.nullOrValue(principalKey));
    bean.setTransitive(transitive);

    return __.toNativeObject(bean.getMemberships());
};

/**
 * Returns a list of principals that are members of the specified principal.
 *
 * @example-ref examples/auth/getMembers.js
 *
 * @param {string} principalKey Principal key to retrieve members for.
 * @returns {object[]} Returns the list of principals.
 */
exports.getMembers = function (principalKey) {
    var bean = __.newBean('com.enonic.xp.lib.auth.GetMembersHandler');

    bean.setPrincipalKey(__.nullOrValue(principalKey));

    return __.toNativeObject(bean.getMembers());
};

/**
 * Creates user from passed parameters.
 *
 * @example-ref examples/auth/createUser.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.idProvider Key for id provider where user has to be created.
 * @param {string} params.name User login name to set.
 * @param {string} params.displayName User display name.
 * @param {string} [params.email] User email.
 */
exports.createUser = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.CreateUserHandler');

    bean.setIdProvider(required(params, 'idProvider'));
    bean.setName(required(params, 'name'));
    bean.setDisplayName(nullOrValue(params.displayName));
    bean.setEmail(nullOrValue(params.email));

    return __.toNativeObject(bean.createUser());
};

/**
 * Retrieves the user specified and updates it with the changes applied.
 *
 * @example-ref examples/auth/modifyUser.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Principal key of the user to modify.
 * @param {function} params.editor User editor function to apply to user.
 * @returns {object} the updated user.
 */
exports.modifyUser = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.ModifyUserHandler');

    bean.setPrincipalKey(required(params, 'key'));
    bean.setEditor(__.toScriptValue(required(params, 'editor')));

    return __.toNativeObject(bean.modifyUser());
};

/**
 * Creates a group.
 *
 * @example-ref examples/auth/createGroup.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.idProvider Key for id provider where group has to be created.
 * @param {string} params.name Group name.
 * @param {string} params.displayName Group display name.
 * @param {string} params.description as principal description .
 */
exports.createGroup = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.CreateGroupHandler');

    bean.setIdProvider(required(params, 'idProvider'));
    bean.setName(required(params, 'name'));
    bean.setDisplayName(nullOrValue(params.displayName));
    bean.setDescription(nullOrValue(params.description));

    return __.toNativeObject(bean.createGroup());
};

/**
 * Retrieves the group specified and updates it with the changes applied.
 *
 * @example-ref examples/auth/modifyGroup.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Principal key of the group to modify.
 * @param {function} params.editor Group editor function to apply to group.
 * @returns {object} the updated group.
 */
exports.modifyGroup = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.ModifyGroupHandler');

    bean.setPrincipalKey(required(params, 'key'));
    bean.setEditor(__.toScriptValue(required(params, 'editor')));

    return __.toNativeObject(bean.modifyGroup());
};

/**
 * Adds members to a principal (user or role).
 *
 * @example-ref examples/auth/addMembers.js
 *
 * @param {string} principalKey Key of the principal to add members to.
 * @param {string} members Keys of the principals to add.
 */
exports.addMembers = function (principalKey, members) {
    var bean = __.newBean('com.enonic.xp.lib.auth.AddMembersHandler');

    bean.setPrincipalKey(nullOrValue(principalKey));
    bean.setMembers([].concat(__.nullOrValue(members)));

    return __.toNativeObject(bean.addMembers());
};

/**
 * Removes members from a principal (user or role).
 *
 * @example-ref examples/auth/removeMembers.js
 *
 * @param {string} principalKey Key of the principal to remove members from.
 * @param {string} members Keys of the principals to remove.
 */
exports.removeMembers = function (principalKey, members) {
    var bean = __.newBean('com.enonic.xp.lib.auth.RemoveMembersHandler');

    bean.setPrincipalKey(nullOrValue(principalKey));
    bean.setMembers([].concat(__.nullOrValue(members)));

    return __.toNativeObject(bean.removeMembers());
};

/**
 * Search for principals matching the specified criteria.
 *
 * @example-ref examples/auth/findPrincipals.js
 *
 * @param {object} params JSON parameters.
 * @param {string} [params.type] Principal type to look for, one of: 'user', 'group' or 'role'. If not specified all principal types will be included.
 * @param {string} [params.idProvider] Key of the id provider to look for. If not specified all id providers will be included.
 * @param {string} [params.start] First principal to return from the search results. It can be used for pagination.
 * @param {string} [params.count] A limit on the number of principals to be returned.
 * @param {string} [params.name] Name of the principal to look for.
 * @param {string} [params.searchText] Text to look for in any principal field.
 * @returns {object} The "total" number of principals matching the search, the "count" of principals included, and an array of "hits" containing the principals.
 */
exports.findPrincipals = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.FindPrincipalsHandler');

    bean.setType(__.nullOrValue(params.type));
    bean.setIdProvider(__.nullOrValue(params.idProvider));
    bean.setStart(__.nullOrValue(params.start));
    bean.setCount(__.nullOrValue(params.count));
    bean.setName(__.nullOrValue(params.name));
    bean.setSearchText(__.nullOrValue(params.searchText));

    return __.toNativeObject(bean.findPrincipals());
};

/**
 * Deletes the principal with the specified key.
 *
 * @example-ref examples/auth/deletePrincipal.js
 *
 * @param {string} principalKey Principal key to delete.
 * @returns {boolean} True if deleted, false otherwise.
 */
exports.deletePrincipal = function (principalKey) {
    var bean = __.newBean('com.enonic.xp.lib.auth.DeletePrincipalHandler');
    bean.setPrincipalKey(__.nullOrValue(principalKey));
    return __.toNativeObject(bean.deletePrincipal());
};

/**
 * This function returns the ID provider configuration.
 * It is meant to be called from an ID provider controller.
 *
 * @example-ref examples/auth/getIdProviderConfig.js
 *
 * @returns {object} The ID provider configuration as JSON.
 */
exports.getIdProviderConfig = function () {
    var bean = __.newBean('com.enonic.xp.lib.auth.GetIdProviderConfigHandler');
    return __.toNativeObject(bean.execute());
};

/**
 * This function retrieves the profile of a user.
 *
 * @example-ref examples/auth/getProfile.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Principal key of the user.
 * @param {string} [params.scope] Scope of the data to retrieve.
 * @returns {object} The extra data as JSON
 */
exports.getProfile = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.GetProfileHandler');

    bean.setKey(__.nullOrValue(params.key));
    bean.setScope(__.nullOrValue(params.scope));

    return __.toNativeObject(bean.execute());
};

/**
 * This function retrieves the profile of a user and updates it.
 *
 * @example-ref examples/auth/modifyProfile.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Principal key of the user.
 * @param {string} [params.scope] Scope of the data to retrieve and update.
 * @param {function} params.editor Profile editor function to apply.
 * @returns {object} The extra data as JSON
 */
exports.modifyProfile = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.ModifyProfileHandler');

    bean.setKey(__.nullOrValue(params.key));
    bean.setScope(__.nullOrValue(params.scope));
    bean.setEditor(__.toScriptValue(required(params, 'editor')));

    return __.toNativeObject(bean.execute());
};

/**
 * Search for users matching the specified query.
 *
 * @example-ref examples/auth/findUsers.js
 *
 * @param {object} params JSON with the parameters.
 * @param {number} [params.start=0] Start index (used for paging).
 * @param {number} [params.count=10] Number of contents to fetch.
 * @param {string} params.query Query expression.
 * @param {string} [params.sort] Sorting expression.
 * @param {boolean} [params.includeProfile=false] Include profile.
 *
 * @returns {boolean} Result of query.
 */
exports.findUsers = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.FindUsersHandler');
    bean.setStart(params.start);
    bean.setCount(params.count);
    bean.setQuery(nullOrValue(params.query));
    bean.setSort(nullOrValue(params.sort));
    bean.setIncludeProfile(!!params.includeProfile);
    return __.toNativeObject(bean.execute());
};

/**
 * Creates a role.
 *
 * @example-ref examples/auth/createRole.js
 *
 * @param {string} params.name Role name.
 * @param {string} params.displayName Role display name.
 * @param {string} params.description as principal description .
 */
exports.createRole = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.CreateRoleHandler');

    bean.setName(required(params, 'name'));
    bean.setDisplayName(nullOrValue(params.displayName));
    bean.setDescription(nullOrValue(params.description));

    return __.toNativeObject(bean.createRole());
};

/**
 * Retrieves the role specified and updates it with the changes applied.
 *
 * @example-ref examples/auth/modifyRole.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Principal key of the role to modify.
 * @param {function} params.editor Role editor function to apply to role.
 * @returns {object} the updated role.
 */
exports.modifyRole = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.auth.ModifyRoleHandler');

    bean.setPrincipalKey(required(params, 'key'));
    bean.setEditor(__.toScriptValue(required(params, 'editor')));

    return __.toNativeObject(bean.modifyRole());
};
