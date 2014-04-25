module api.schema.content {

    export class ContentTypeSummary extends api.schema.Schema implements api.Equitable {

        private allowChildContent: boolean;

        private abstract: boolean;

        private final: boolean;

        private superType: api.schema.content.ContentTypeName;

        private contentDisplayNameScript: string;

        private modifier: string;

        private owner: string;

        constructor(builder: ContentTypeSummaryBuilder) {
            super(builder);
            this.allowChildContent = builder.allowChildContent;
            this.final = builder.final;
            this.abstract = builder.abstract;
            this.superType = builder.superType;
            this.contentDisplayNameScript = builder.contentDisplayNameScript;
            this.owner = builder.owner;
            this.modifier = builder.modifier;
        }

        getContentTypeName(): api.schema.content.ContentTypeName {
            return new api.schema.content.ContentTypeName(this.getName());
        }

        isFinal(): boolean {
            return this.final;
        }

        isAbstract(): boolean {
            return this.abstract;
        }

        isAllowChildContent(): boolean {
            return this.allowChildContent;
        }

        getSuperType(): api.schema.content.ContentTypeName {
            return this.superType;
        }

        hasContentDisplayNameScript(): boolean {
            return !api.util.isStringBlank(this.contentDisplayNameScript);
        }

        getContentDisplayNameScript(): string {
            return this.contentDisplayNameScript;
        }

        getOwner(): string {
            return this.owner;
        }

        getModifier(): string {
            return this.modifier;
        }

        equals(o: api.Equitable): boolean {

            if (!(o instanceof ContentTypeSummary)) {
                return false;
            }

            if (!super.equals(o)) {
                return false;
            }

            var other = <ContentTypeSummary>o;

            if (!EquitableHelper.booleanEquals(this.allowChildContent, other.allowChildContent)) {
                return false;
            }

            if (!EquitableHelper.booleanEquals(this.abstract, other.abstract)) {
                return false;
            }

            if (!EquitableHelper.equals(this.superType, other.superType)) {
                return false;
            }

            if (!EquitableHelper.stringEquals(this.contentDisplayNameScript, other.contentDisplayNameScript)) {
                return false;
            }

            if (!EquitableHelper.stringEquals(this.modifier, other.modifier)) {
                return false;
            }

            if (!EquitableHelper.stringEquals(this.owner, other.owner)) {
                return false;
            }

            return true;
        }

        static fromJsonArray(jsonArray: api.schema.content.json.ContentTypeSummaryJson[]): ContentTypeSummary[] {
            var array: ContentTypeSummary[] = [];

            jsonArray.forEach((summaryJson: api.schema.content.json.ContentTypeSummaryJson) => {
                array.push(ContentTypeSummary.fromJson(summaryJson));
            });
            return array;
        }

        static fromJson(json: api.schema.content.json.ContentTypeSummaryJson): ContentTypeSummary {
            return new ContentTypeSummaryBuilder().fromContentTypeSummaryJson(json).build();
        }

    }

    export class ContentTypeSummaryBuilder extends api.schema.SchemaBuilder {

        allowChildContent: boolean;

        abstract: boolean;

        final: boolean;

        superType: api.schema.content.ContentTypeName;

        contentDisplayNameScript: string;

        modifier: string;

        owner: string;

        constructor(source?: ContentTypeSummary) {
            if (source) {
                super(source);
                this.allowChildContent = source.isAllowChildContent();
                this.abstract = source.isAbstract();
                this.final = source.isFinal();
                this.superType = source.getSuperType();
                this.contentDisplayNameScript = source.getContentDisplayNameScript();
                this.modifier = source.getModifier();
                this.owner = source.getOwner();
            }
        }

        fromContentTypeSummaryJson(json: api.schema.content.json.ContentTypeSummaryJson): ContentTypeSummaryBuilder {
            super.fromSchemaJson(json);

            this.allowChildContent = json.allowChildContent;
            this.final = json.final;
            this.abstract = json.abstract;
            this.superType = new api.schema.content.ContentTypeName(json.superType);
            this.contentDisplayNameScript = json.contentDisplayNameScript;
            this.owner = json.owner;
            this.modifier = json.modifier;
            return this;
        }

        build(): ContentTypeSummary {
            return new ContentTypeSummary(this);
        }
    }
}