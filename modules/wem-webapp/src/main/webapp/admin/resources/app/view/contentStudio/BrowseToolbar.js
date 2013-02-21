Ext.define('Admin.view.contentStudio.BrowseToolbar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.contentStudio.browseToolbar',

    cls: 'admin-toolbar',

    defaults: {
        scale: 'medium',
        iconAlign: 'top'
    },

    initComponent: function () {
        this.items = [

            {
                text: 'New',
                action: 'newSchema'
            },
            {
                text: 'Edit',
                disabled: true,
                action: 'editSchema'
            },
            {
                text: 'Open',
                disabled: true,
                action: 'viewContentType'
            },
            {
                text: 'Delete',
                disabled: true,
                action: 'deleteSchema'
            },
            {
                text: 'Re-index',
                action: 'reindexContentTypes'
            },

            {
                text: 'Export',
                action: 'exportContentTypes'
            }
        ];

        this.callParent(arguments);
    }
});
