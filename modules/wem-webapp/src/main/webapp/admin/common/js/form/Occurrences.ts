module api.form {

    export class Occurrences implements api.Equitable {

        private minimum: number;
        private maximum: number;

        constructor(json) {
            this.minimum = json.minimum;
            this.maximum = json.maximum;
        }

        getMaximum(): number {
            return this.maximum;
        }

        getMinimum(): number {
            return this.minimum;
        }

        required(): boolean {
            return this.minimum > 0;
        }

        multiple(): boolean {
            return this.maximum > 1 || this.maximum == 0;
        }

        minimumReached(occurrenceCount: number) {
            return occurrenceCount > this.minimum;
        }

        maximumReached(occurrenceCount: number): boolean {
            if (this.maximum == 0) {
                return false;
            }
            return occurrenceCount >= this.maximum;
        }

        maximumBreached(occurrenceCount: number): boolean {
            if (this.maximum == 0) {
                return false;
            }
            return occurrenceCount > this.maximum;
        }

        public toJson(): api.form.json.OccurrencesJson {

            return <api.form.json.OccurrencesJson>{
                maximum: this.getMaximum(),
                minimum: this.getMinimum()
            };
        }

        equals(o: api.Equitable): boolean {

            if (!(o instanceof Occurrences)) {
                return false;
            }

            var other = <Occurrences>o;

            if (!api.EquitableHelper.numberEquals(this.minimum, other.minimum)) {
                return false;
            }

            if (!api.EquitableHelper.numberEquals(this.maximum, other.maximum)) {
                return false;
            }

            return true;
        }
    }
}