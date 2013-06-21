module app_browse {

    export class NewContentAction extends api_ui.Action {

        constructor() {
            super("New");
            this.addExecutionListener(() => {
                new app_event.NewContentEvent().fire();
            });
        }
    }

    export class OpenContentAction extends api_ui.Action {

        constructor() {
            super("Open");
            this.setEnabled(false);
            this.addExecutionListener(() => {
                new app_event.OpenContentEvent(app.ContentContext.get().getSelectedContents()).fire();
            });
        }
    }

    export class EditContentAction extends api_ui.Action {

        constructor() {
            super("Edit");
            this.setEnabled(false);
            this.addExecutionListener(() => {
                new app_event.EditContentEvent(app.ContentContext.get().getSelectedContents()).fire();
            });
        }
    }

    export class DeleteContentAction extends api_ui.Action {

        constructor() {
            super("Delete", "mod+del");
            this.setEnabled(false);
            this.addExecutionListener(() => {
                new app_event.DeleteContentEvent(app.ContentContext.get().getSelectedContents()).fire();
            });
        }
    }

    export class DuplicateContentAction extends api_ui.Action {

        constructor() {
            super("Duplicate");
            this.setEnabled(false);
            this.addExecutionListener(() => {
                console.log('TODO: Duplicate content');
            });
        }
    }

    export class MoveContentAction extends api_ui.Action {

        constructor() {
            super("Move");
            this.setEnabled(false);
            this.addExecutionListener(() => {
                console.log('TODO: Move content');
            });
        }
    }

    export class BrowseContentSettingsAction extends api_ui.Action {

        constructor() {
            super("");
            this.setEnabled(true);
            this.setIconClass('icon-toolbar-settings');
            this.addExecutionListener(() => {
                console.log('TODO: browse content settings');
            });
        }
    }

    export class ContentBrowseActions {

        static NEW_CONTENT:api_ui.Action = new NewContentAction();
        static OPEN_CONTENT:api_ui.Action = new OpenContentAction;
        static EDIT_CONTENT:api_ui.Action = new EditContentAction();
        static DELETE_CONTENT:api_ui.Action = new DeleteContentAction();
        static DUPLICATE_CONTENT:api_ui.Action = new DuplicateContentAction();
        static MOVE_CONTENT:api_ui.Action = new MoveContentAction();
        static BROWSE_CONTENT_SETTINGS:api_ui.Action = new BrowseContentSettingsAction();

        static init() {

            app_event.GridSelectionChangeEvent.on((event) => {

                var contents:api_model.ContentModel[] = event.getModels();

                if (contents.length <= 0) {
                    NEW_CONTENT.setEnabled(true);
                    OPEN_CONTENT.setEnabled(false);
                    EDIT_CONTENT.setEnabled(false);
                    DELETE_CONTENT.setEnabled(false);
                    DUPLICATE_CONTENT.setEnabled(false);
                    MOVE_CONTENT.setEnabled(false);
                }
                else if (contents.length == 1) {
                    NEW_CONTENT.setEnabled(false);
                    OPEN_CONTENT.setEnabled(true);
                    EDIT_CONTENT.setEnabled(contents[0].data.editable);
                    DELETE_CONTENT.setEnabled(contents[0].data.deletable);
                    DUPLICATE_CONTENT.setEnabled(true);
                    MOVE_CONTENT.setEnabled(true);
                }
                else {
                    NEW_CONTENT.setEnabled(false);
                    OPEN_CONTENT.setEnabled(true);
                    EDIT_CONTENT.setEnabled(anyEditable(contents));
                    DELETE_CONTENT.setEnabled(anyDeleteable(contents));
                    DUPLICATE_CONTENT.setEnabled(true);
                    MOVE_CONTENT.setEnabled(true);
                }
            });
        }

        static anyEditable(contents:api_model.ContentModel[]):bool {
            for (var i in contents) {
                var content:api_model.ContentModel = contents[i];
                if (content.data.editable) {
                    return true;
                }
            }
            return false;
        }

        static anyDeleteable(contents:api_model.ContentModel[]):bool {
            for (var i in contents) {
                var content:api_model.ContentModel = contents[i];
                if (content.data.deletable) {
                    return true;
                }
            }
            return false;
        }
    }
}
