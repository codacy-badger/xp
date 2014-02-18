module api.form.layout {

    export class LayoutView extends api.form.FormItemView {

        private layout:api.form.Layout;

        constructor(context: api.form.FormContext, layout:api.form.Layout, parent: api.form.formitemset.FormItemSetOccurrenceView, className:string) {
            super(className, context, layout, parent);

            this.layout = layout;
        }

        getData():api.data.Data[] {
            return null;
        }

    }
}