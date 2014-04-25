module api.content {

    export class ContentSummary extends ContentIdBaseItem implements api.node.Node, api.Equitable {

        private id: string;

        private name: ContentName;

        private displayName: string;

        private path: ContentPath;

        private root: boolean;

        private children: boolean;

        private type: api.schema.content.ContentTypeName;

        private iconUrl: string;

        private modifier: string;

        private owner: string;

        private site: boolean;

        private page: boolean;

        private embedded: boolean;

        private draft: boolean;

        private createdTime: Date;

        private modifiedTime: Date;

        private deletable: boolean;

        private editable: boolean;

        constructor(builder: ContentSummaryBuilder) {
            super(builder);
            this.name = builder.name;
            this.displayName = builder.displayName;
            this.path = builder.path;
            this.root = builder.root;
            this.children = builder.children;
            this.type = builder.type;
            this.iconUrl = builder.iconUrl;
            this.modifier = builder.modifier;
            this.owner = builder.owner;
            this.site = builder.site;
            this.page = builder.page;
            this.embedded = builder.embedded;
            this.draft = builder.draft;

            this.id = builder.id;
            this.createdTime = builder.createdTime;
            this.modifiedTime = builder.modifiedTime;
            this.deletable = builder.deletable;
            this.editable = builder.editable;
        }

        getName(): ContentName {
            return this.name;
        }

        getDisplayName(): string {
            return this.displayName;
        }

        hasParent(): boolean {
            return this.path.hasParentContent();
        }

        getPath(): ContentPath {
            return this.path;
        }

        isRoot(): boolean {
            return this.root;
        }

        hasChildren(): boolean {
            return this.children;
        }

        getType(): api.schema.content.ContentTypeName {
            return this.type;
        }

        getIconUrl(): string {
            return this.iconUrl;
        }

        getOwner(): string {
            return this.owner;
        }

        getModifier(): string {
            return this.modifier;
        }

        isSite(): boolean {
            return this.site;
        }

        isPage(): boolean {
            return this.page;
        }

        isEmbedded(): boolean {
            return this.embedded;
        }

        isDraft(): boolean {
            return this.draft;
        }

        getId(): string {
            return this.id;
        }

        getCreatedTime(): Date {
            return this.createdTime;
        }

        getModifiedTime(): Date {
            return this.modifiedTime;
        }

        isDeletable(): boolean {
            return this.deletable;
        }

        isEditable(): boolean {
            return this.editable;
        }

        equals(o: api.Equitable): boolean {

            if (!(o instanceof ContentSummary)) {
                return false;
            }

            if (!super.equals(o)) {
                return false;
            }

            var other = <ContentSummary>o;

            if (!api.EquitableHelper.stringEquals(this.id, other.id)) {
                return false;
            }
            if (!api.EquitableHelper.equals(this.name, other.name)) {
                return false;
            }
            if (!api.EquitableHelper.stringEquals(this.displayName, other.displayName)) {
                return false;
            }
            if (!api.EquitableHelper.equals(this.path, other.path)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.root, other.root)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.children, other.children)) {
                return false;
            }
            if (!api.EquitableHelper.equals(this.type, other.type)) {
                return false;
            }
            if (!api.EquitableHelper.stringEquals(this.iconUrl, other.iconUrl)) {
                return false;
            }
            if (!api.EquitableHelper.stringEquals(this.modifier, other.modifier)) {
                return false;
            }
            if (!api.EquitableHelper.stringEquals(this.owner, other.owner)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.site, other.site)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.page, other.page)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.embedded, other.embedded)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.draft, other.draft)) {
                return false;
            }
            if (!api.EquitableHelper.dateEquals(this.createdTime, other.createdTime)) {
                return false;
            }
            if (!api.EquitableHelper.dateEquals(this.modifiedTime, other.modifiedTime)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.deletable, other.deletable)) {
                return false;
            }
            if (!api.EquitableHelper.booleanEquals(this.editable, other.editable)) {
                return false;
            }
            return true;
        }

        static fromJson(json: api.content.json.ContentSummaryJson): ContentSummary {
            return new ContentSummaryBuilder().fromContentSummaryJson(json).build();
        }

        static fromJsonArray(jsonArray: api.content.json.ContentSummaryJson[]): ContentSummary[] {
            var array: ContentSummary[] = [];
            jsonArray.forEach((json: api.content.json.ContentSummaryJson) => {
                array.push(ContentSummary.fromJson(json));
            });
            return array;
        }
    }

    export class ContentSummaryBuilder extends ContentIdBaseItemBuilder {

        id: string;

        name: ContentName;

        displayName: string;

        path: ContentPath;

        root: boolean;

        children: boolean;

        type: api.schema.content.ContentTypeName;

        iconUrl: string;

        modifier: string;

        owner: string;

        site: boolean;

        page: boolean;

        embedded: boolean;

        draft: boolean;

        createdTime: Date;

        modifiedTime: Date;

        deletable: boolean;

        editable: boolean;

        constructor(source?: ContentSummary) {
            super(source);
            if (source) {
                this.id = source.getId();
                this.name = source.getName();
                this.displayName = source.getDisplayName();
                this.path = source.getPath();
                this.root = source.isRoot();
                this.children = source.hasChildren();
                this.type = source.getType();
                this.iconUrl = source.getIconUrl();
                this.modifier = source.getModifier();
                this.owner = source.getOwner();
                this.site = source.isSite();
                this.page = source.isPage();
                this.embedded = source.isEmbedded();
                this.draft = source.isDraft();
                this.createdTime = source.getCreatedTime();
                this.modifiedTime = source.getModifiedTime();
                this.deletable = source.isDeletable();
                this.editable = source.isEditable();
            }
        }

        fromContentSummaryJson(json: api.content.json.ContentSummaryJson): ContentSummaryBuilder {

            super.fromContentIdBaseItemJson(json);

            this.name = ContentName.fromString(json.name);
            this.displayName = json.displayName;
            this.path = ContentPath.fromString(json.path);
            this.root = json.isRoot;
            this.children = json.hasChildren;
            this.type = new api.schema.content.ContentTypeName(json.type);
            this.iconUrl = json.iconUrl;
            this.modifier = json.modifier;
            this.owner = json.owner;
            this.site = json.isSite;
            this.page = json.isPage;
            this.embedded = json.isEmbedded;
            this.draft = json.draft;

            this.id = json.id;
            this.createdTime = json.createdTime ? new Date(Date.parse(json.createdTime)) : null;
            this.modifiedTime = json.modifiedTime ? new Date(Date.parse(json.modifiedTime)) : null;

            this.deletable = json.deletable;
            this.editable = json.editable;

            return this;
        }

        build(): ContentSummary {
            return new ContentSummary(this);
        }
    }
}