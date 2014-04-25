module api.content.page {

    export class PageTemplateSummary implements api.Equitable {

        private key: PageTemplateKey;

        private displayName: string;

        private descriptorKey: DescriptorKey;

        constructor(builder: PageTemplateSummaryBuilder) {

            this.key = builder.key;
            this.displayName = builder.displayName;
            this.descriptorKey = builder.descriptorKey;
        }

        getKey(): PageTemplateKey {
            return this.key;
        }

        getName(): PageTemplateName {
            return this.key.getTemplateName();
        }

        getDisplayName(): string {
            return this.displayName;
        }

        getDescriptorKey(): DescriptorKey {
            return this.descriptorKey;
        }

        equals(o: api.Equitable): boolean {

            if (!(o instanceof PageTemplateSummary)) {
                return false;
            }

            var other = <PageTemplateSummary>o;

            if (!api.EquitableHelper.equals(this.key, other.key)) {
                return false;
            }

            if (!api.EquitableHelper.stringEquals(this.displayName, other.displayName)) {
                return false;
            }

            if (!api.EquitableHelper.equals(this.descriptorKey, other.descriptorKey)) {
                return false;
            }

            return true;
        }

        static fromJsonArray(jsonArray: PageTemplateSummaryJson[]): PageTemplateSummary[] {
            var array: PageTemplateSummary[] = [];

            jsonArray.forEach((summaryJson: PageTemplateSummaryJson) => {
                array.push(new PageTemplateSummaryBuilder().fromJson(summaryJson).build());
            });
            return array;
        }
    }

    export class PageTemplateSummaryBuilder {

        key: PageTemplateKey;

        displayName: string;

        descriptorKey: DescriptorKey;

        fromJson(json: api.content.page.PageTemplateSummaryJson): PageTemplateSummaryBuilder {

            this.setKey(PageTemplateKey.fromString(json.key));
            this.setDisplayName(json.displayName);
            this.setDescriptorKey(DescriptorKey.fromString(json.descriptorKey));
            return this;
        }

        public setKey(value: PageTemplateKey): PageTemplateSummaryBuilder {
            this.key = value;
            return this;
        }

        public setDisplayName(value: string): PageTemplateSummaryBuilder {
            this.displayName = value;
            return this;
        }

        public setDescriptorKey(value: DescriptorKey): PageTemplateSummaryBuilder {
            this.descriptorKey = value;
            return this;
        }

        public build(): PageTemplateSummary {
            return new PageTemplateSummary(this);
        }
    }
}