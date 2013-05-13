module LiveEdit.model {
    var $ = $liveedit;

    export class Base {
        public cssSelector:string = '';

        constructor() {
        }


        attachMouseOverEvent():void {

            $(document).on('mouseover', this.cssSelector, (event:JQueryEventObject) => {
                var component:JQuery = $(event.currentTarget);
                var targetIsUiComponent = this.isLiveEditUiComponent($(event.target));
                var cancelEvents = targetIsUiComponent || this.hasComponentSelected() || LiveEdit.DragDropSort.isDragging();
                if (cancelEvents) {
                    return;
                }
                event.stopPropagation();

                $(window).trigger('component.mouseOver', [component]);
            });
        }


        attachMouseOutEvent():void {

            $(document).on('mouseout', () => {
                if (this.hasComponentSelected()) {
                    return;
                }
                $(window).trigger('component.mouseOut');
            });
        }


        attachClickEvent() {

            $(document).on('click contextmenu touchstart', this.cssSelector, (event:JQueryEventObject) => {
                // Is this needed? We are using $.on with a delegate so the target would always be a LE component
                if (this.isLiveEditUiComponent($(event.target))) {
                    return;
                }
                event.stopPropagation();
                event.preventDefault();

                var component:JQuery = $(event.currentTarget),
                    componentIsSelected = component.hasClass('live-edit-selected-component'),
                    pageHasComponentSelected = $('.live-edit-selected-component').length > 0;

                if (componentIsSelected || pageHasComponentSelected) {
                    $(window).trigger('component.onDeselect');
                } else {

                    // Used by eg. Menu
                    var pagePosition:any = {
                        x: event.pageX,
                        y: event.pageY
                    };

                    $(window).trigger('component.onSelect', [component, pagePosition]);
                }
            });
        }


        hasComponentSelected() {
            return $('.live-edit-selected-component').length > 0;
        }


        isLiveEditUiComponent(target:JQuery) {
            return target.is('[id*=live-edit-ui-cmp]') || target.parents('[id*=live-edit-ui-cmp]').length > 0;
        }


        getAll() {
            return $(this.cssSelector);
        }

    }
}
