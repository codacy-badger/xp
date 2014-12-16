module app.wizard {

    import User = api.security.User;
    import UserBuilder = api.security.UserBuilder;
    import CreateUserRequest = api.security.CreateUserRequest;
    import UpdateUserRequest = api.security.UpdateUserRequest;

    import Principal = api.security.Principal;
    import PrincipalKey = api.security.PrincipalKey;
    import UserStoreKey = api.security.UserStoreKey;
    import GetPrincipalByKeyRequest = api.security.GetPrincipalByKeyRequest;

    import ConfirmationDialog = api.ui.dialog.ConfirmationDialog;
    import WizardStep = api.app.wizard.WizardStep;

    export class UserWizardPanel extends PrincipalWizardPanel {

        private userEmailWizardStepForm: UserEmailWizardStepForm;
        private userPasswordWizardStepForm: UserPasswordWizardStepForm;
        private userMembershipsWizardStepForm: UserMembershipsWizardStepForm;

        private userStore: UserStoreKey;

        constructor(params: PrincipalWizardPanelParams, callback: (wizard: PrincipalWizardPanel) => void) {

            this.userEmailWizardStepForm = new UserEmailWizardStepForm(params.userStore);
            this.userPasswordWizardStepForm = new UserPasswordWizardStepForm();
            this.userMembershipsWizardStepForm = new UserMembershipsWizardStepForm();
            this.userStore = params.userStore;

            super(params, () => {
                this.addClass("user-wizard-panel");
                callback(this);
            });
        }

        giveInitialFocus() {
            var newWithoutDisplayCameScript = this.isLayingOutNew();

            if (newWithoutDisplayCameScript) {
                this.principalWizardHeader.giveFocus();
            } else if (!this.principalWizardHeader.giveFocus()) {
                this.principalWizardHeader.giveFocus();
            }

            this.startRememberFocus();
        }

        saveChanges(): wemQ.Promise<Principal> {
            // TODO: Add a validation of the password filed, when implemented.
            var formEmail = this.userEmailWizardStepForm.getEmail(),
                email = this.getPersistedItem() ? this.getPersistedItem().asUser().getEmail() : null;
            if (this.userEmailWizardStepForm.isValid() || (formEmail === email)) {
                return super.saveChanges();
            } else {
                var deferred = wemQ.defer<Principal>(),
                    message = "";
                if (!formEmail) {
                    message = "E-mail can not be empty.";
                } else {
                    message = "E-mail is invalid.";
                }
                api.notify.showError(message);
                // deferred.resolve(null);
                deferred.reject(new Error(message));
                return deferred.promise;
            }
        }

        createSteps(): wemQ.Promise<any[]> {
            var deferred = wemQ.defer<WizardStep[]>();

            var steps: WizardStep[] = [];

            steps.push(new WizardStep("User", this.userEmailWizardStepForm));
            steps.push(new WizardStep("Authentication", this.userPasswordWizardStepForm));
            steps.push(new WizardStep("Groups & Roles", this.userMembershipsWizardStepForm));

            this.setSteps(steps);

            deferred.resolve(steps);
            return deferred.promise;
        }

        preLayoutNew(): wemQ.Promise<void> {
            var deferred = wemQ.defer<void>();

            this.doLayoutPersistedItem(null);

            deferred.resolve(null);

            return deferred.promise;
        }

        postLayoutNew(): wemQ.Promise<void> {
            var deferred = wemQ.defer<void>();

            this.principalWizardHeader.initNames("", "", false);

            deferred.resolve(null);
            return deferred.promise;
        }

        layoutPersistedItem(persistedPrincipal: Principal): wemQ.Promise<void> {
            if (!this.constructing) {

                var deferred = wemQ.defer<void>();
                var viewedPrincipal = this.assembleViewedPrincipal();

                if (!this.isPersistedEqualsViewed()) {

                    console.warn("Received Principal from server differs from what's viewed:");
                    console.warn(" viewedPrincipal: ", viewedPrincipal);
                    console.warn(" persistedPrincipal: ", persistedPrincipal);

                    ConfirmationDialog.get().
                        setQuestion("Received Principal from server differs from what you have. Would you like to load changes from server?").
                        setYesCallback(() => this.doLayoutPersistedItem(persistedPrincipal.clone())).
                        setNoCallback(() => {/* Do nothing */
                        }).
                        show();
                }

                deferred.resolve(null);
                return deferred.promise;
            } else {
                return this.doLayoutPersistedItem(persistedPrincipal.clone());
            }
        }

        doLayoutPersistedItem(principal: Principal): wemQ.Promise<void> {
            var parallelPromises: wemQ.Promise<any>[] = [
                // Load attachments?
                this.createSteps()
            ];

            if (!!principal) {
                parallelPromises.push(
                    new GetPrincipalByKeyRequest(this.getPersistedItem().getKey()).
                        includeUserMemberships(true).
                        sendAndParse().
                        then((p: Principal) => {
                            this.getPersistedItem().asUser();
                            this.getPersistedItem().asUser().setMemberships(p.asUser().getMemberships());
                            principal = this.getPersistedItem().asUser().clone();
                        })
                );
            }

            return wemQ.all(parallelPromises).spread<void>(() => {
                this.principalWizardHeader.setDisplayName(principal.getDisplayName());
                this.userEmailWizardStepForm.layout(principal);
                this.userPasswordWizardStepForm.layout(principal);
                this.userMembershipsWizardStepForm.layout(principal);

                return wemQ(null);
            });
        }

        persistNewItem(): wemQ.Promise<Principal> {
            return this.produceCreateUserRequest().sendAndParse().
                then((principal: Principal) => {
                    this.principalWizardHeader.disableNameInput();
                    this.principalWizardHeader.setAutoGenerationEnabled(false);
                    api.notify.showFeedback('User was created!');
                    return principal;
                });
        }

        produceCreateUserRequest(): CreateUserRequest {
            var key = PrincipalKey.ofUser(this.userStore, this.principalWizardHeader.getName()),
                name = this.principalWizardHeader.getDisplayName(),
                email = this.userEmailWizardStepForm.getEmail(),
                login = this.principalWizardHeader.getName(),
                password = this.userPasswordWizardStepForm.getPassword(),
                memberships = this.userMembershipsWizardStepForm.getMemberships().map((el) => {
                    return el.getKey();
                });
            return new CreateUserRequest().setKey(key).
                setDisplayName(name).
                setEmail(email).
                setLogin(login).
                setPassword(password).
                setMemberships(memberships);
        }

        updatePersistedItem(): wemQ.Promise<Principal> {
            return this.produceUpdateUserRequest(this.assembleViewedPrincipal()).
                sendAndParse().
                then((principal: Principal) => {
                    if (!this.getPersistedItem().getDisplayName() && !!principal.getDisplayName()) {
                        this.notifyPrincipalNamed(principal);
                    }
                    this.userEmailWizardStepForm.layout(principal);
                    api.notify.showFeedback('User was updated!');

                    return principal;
                });
        }

        produceUpdateUserRequest(viewedPrincipal: Principal): UpdateUserRequest {
            var user = viewedPrincipal.asUser(),
                key = user.getKey(),
                displayName = user.getDisplayName(),
                email = user.getEmail(),
                login = user.getLogin(),
                oldMemberships = this.getPersistedItem().asUser().getMemberships().map((el) => {
                    return el.getKey();
                }),
                oldMembershipsIds = oldMemberships.map((el) => {
                    return el.getId();
                }),
                newMemberships = user.getMemberships().map((el) => {
                    return el.getKey();
                }),
                newMembershipsIds = newMemberships.map((el) => {
                    return el.getId();
                }),
                addMemberships = newMemberships.filter((el) => {
                    return oldMembershipsIds.indexOf(el.getId()) < 0;
                }),
                removeMemberships = oldMemberships.filter((el) => {
                    return newMembershipsIds.indexOf(el.getId()) < 0;
                });

            return new UpdateUserRequest().
                setKey(key).
                setDisplayName(displayName).
                setEmail(email).
                setLogin(login).
                addMemberships(addMemberships).
                removeMemberships(removeMemberships);
        }

        assembleViewedPrincipal(): Principal {
            return new UserBuilder(!!this.getPersistedItem() ? this.getPersistedItem().asUser() : null).
                setDisplayName(this.principalWizardHeader.getDisplayName()).
                setEmail(this.userEmailWizardStepForm.getEmail()).
                setLogin(this.principalWizardHeader.getName()).
                setMemberships(this.userMembershipsWizardStepForm.getMemberships()).
                // setDisabled().
                build();
        }

        isPersistedEqualsViewed(): boolean {
            var persistedPrincipal = this.getPersistedItem().asUser();
            var viewedPrincipal = this.assembleViewedPrincipal().asUser();
            // Group/User order can be different for viewed and persisted principal
            viewedPrincipal.getMemberships().sort((a, b) => {
                return a.getKey().getId().localeCompare(b.getKey().getId());
            });
            persistedPrincipal.getMemberships().sort((a, b) => {
                return a.getKey().getId().localeCompare(b.getKey().getId());
            });

            // #hack - The newly added members will have different modifiedData
            var viewedMembershipsKeys = viewedPrincipal.getMemberships().map((el) => {
                    return el.getKey()
                }),
                persistedMembershipsKeys = persistedPrincipal.getMemberships().map((el) => {
                    return el.getKey()
                });

            if (api.ObjectHelper.arrayEquals(viewedMembershipsKeys, persistedMembershipsKeys)) {
                viewedPrincipal.setMemberships(persistedPrincipal.getMemberships());
            }

            return viewedPrincipal.equals(persistedPrincipal);
        }

        hasUnsavedChanges(): boolean {
            var persistedPrincipal = this.getPersistedItem();
            if (persistedPrincipal == undefined) {
                return true;
            } else {
                return !this.isPersistedEqualsViewed();
            }
        }
    }
}