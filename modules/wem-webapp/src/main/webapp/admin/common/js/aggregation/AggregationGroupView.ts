module api.aggregation {

    export class AggregationGroupView extends api.dom.DivEl {

        private name: string;

        private aggregationViews: api.aggregation.AggregationView[] = [];

        private titleEl = new api.dom.H2El();

        private bucketSelectionChangedListeners: Function[] = [];

        private handleAggregationFilter: (aggregation: api.aggregation.Aggregation) => boolean;

        constructor(name: string, aggregations?: api.aggregation.Aggregation[],
                    handleAggregationFilter?: (aggregation: api.aggregation.Aggregation) => boolean) {
            super("facet-group-view");

            this.name = name;
            this.handleAggregationFilter = handleAggregationFilter;

            this.titleEl.getEl().setInnerHtml(this.name);
            this.appendChild(this.titleEl);

            if (aggregations) {
                aggregations.forEach((aggregation: api.aggregation.Aggregation) => {
                    this.addAggregationView(api.aggregation.AggregationView.createAggregationView(aggregation, this));
                });
            }
        }

        private addAggregationView(aggregationView: api.aggregation.AggregationView) {
            this.appendChild(aggregationView);

            aggregationView.addBucketViewSelectionChangedEventListener((event: api.aggregation.BucketViewSelectionChangedEvent) => {
                    this.notifyBucketViewSelectionChangedEventChanged(event);
                }
            );

            this.aggregationViews.push(aggregationView);
        }

        /*
         * Override this method to give other criteria for this group to display given facet.
         */
        handlesAggregation(aggregation: api.aggregation.Aggregation) {

            if (this.handleAggregationFilter) {
                return this.handleAggregationFilter(aggregation);
            }
            else {
                return aggregation.getName() == this.name;
            }
        }

        getSelectedValuesByAggregationName(): { [s : string ] : string[];
        } {

            var values: {[s:string] : string[];
            } = {};

            this.aggregationViews.forEach((termsAggregationView: api.aggregation.TermsAggregationView) => {
                values[termsAggregationView.getName()] = termsAggregationView.getSelectedValues();
            });

            return values;
        }

        hasSelections(): boolean {
            var hasSelections = false;
            for (var i = 0; i < this.aggregationViews.length; i++) {
                if (this.aggregationViews[i].hasSelectedEntry()) {
                    hasSelections = true;
                    break;
                }
            }
            return hasSelections;
        }

        deselectGroup(supressEvent?: boolean) {
            this.aggregationViews.forEach((aggregationView: api.aggregation.AggregationView) => {
                aggregationView.deselectFacet(supressEvent);
            });
        }

        addBucketViewSelectionChangedEventListener(listener: (event: api.aggregation.BucketViewSelectionChangedEvent) => void) {
            this.bucketSelectionChangedListeners.push(listener);
        }

        removeBucketViewSelectionChangedEventListener(listener: (event: api.aggregation.BucketViewSelectionChangedEvent) => void) {
            this.bucketSelectionChangedListeners = this.bucketSelectionChangedListeners.filter(function (curr) {
                return curr != listener;
            });
        }

        notifyBucketViewSelectionChangedEventChanged(event: api.aggregation.BucketViewSelectionChangedEvent) {

            this.bucketSelectionChangedListeners.forEach((listener: (event: BucketViewSelectionChangedEvent) => void) => {
                listener(event);
            });
        }

        update(aggregations: api.aggregation.Aggregation[]) {

            aggregations.forEach((aggregation: api.aggregation.Aggregation) => {

                var existingAggregationView: api.aggregation.AggregationView = this.getAggregationView(aggregation.getName());

                if (existingAggregationView == null) {
                    this.addAggregationView(api.aggregation.AggregationView.createAggregationView(aggregation, this));
                }
                else {
                    if (existingAggregationView instanceof api.aggregation.TermsAggregationView) {

                        var termsAggregationView: api.aggregation.TermsAggregationView = <api.aggregation.TermsAggregationView>existingAggregationView;
                        termsAggregationView.update(aggregation);

                    }
                    // else if (existingFacetView instanceof QueryFacetView) {
                    //     var queryFacetView: QueryFacetView = <QueryFacetView>existingFacetView;
                    //     queryFacetView.update(facet);
                    // }
                }
            });
        }

        private getAggregationView(name: string): api.aggregation.AggregationView {

            for (var i = 0; i < this.aggregationViews.length; i++) {
                var aggregationView: api.aggregation.AggregationView = this.aggregationViews[i];
                if (aggregationView.getName() == name) {
                    return aggregationView;
                }
            }
            return null;
        }
    }

}