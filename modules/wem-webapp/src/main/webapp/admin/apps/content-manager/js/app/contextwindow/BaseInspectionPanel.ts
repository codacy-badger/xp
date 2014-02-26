module app.contextwindow {

    export class BaseInspectionPanel extends api.ui.Panel {

        private nameAndIcon: api.app.NamesAndIconView;
        private iconClass: string;

        constructor(iconClass: string) {
            super("inspection-panel");

            this.iconClass = iconClass;
            this.nameAndIcon = new api.app.NamesAndIconView(new api.app.NamesAndIconViewBuilder().
                setSize(api.app.NamesAndIconViewSize.medium)).
                setIconClass(this.iconClass);

            this.appendChild(this.nameAndIcon);

            this.onRendered((event) => {
                $(this.getHTMLElement()).slimScroll({
                    height: '450px'
                });
            })
        }

        setMainName(value: string) {
            this.nameAndIcon.setMainName(value);
        }

        setSubName(value: string) {
            this.nameAndIcon.setSubName(value);
        }
    }
}