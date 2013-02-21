Ext.define('Admin.controller.contentStudio.WizardController', {
    extend: 'Admin.controller.contentStudio.SchemaController',

    /*      Controller for handling Content Type Wizard UI events       */


    stores: [
    ],
    models: [
    ],
    views: [],


    init: function () {
        var me = this;
        me.control({
            'contentStudioWizardPanel *[action=closeWizard]': {
                click: me.closeWizard
            },
            'contentStudioWizardPanel *[action=saveType]': {
                click: function (button, event) {
                    me.saveSchema(button.up('contentStudioWizardPanel'), false, button.schema);
                }
            },
            'contentStudioWizardPanel wizardPanel': {
                finished: function (wizard, data) {
                    me.saveSchema(wizard.up('contentStudioWizardPanel'), true);
                }
            },
            'contentStudioWizardPanel *[action=deleteType]': {
                click: function (button, event) {
                    this.deleteSchema(button.up('contentStudioWizardPanel'), false, button.schema);
                }
            }
        });
    },

    closeWizard: function (el, e) {
        var tab = this.getWizardTab();
        var schemaWizard = this.getWizardPanel();
        if (schemaWizard.getWizardPanel().isWizardDirty) {
            Ext.Msg.confirm('Close wizard', 'There are unsaved changes, do you want to close it anyway ?',
                function (answer) {
                    if ('yes' === answer) {
                        tab.close();
                    }
                });
        } else {
            tab.close();
        }
    },

    saveSchema: function (wizard, closeWizard, schema) {
        switch (schema) {
        case 'contentType':
            this.application.fireEvent('saveContentType', wizard, closeWizard);
            break;
        case 'mixin':
            this.application.fireEvent('saveMixin', wizard, closeWizard);
            break;
        case 'relationshipType':
            this.application.fireEvent('saveRelationshipType', wizard, closeWizard);
            break;
        default:
            return;
        }

    },

    deleteSchema: function (wizard, closeWizard, schema) {
        switch (schema) {
        case 'contentType':
            this.application.fireEvent('deleteContentType', wizard, closeWizard);
            break;
        case 'mixin':
            this.application.fireEvent('deleteMixin', wizard);
            break;
        case 'relationshipType':
            this.application.fireEvent('deleteRelationshipType', wizard, closeWizard);
            break;
        default:
            return;
        }

    },

    /*      Getters     */

    getWizardTab: function () {
        return this.getCmsTabPanel().getActiveTab();
    },

    getWizardPanel: function () {
        return this.getWizardTab();
    }

});
