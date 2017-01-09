import "../../../../../../api.ts";
import {PageTemplateOption} from "./PageTemplateOption";

import ContentId = api.content.ContentId;
import ContentTypeName = api.schema.content.ContentTypeName;
import GetPageTemplatesByCanRenderRequest = api.content.page.GetPageTemplatesByCanRenderRequest;
import PageModel = api.content.page.PageModel;
import PageTemplate = api.content.page.PageTemplate;
import PageTemplateLoader = api.content.page.PageTemplateLoader;
import Option = api.ui.selector.Option;
import LoadedDataEvent = api.util.loader.event.LoadedDataEvent;

export class PageTemplateOptions {

    private siteId: ContentId;

    private contentType: ContentTypeName;

    private pageModel: PageModel;

    private defaultPageTemplateOption: Option<PageTemplateOption>;

    constructor(siteId: ContentId, contentType: ContentTypeName, pageModel: PageModel) {
        this.siteId = siteId;
        this.contentType = contentType;
        this.pageModel = pageModel;
        this.defaultPageTemplateOption = {value: "__auto__", displayValue: new PageTemplateOption(null, pageModel)};
    }

    getDefault(): Option<PageTemplateOption> {
        return this.defaultPageTemplateOption;
    }

    getOptions(): wemQ.Promise<Option<PageTemplateOption>[]> {

        let deferred = wemQ.defer<Option<PageTemplateOption>[]>();

        let options: Option<PageTemplateOption>[] = [];
        options.push(this.defaultPageTemplateOption);

        let loader = new PageTemplateLoader(new GetPageTemplatesByCanRenderRequest(this.siteId,
            this.contentType));
        loader.setComparator(new api.content.page.PageTemplateByDisplayNameComparator());
        loader.onLoadedData((event: LoadedDataEvent<PageTemplate>) => {

            event.getData().forEach((pageTemplate: PageTemplate) => {

                let indices: string[] = [];
                indices.push(pageTemplate.getName().toString());
                indices.push(pageTemplate.getDisplayName());
                indices.push(pageTemplate.getController().toString());

                let option = {
                    value: pageTemplate.getId().toString(),
                    displayValue: new PageTemplateOption(pageTemplate, this.pageModel),
                    indices: indices
                };
                options.push(option);
            });

            deferred.resolve(options);
        });
        loader.load();
        return deferred.promise;
    }
}
