/**
 * Functions to log and find audit logs.
 *
 * @example
 * var auditLib = require('/lib/xp/auditlog');
 *
 * @module audit
 */
/* global __*/

function required(params, name) {
    var value = params[name];
    if (value === undefined) {
        throw 'Parameter "' + name + '" is required';
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
 * This function creates a single audit log entry.
 *
 * The parameter 'type' is required and all other parameters are optional.
 *
 * @example-ref examples/auditlog/log.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.type Type of log entry.
 * @param {string} [params.time] Log entry timestamp. Defaults to now.
 * @param {string} [params.source] Log entry source. Defaults to the application ID.
 * @param {string} [params.user] Log entry user. Defaults to the user of current context.
 * @param {array}  [params.objects] URIs to objects that relate to this log entry. Defaults to empty array.
 * @param {object} [params.data] Custom extra data for the this log entry. Defaults to empty object.
 *
 * @returns {object} Audit log created as JSON.
 */
exports.log = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.audit.CreateAuditLogHandler');
    bean.setType(required(params, 'type'));
    bean.setTime(nullOrValue(params.time));
    bean.setSource(params.source === undefined ? app.name : params.source);
    bean.setUser(nullOrValue(params.user));
    bean.setObjectUris(__.toScriptValue(params.objects));
    bean.setData(__.toScriptValue(params.data));

    return __.toNativeObject(bean.execute());
};

/**
 * This function fetches an audit log.
 *
 * @example-ref examples/auditlog/get.js
 *
 * @param {object} params     JSON with the parameters.
 * @param {string} params.id  Id of the audit log.
 *
 * @returns {object} Audit log as JSON.
 */
exports.get = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.audit.GetAuditLogHandler');
    bean.setId(required(params, 'id'));
    return __.toNativeObject(bean.execute());
};

/**
 * This function searches for audit logs.
 *
 * All parameters are semi-optional, meaning that you should at least supply one
 * of them. If no parameters are supplied you will get an empty result.
 *
 * @example-ref examples/auditlog/find.js
 *
 * @param {object} params     JSON with the parameters.
 * @param {number} [params.start=0] Start index (used for paging).
 * @param {number} [params.count=10] Number of contents to fetch.
 * @param {array} [params.ids] Filter by ids of audit logs.
 * @param {string} [params.from] Filter by logs younger than from.
 * @param {string} [params.to] Filter by logs older than to.
 * @param {string} [params.type] Filter by type.
 * @param {string} [params.source] Filter by source.
 * @param {array} [params.users] Filter by user keys.
 * @param {array} [params.objects] Filter by object URIs.
 *
 * @returns {object} Audit log search results.
 */
exports.find = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.audit.FindAuditLogHandler');
    bean.setStart(params.start);
    bean.setCount(params.count);
    bean.setIds(__.toScriptValue(params.ids));
    bean.setFrom(nullOrValue(params.from));
    bean.setTo(nullOrValue(params.to));
    bean.setType(nullOrValue(params.type));
    bean.setSource(nullOrValue(params.source));
    bean.setUsers(__.toScriptValue(params.users));
    bean.setObjectUris(__.toScriptValue(params.objects));
    return __.toNativeObject(bean.execute());
};
