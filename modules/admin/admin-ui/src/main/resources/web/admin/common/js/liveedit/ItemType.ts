module api.liveedit {

    import StringHelper = api.util.StringHelper;

    export class ItemType implements api.Equitable {

        static ATTRIBUTE_TYPE = "portal-component-type";
        static ATTRIBUTE_REGION_NAME = "portal-region";

        private static shortNameToInstance: {[shortName: string]: ItemType} = {};

        private shortName: string;

        private config: ItemTypeConfig;

        constructor(shortName: string) {
            ItemType.shortNameToInstance[shortName] = this;
            this.shortName = shortName;
            this.config = this.getItemTypeConfig(shortName);
        }

        protected getItemTypeConfig(itemType: string): ItemTypeConfig {
            return null;
        }

        getShortName(): string {
            return this.shortName;
        }

        getConfig(): ItemTypeConfig {
            return this.config;
        }


        isComponentType(): boolean {
            return false
        }

        toComponentType(): api.content.page.region.ComponentType {
            api.util.assert(this.isComponentType(), "Not support when ItemType is not a ComponentType");
            return api.content.page.region.ComponentType.byShortName(this.shortName);
        }

        createView(config: CreateItemViewConfig<ItemView,any>): ItemView {
            throw new Error("Must be implemented by inheritors");
        }

        equals(o: api.Equitable): boolean {

            if (!api.ObjectHelper.iFrameSafeInstanceOf(o, ItemType)) {
                return false;
            }

            var other = <ItemType>o;

            if (!api.ObjectHelper.stringEquals(this.shortName, other.shortName)) {
                return false;
            }

            return true;
        }

        static getDraggables(): ItemType[] {
            var draggables: ItemType[] = [];
            for (var shortName in  ItemType.shortNameToInstance) {
                var itemType = ItemType.shortNameToInstance[shortName];
                if (itemType.getConfig().isDraggable()) {
                    draggables.push(itemType);
                }
            }
            return draggables;
        }

        static byShortName(shortName: string): ItemType {
            return ItemType.shortNameToInstance[shortName];
        }

        static fromHTMLElement(element: HTMLElement): ItemType {
            var typeAsString = element.getAttribute("data-" + ItemType.ATTRIBUTE_TYPE);
            if (StringHelper.isBlank(typeAsString)) {
                var regionName = element.getAttribute("data-" + ItemType.ATTRIBUTE_REGION_NAME);
                if (!StringHelper.isBlank(regionName)) {
                    typeAsString = "region";
                }
            }
            return ItemType.byShortName(typeAsString);
        }

        static fromElement(element: api.dom.Element): ItemType {
            return ItemType.fromHTMLElement(element.getHTMLElement());
        }
    }
}