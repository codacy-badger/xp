module api.content.site {

    export class ModuleConfig implements api.Equitable {

        private moduleKey: api.module.ModuleKey;

        private config: api.data.RootDataSet;

        constructor(builder: ModuleConfigBuilder) {
            this.moduleKey = builder.moduleKey;
            this.config = builder.config;
        }

        getModuleKey(): api.module.ModuleKey {
            return this.moduleKey;
        }

        getConfig(): api.data.RootDataSet {
            return this.config;
        }

        setConfig(config: api.data.RootDataSet) {
            this.config = config;
        }

        toJson(): Object {
            return {
                moduleKey: this.moduleKey.toString(),
                config: this.config.toJson()
            }
        }

        equals(o: api.Equitable): boolean {

            if (!(o instanceof ModuleConfig)) {
                return false;
            }

            var other = <ModuleConfig>o;

            if (!api.EquitableHelper.equals(this.moduleKey, other.moduleKey)) {
                return false;
            }

            if (!api.EquitableHelper.equals(this.config, other.config)) {
                return false;
            }

            return true;
        }
    }

    export class ModuleConfigBuilder {

        moduleKey: api.module.ModuleKey;

        config: api.data.RootDataSet;

        setFromJson(moduleConfigJson: api.content.site.ModuleConfigJson): ModuleConfigBuilder {
            this.moduleKey = api.module.ModuleKey.fromString(moduleConfigJson.moduleKey);
            this.config = api.data.DataFactory.createRootDataSet(moduleConfigJson.config);
            return this;
        }

        setModuleKey(value: api.module.ModuleKey): ModuleConfigBuilder {
            this.moduleKey = value;
            return this;
        }

        setConfig(value: api.data.RootDataSet): ModuleConfigBuilder {
            this.config = value;
            return this;
        }

        build(): ModuleConfig {
            return new ModuleConfig(this);
        }
    }

}