describe("api.schema.mixin.MixinNamesTest", function () {

    var MixinNames = api.schema.mixin.MixinNames;

    describe("constructor", function () {

        it("given duplicates then Error is thrown", function () {

            expect(function () {

                MixinNames.create().fromStrings(["mymodule:duplicate", "mymodule:duplicate"]).build();

            }).toThrow(new Error("MixinNames do not allow duplicates, found: 'mymodule:duplicate'"));
        });
    });
});