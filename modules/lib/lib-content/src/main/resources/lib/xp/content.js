/**
 * Functions and constants to find and manipulate content.
 *
 * @example
 * var contentLib = require('/lib/xp/content');
 *
 * @module content
 */
/* global __, Java*/

exports.ARCHIVE_ROOT_PATH = Java.type('com.enonic.xp.archive.ArchiveConstants').ARCHIVE_ROOT_PATH;
exports.CONTENT_ROOT_PATH = Java.type('com.enonic.xp.content.ContentConstants').CONTENT_ROOT_PATH;


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
 * @typedef ContentType
 * @type Object
 * @property {string} name Name of the content type.
 * @property {string} displayName Display name of the content type.
 * @property {string} description Description of the content type.
 * @property {string} superType Name of the super type, or null if it has no super type.
 * @property {boolean} abstract Whether or not content of this type may be instantiated.
 * @property {boolean} final Whether or not it may be used as super type of other content types.
 * @property {boolean} allowChildContent Whether or not allow creating child items on content of this type.
 * @property {string} displayNameExpression ES6 string template for generating the content name based on values in the content form.
 * @property {object} [icon] Icon of the content type.
 * @property {object} [icon.data] Stream with the binary data for the icon.
 * @property {string} [icon.mimeType] Mime type of the icon image.
 * @property {string} [icon.modifiedTime] Modified time of the icon. May be used for caching.
 * @property {object[]} form Form schema represented as an array of form items: Input, ItemSet, Layout, OptionSet.
 */

/**
 * This function fetches a content.
 *
 * @example-ref examples/content/get.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 * @param {string} [params.versionId] Version Id of the content.
 *
 * @returns {object} The content (as JSON) fetched from the repository.
 */
exports.get = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetContentHandler');
    bean.setKey(required(params, 'key'));
    bean.setVersionId(nullOrValue(params.versionId));
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns a content attachments.
 *
 * @example-ref examples/content/getAttachments.js
 *
 * @param {string} key Path or id to the content.
 *
 * @returns {object} An object with all the attachments that belong to the content, where the key is the attachment name. Or null if the content cannot be found.
 */
exports.getAttachments = function (key) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetAttachmentsHandler');
    bean.setKey(nullOrValue(key));
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns a data-stream for the specified content attachment.
 *
 * @example-ref examples/content/getAttachmentStream.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 * @param {string} params.name Attachment name.
 *
 * @returns {*} Stream of the attachment data.
 */
exports.getAttachmentStream = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetAttachmentStreamHandler');
    bean.setKey(required(params, 'key'));
    bean.setName(required(params, 'name'));
    return bean.getStream();
};

/**
 * Adds an attachment to an existing content.
 *
 * @example-ref examples/content/addAttachment.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 * @param {string} params.name Attachment name.
 * @param {string} params.mimeType Attachment content type.
 * @param {string} [params.label] Attachment label.
 * @param {object} params.data Stream with the binary data for the attachment.
 */
exports.addAttachment = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.AddAttachmentHandler');
    bean.setKey(required(params, 'key'));
    bean.setName(required(params, 'name'));
    bean.setMimeType(required(params, 'mimeType'));
    bean.setLabel(nullOrValue(params.label));
    bean.setData(required(params, 'data'));
    bean.execute();
};

/**
 * Removes an attachment from an existing content.
 *
 * @example-ref examples/content/removeAttachment.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 * @param {string|string[]} params.name Attachment name, or array of names.
 */
exports.removeAttachment = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.RemoveAttachmentHandler');
    bean.setKey(required(params, 'key'));
    bean.setName([].concat(required(params, 'name')));
    bean.execute();
};

/**
 * This function returns the parent site of a content.
 *
 * @example-ref examples/content/getSite.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 *
 * @returns {object} The current site as JSON.
 */
exports.getSite = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetSiteHandler');
    bean.setKey(nullOrValue(params.key));
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns the site configuration for this app in the parent site of a content.
 *
 * @example-ref examples/content/getSiteConfig.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 * @param {string} params.applicationKey Application key.
 *
 * @returns {object} The site configuration for current application as JSON.
 */
exports.getSiteConfig = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetSiteConfigHandler');
    bean.setKey(nullOrValue(params.key));
    bean.setApplicationKey(nullOrValue(params.applicationKey));
    return __.toNativeObject(bean.execute());
};

/**
 * This function deletes a content.
 *
 * @example-ref examples/content/delete.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 *
 * @returns {boolean} True if deleted, false otherwise.
 */
exports.delete = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.DeleteContentHandler');
    bean.setKey(required(params, 'key'));
    return bean.execute();
};

/**
 * This function fetches children of a content.
 *
 * @example-ref examples/content/getChildren.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the parent content.
 * @param {number} [params.start=0] Start index (used for paging).
 * @param {number} [params.count=10] Number of contents to fetch.
 * @param {string} [params.sort] Sorting expression.
 *
 * @returns {Object} Result (of content) fetched from the repository.
 */
exports.getChildren = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetChildContentHandler');
    bean.setKey(required(params, 'key'));
    bean.setStart(params.start);
    bean.setCount(params.count);
    bean.setSort(nullOrValue(params.sort));
    return __.toNativeObject(bean.execute());
};

/**
 * This function creates a content.
 *
 * The parameter `name` is optional, but if it is not set then `displayName` must be specified. When name is not set, the
 * system will auto-generate a `name` based on the `displayName`, by lower-casing and replacing certain characters. If there
 * is already a content with the auto-generated name, a suffix will be added to the `name` in order to make it unique.
 *
 * To create a content where the name is not important and there could be multiple instances under the same parent content,
 * skip the `name` parameter and specify a `displayName`.
 *
 * @example-ref examples/content/create.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} [params.name] Name of content.
 * @param {string} params.parentPath Path to place content under.
 * @param {string} [params.displayName] Display name. Default is same as `name`.
 * @param {boolean} [params.requireValid=true] The content has to be valid, according to the content type, to be created. If requireValid=true and the content is not strictly valid, an error will be thrown.
 * @param {boolean} [params.refresh=true] If refresh is true, the created content will to be searchable through queries immediately, else within 1 second. Since there is a performance penalty doing this refresh, refresh should be set to false for bulk operations.
 * @param {string} params.contentType Content type to use.
 * @param {string} [params.language] The language tag representing the content’s locale.
 * @param {string} [params.childOrder] Default ordering of children when doing getChildren if no order is given in query
 * @param {object} params.data Actual content data.
 * @param {object} [params.x] eXtra data to use.
 * @param {object} [params.workflow] Workflow information to use. Default has state READY and empty check list.
 *
 * @returns {object} Content created as JSON.
 */
exports.create = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.CreateContentHandler');
    bean.setName(nullOrValue(params.name));
    bean.setParentPath(nullOrValue(params.parentPath));
    bean.setDisplayName(nullOrValue(params.displayName));
    bean.setContentType(nullOrValue(params.contentType));
    bean.setRequireValid(nullOrValue(params.requireValid));
    bean.setRefresh(nullOrValue(params.refresh));
    bean.setLanguage(nullOrValue(params.language));
    bean.setChildOrder(nullOrValue(params.childOrder));

    bean.setData(__.toScriptValue(params.data));
    bean.setX(__.toScriptValue(params.x));

    bean.setIdGenerator(nullOrValue(params.idGenerator));
    bean.setWorkflow(__.toScriptValue(params.workflow));

    return __.toNativeObject(bean.execute());
};

/**
 * This command queries content.
 *
 * @example-ref examples/content/query.js
 *
 * @param {object} params JSON with the parameters.
 * @param {number} [params.start=0] Start index (used for paging).
 * @param {number} [params.count=10] Number of contents to fetch.
 * @param {string|object} params.query Query expression.
 * @param {object} [params.filters] Filters to apply to query result
 * @param {string|object|object[]} [params.sort] Sorting expression.
 * @param {string} [params.aggregations] Aggregations expression.
 * @param {string[]} [params.contentTypes] Content types to filter on.
 *
 * @returns {Object} Result of query.
 */
exports.query = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.QueryContentHandler');
    bean.setStart(params.start);
    bean.setCount(params.count);
    bean.setQuery(__.toScriptValue((params.query)));
    bean.setSort(__.toScriptValue(params.sort));
    bean.setAggregations(__.toScriptValue(params.aggregations));
    bean.setContentTypes(__.toScriptValue(params.contentTypes));
    bean.setFilters(__.toScriptValue(params.filters));
    bean.setHighlight(__.toScriptValue(params.highlight));
    return __.toNativeObject(bean.execute());
};

/**
 * This function modifies a content.
 *
 * @example-ref examples/content/modify.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.key Path or id to the content.
 * @param {function} params.editor Editor callback function.
 * @param {boolean} [params.requireValid=true] The content has to be valid, according to the content type, to be updated. If requireValid=true and the content is not strictly valid, an error will be thrown.
 *
 * @returns {object} Modified content as JSON.
 */
exports.modify = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.ModifyContentHandler');
    bean.setKey(required(params, 'key'));
    bean.setEditor(__.toScriptValue(params.editor));
    bean.setRequireValid(nullOrValue(params.requireValid));
    return __.toNativeObject(bean.execute());
};

/**
 * This function publishes content to a branch.
 *
 * @example-ref examples/content/publish.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string[]} params.keys List of all content keys(path or id) that should be published.
 * @param {string} params.sourceBranch The branch where the content to be published is stored.
 * @param {string} params.targetBranch The branch to which the content should be published.  Technically, publishing is just a move from one branch
 * to another, and publishing user content from master to draft is therefore also valid usage of this function, which may be practical if user input to a web-page is stored on master.
 * @param {object} [params.schedule] Schedule the publish.
 * @param {string} [params.schedule.from] Time from which the content is considered published. Defaults to the time of the publish
 * @param {string} [params.schedule.to] Time until which the content is considered published.
 * @param {string[]} [params.excludeChildrenIds] List of all content keys which children should be excluded from publishing content.
 * @param {boolean} [params.includeDependencies=true] Whether all related content should be included when publishing content.
 * @param {string} params.message Publish message.
 *
 * @returns {object} Status of the publish operation in JSON.
 */
exports.publish = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.PublishContentHandler');
    bean.setKeys(required(params, 'keys'));
    bean.setTargetBranch(required(params, 'targetBranch'));
    bean.setSourceBranch(required(params, 'sourceBranch'));
    bean.setContentPublishInfo(__.toScriptValue(params.schedule));
    if (params.excludeChildrenIds) {
        bean.setExcludeChildrenIds(params.excludeChildrenIds);
    }
    if (!nullOrValue(params.includeChildren)) {
        // keep for backwards compatibility
        bean.setIncludeChildren(params.includeChildren);
    }
    if (!nullOrValue(params.includeDependencies)) {
        bean.setIncludeDependencies(params.includeDependencies);
    }
    bean.setMessage(nullOrValue(params.message));
    return __.toNativeObject(bean.execute());
};

/**
 * This function unpublishes content that had been published to the master branch.
 *
 * @example-ref examples/content/unpublish.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string[]} params.keys List of all content keys(path or id) that should be unpublished.
 *
 * @returns {string[]} List with ids of the content that were unpublished.
 */
exports.unpublish = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.UnpublishContentHandler');
    bean.setKeys(required(params, 'keys'));
    return __.toNativeObject(bean.execute());
};


/**
 * Check if content exists.
 *
 * @example-ref examples/content/exists.js
 *
 * @param {string} [params.key] content id.
 *
 * @returns {boolean} True if exist, false otherwise.
 */
exports.exists = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.ContentExistsHandler');

    bean.setKey(required(params, 'key'));

    return __.toNativeObject(bean.execute());
};

/**
 * Creates a media content.
 *
 * @example-ref examples/content/createMedia.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} [params.name] Name of content.
 * @param {string} [params.parentPath=/] Path to place content under.
 * @param {string} [params.mimeType] Mime-type of the data.
 * @param {number} [params.focalX] Focal point for X axis (if it's an image).
 * @param {number} [params.focalY] Focal point for Y axis (if it's an image).
 * @param  params.data Data (as stream) to use.
 *
 * @returns {object} Returns the created media content.
 */
exports.createMedia = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.CreateMediaHandler');
    bean.setName(required(params, 'name'));
    bean.setParentPath(nullOrValue(params.parentPath));
    bean.setMimeType(nullOrValue(params.mimeType));
    if (params.focalX) {
        bean.setFocalX(params.focalX);
    }
    if (params.focalY) {
        bean.setFocalY(params.focalY);
    }
    bean.setData(nullOrValue(params.data));
    bean.setIdGenerator(nullOrValue(params.idGenerator));
    return __.toNativeObject(bean.execute());
};

/**
 * Rename a content or move it to a new path.
 *
 * @example-ref examples/content/move.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.source Path or id of the content to be moved or renamed.
 * @param {string} params.target New path or name for the content. If the target ends in slash '/', it specifies the parent path where to be moved. Otherwise it means the new desired path or name for the content.
 *
 * @returns {object} The content that was moved or renamed.
 */
exports.move = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.MoveContentHandler');
    bean.setSource(required(params, 'source'));
    bean.setTarget(required(params, 'target'));
    return __.toNativeObject(bean.execute());
};

/**
 * Archive a content.
 *
 * @example-ref examples/content/archive.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.content Path or id of the content to be archived.
 *
 * @returns {string[]} List with ids of the contents that were archived.
 */
exports.archive = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.ArchiveContentHandler');
    bean.setContent(required(params, 'content'));
    return __.toNativeObject(bean.execute());
};

/**
 * Restore a content from the archive.
 *
 * @example-ref examples/content/restore.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.content Path or id of the content to be restored.
 * @param {string} params.path Path of parent for restored content.
 *
 * @returns {string[]} List with ids of the contents that were restored.
 */
exports.restore = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.RestoreContentHandler');
    bean.setContent(required(params, 'content'));
    bean.setPath(nullOrValue(params.path));
    return __.toNativeObject(bean.execute());
};

/**
 * Sets permissions on a content.
 *
 * @example-ref examples/content/setPermissions.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Path or id of the content.
 * @param {boolean} [params.inheritPermissions] Set to true if the content must inherit permissions. Default to false.
 * @param {boolean} [params.overwriteChildPermissions] Set to true to overwrite child permissions. Default to false.
 * @param {array} [params.permissions] Array of permissions.
 * @param {string} params.permissions.principal Principal key.
 * @param {array} params.permissions.allow Allowed permissions.
 * @param {array} params.permissions.deny Denied permissions.
 * @returns {boolean} True if successful, false otherwise.
 */
exports.setPermissions = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.SetPermissionsHandler');

    if (params.key) {
        bean.setKey(params.key);
    }
    if (params.inheritPermissions) {
        bean.setInheritPermissions(params.inheritPermissions);
    }
    if (params.overwriteChildPermissions) {
        bean.setOverwriteChildPermissions(params.overwriteChildPermissions);
    }
    if (params.permissions) {
        bean.setPermissions(__.toScriptValue(params.permissions));
    }
    return __.toNativeObject(bean.execute());
};

/**
 * Gets permissions on a content.
 *
 * @example-ref examples/content/getPermissions.js
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Path or id of the content.
 * @returns {object} Content permissions.
 */
exports.getPermissions = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetPermissionsHandler');

    if (params.key) {
        bean.setKey(params.key);
    }
    return __.toNativeObject(bean.execute());
};

/**
 * Returns the properties and icon of the specified content type.
 *
 * @example-ref examples/content/getType.js
 *
 * @param name Name of the content type, as 'app:name' (e.g. 'com.enonic.myapp:article').
 * @returns {ContentType} The content type object if found, or null otherwise. See ContentType type definition below.
 */
exports.getType = function (name) {
    var bean = __.newBean('com.enonic.xp.lib.content.ContentTypeHandler');
    bean.setName(nullOrValue(name));
    return __.toNativeObject(bean.getContentType());
};

/**
 * Returns the list of all the content types currently registered in the system.
 *
 * @example-ref examples/content/getTypes.js
 *
 * @returns {ContentType[]} Array with all the content types found. See ContentType type definition below.
 */
exports.getTypes = function () {
    var bean = __.newBean('com.enonic.xp.lib.content.ContentTypeHandler');
    return __.toNativeObject(bean.getAllContentTypes());
};

/**
 * Returns outbound dependencies on a content.
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Path or id of the content.
 * @returns {object} Content Ids.
 */
exports.getOutboundDependencies = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.GetOutboundDependenciesHandler');

    bean.setKey(required(params, 'key'));

    return __.toNativeObject(bean.execute());
};

/** Resets dropped inherit flags back.
 *
 * @param {object} params JSON parameters.
 * @param {string} params.key Path or id of the content.
 * @param {string} params.projectName name of project with content.
 * @param {string[]} params.inherit flags to be reset.
 */
exports.resetInheritance = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.content.ResetInheritanceHandler');

    bean.setKey(required(params, 'key'));
    bean.setProjectName(required(params, 'projectName'));
    bean.setInherit(required(params, 'inherit'));

    return __.toNativeObject(bean.execute());
};
