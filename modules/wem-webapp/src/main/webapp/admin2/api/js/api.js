var API;
(function (API) {
    (function (util) {
        util.baseUri = '../../..';
        function getAbsoluteUri(uri) {
            return this.baseUri + '/' + uri;
        }
        util.getAbsoluteUri = getAbsoluteUri;
    })(API.util || (API.util = {}));
    var util = API.util;
})(API || (API = {}));
var API_action;
(function (API_action) {
    var Action = (function () {
        function Action(label) {
            this.enabled = true;
            this.executionListeners = [];
            this.propertyChangeListeners = [];
            this.label = label;
        }
        Action.prototype.getLabel = function () {
            return this.label;
        };
        Action.prototype.setLabel = function (value) {
            this.label = value;
        };
        Action.prototype.isEnabled = function () {
            return this.enabled;
        };
        Action.prototype.setEnabled = function (value) {
            this.enabled = value;
            for(var i in this.propertyChangeListeners) {
                this.propertyChangeListeners[i](this);
            }
        };
        Action.prototype.execute = function () {
            for(var i in this.executionListeners) {
                this.executionListeners[i](this);
            }
        };
        Action.prototype.addExecutionListener = function (listener) {
            this.executionListeners.push(listener);
        };
        Action.prototype.addPropertyChangeListener = function (listener) {
            this.propertyChangeListeners.push(listener);
        };
        return Action;
    })();
    API_action.Action = Action;    
})(API_action || (API_action = {}));
var API_ui_toolbar;
(function (API_ui_toolbar) {
    var Button = (function () {
        function Button(action) {
            var _this = this;
            this.action = action;
            this.id = 'button-' + ++Button.counstructorCounter;
            action.addPropertyChangeListener(function (action) {
                _this.enable(action.isEnabled());
            });
        }
        Button.counstructorCounter = 0;
        Button.prototype.enable = function (isEnabled) {
            if(isEnabled) {
                this.element.className = 'enabled';
            } else {
                this.element.className = 'disabled';
            }
        };
        Button.prototype.toHTML = function () {
            var html = '';
            html += '<button id="' + this.id + '">';
            html += this.action.getLabel();
            html += '</button>';
            return html;
        };
        Button.prototype.afterRender = function () {
            var _this = this;
            this.element = document.getElementById(this.id);
            this.element.addEventListener('click', function () {
                _this.action.execute();
            });
        };
        return Button;
    })();
    API_ui_toolbar.Button = Button;    
})(API_ui_toolbar || (API_ui_toolbar = {}));
var API_ui_toolbar;
(function (API_ui_toolbar) {
    var Toolbar = (function () {
        function Toolbar(actions) {
            this.buttons = [];
            for(var i in actions) {
                this.addAction(actions[i]);
            }
            this.init();
        }
        Toolbar.prototype.init = function () {
            this.ext = new Ext.Component({
                html: this.toHtml(),
                region: 'north'
            });
        };
        Toolbar.prototype.toHtml = function () {
            var html = '';
            html += '<div id="toolbar">';
            for(var i in this.buttons) {
                html += this.buttons[i].toHTML();
            }
            html += '</div>';
            return html;
        };
        Toolbar.prototype.add = function (action) {
            var btn = this.addAction(action);
            this.element.innerHTML += btn.toHTML();
            btn.afterRender();
        };
        Toolbar.prototype.afterRender = function () {
            this.element = document.getElementById('toolbar');
            for(var i in this.buttons) {
                this.buttons[i].afterRender();
            }
        };
        Toolbar.prototype.addAction = function (action) {
            var button = new API_ui_toolbar.Button(action);
            this.buttons.push(button);
            return button;
        };
        return Toolbar;
    })();
    API_ui_toolbar.Toolbar = Toolbar;    
})(API_ui_toolbar || (API_ui_toolbar = {}));
var API;
(function (API) {
    (function (event) {
        var Event = (function () {
            function Event(name) {
                this.name = name;
            }
            Event.prototype.getName = function () {
                return this.name;
            };
            Event.prototype.fire = function () {
                event.fireEvent(this);
            };
            return Event;
        })();
        event.Event = Event;        
    })(API.event || (API.event = {}));
    var event = API.event;
})(API || (API = {}));
var API;
(function (API) {
    (function (event) {
        var bus = new Ext.util.Observable({
        });
        function onEvent(name, handler) {
            bus.on(name, handler);
        }
        event.onEvent = onEvent;
        function fireEvent(event) {
            bus.fireEvent(event.getName(), event);
        }
        event.fireEvent = fireEvent;
    })(API.event || (API.event = {}));
    var event = API.event;
})(API || (API = {}));
var API;
(function (API) {
    (function (notify) {
        (function (Type) {
            Type._map = [];
            Type._map[0] = "INFO";
            Type.INFO = 0;
            Type._map[1] = "ERROR";
            Type.ERROR = 1;
            Type._map[2] = "ACTION";
            Type.ACTION = 2;
        })(notify.Type || (notify.Type = {}));
        var Type = notify.Type;
        var Action = (function () {
            function Action(name, handler) {
                this.name = name;
                this.handler = handler;
            }
            Action.prototype.getName = function () {
                return this.name;
            };
            Action.prototype.getHandler = function () {
                return this.handler;
            };
            return Action;
        })();
        notify.Action = Action;        
        var Message = (function () {
            function Message(type, text) {
                this.type = type;
                this.text = text;
                this.actions = [];
            }
            Message.prototype.getType = function () {
                return this.type;
            };
            Message.prototype.getText = function () {
                return this.text;
            };
            Message.prototype.getActions = function () {
                return this.actions;
            };
            Message.prototype.addAction = function (name, handler) {
                this.actions.push(new Action(name, handler));
            };
            Message.prototype.send = function () {
                notify.sendNotification(this);
            };
            return Message;
        })();
        notify.Message = Message;        
        function newInfo(text) {
            return new Message(Type.INFO, text);
        }
        notify.newInfo = newInfo;
        function newError(text) {
            return new Message(Type.ERROR, text);
        }
        notify.newError = newError;
        function newAction(text) {
            return new Message(Type.ACTION, text);
        }
        notify.newAction = newAction;
    })(API.notify || (API.notify = {}));
    var notify = API.notify;
})(API || (API = {}));
var API;
(function (API) {
    (function (notify) {
        var space = 3;
        var lifetime = 5000;
        var slideDuration = 1000;
        var templates = {
            manager: new Ext.Template('<div class="admin-notification-container">', '   <div class="admin-notification-wrapper"></div>', '</div>'),
            notify: new Ext.Template('<div class="admin-notification" style="height: 0; opacity: 0;">', '   <div class="admin-notification-inner">', '       <a class="admin-notification-remove" href="#">X</a>', '       <div class="admin-notification-content">{message}</div>', '   </div>', '</div>')
        };
        var NotifyManager = (function () {
            function NotifyManager() {
                this.timers = {
                };
                this.render();
            }
            NotifyManager.prototype.render = function () {
                var template = templates.manager;
                var node = template.append(Ext.getBody());
                this.el = Ext.get(node);
                this.el.setStyle('bottom', 0);
                this.getWrapperEl().setStyle({
                    margin: 'auto'
                });
            };
            NotifyManager.prototype.getWrapperEl = function () {
                return this.el.first('.admin-notification-wrapper');
            };
            NotifyManager.prototype.notify = function (message) {
                var opts = notify.buildOpts(message);
                this.doNotify(opts);
            };
            NotifyManager.prototype.doNotify = function (opts) {
                var _this = this;
                var notificationEl = this.renderNotification(opts);
                var height = getInnerEl(notificationEl).getHeight();
                this.setListeners(notificationEl, opts);
                notificationEl.animate({
                    duration: slideDuration,
                    to: {
                        height: height + space,
                        opacity: 1
                    },
                    callback: function () {
                        _this.timers[notificationEl.id] = {
                            remainingTime: lifetime
                        };
                        _this.startTimer(notificationEl);
                    }
                });
            };
            NotifyManager.prototype.setListeners = function (el, opts) {
                var _this = this;
                el.on({
                    'click': {
                        fn: function () {
                            _this.remove(el);
                        },
                        stopEvent: true
                    },
                    'mouseover': function () {
                        _this.stopTimer(el);
                    },
                    'mouseleave': function () {
                        _this.startTimer(el);
                    }
                });
                if(opts.listeners) {
                    Ext.each(opts.listeners, function (listener) {
                        el.on({
                            'click': listener
                        });
                    });
                }
            };
            NotifyManager.prototype.remove = function (el) {
                if(!el) {
                    return;
                }
                el.animate({
                    duration: slideDuration,
                    to: {
                        height: 0,
                        opacity: 0
                    },
                    callback: function () {
                        Ext.removeNode(el.dom);
                    }
                });
                delete this.timers[el.id];
            };
            NotifyManager.prototype.startTimer = function (el) {
                var _this = this;
                var timer = this.timers[el.id];
                if(!timer) {
                    return;
                }
                timer.id = setTimeout(function () {
                    _this.remove(el);
                }, timer.remainingTime);
                timer.startTime = Date.now();
            };
            NotifyManager.prototype.stopTimer = function (el) {
                var timer = this.timers[el.id];
                if(!timer || !timer.id) {
                    return;
                }
                clearTimeout(timer.id);
                timer.id = null;
                timer.remainingTime -= Date.now() - timer.startTime;
            };
            NotifyManager.prototype.renderNotification = function (opts) {
                var style = {
                };
                var template = templates.notify;
                var notificationEl = template.append(this.getWrapperEl(), opts, true);
                if(opts.backgroundColor) {
                    style['backgroundColor'] = opts.backgroundColor;
                }
                style['marginTop'] = space + 'px';
                getInnerEl(notificationEl).setStyle(style);
                return notificationEl;
            };
            return NotifyManager;
        })();
        notify.NotifyManager = NotifyManager;        
        function getInnerEl(notificationEl) {
            return notificationEl.down('.admin-notification-inner');
        }
        var manager = new NotifyManager();
        function sendNotification(message) {
            manager.notify(message);
        }
        notify.sendNotification = sendNotification;
    })(API.notify || (API.notify = {}));
    var notify = API.notify;
})(API || (API = {}));
var API;
(function (API) {
    (function (notify) {
        var NotifyOpts = (function () {
            function NotifyOpts() { }
            return NotifyOpts;
        })();
        notify.NotifyOpts = NotifyOpts;        
        function buildOpts(message) {
            var opts = new NotifyOpts();
            if(message.getType() == notify.Type.ERROR) {
                opts.backgroundColor = 'red';
            } else if(message.getType() == notify.Type.ACTION) {
                opts.backgroundColor = '#669c34';
            }
            createHtmlMessage(message, opts);
            addListeners(message, opts);
            return opts;
        }
        notify.buildOpts = buildOpts;
        function addListeners(message, opts) {
            opts.listeners = [];
            var actions = message.getActions();
            for(var i = 0; i < actions.length; i++) {
                opts.listeners.push({
                    fn: actions[i].getHandler(),
                    delegate: 'notify_action_' + i,
                    stopEvent: true
                });
            }
        }
        function createHtmlMessage(message, opts) {
            var actions = message.getActions();
            opts.message = '<span>' + message.getText() + '</span>';
            if(actions.length > 0) {
                var linkHtml = '<span style="float: right; margin-left: 30px;">';
                for(var i = 0; i < actions.length; i++) {
                    if((i > 0) && (i == (actions.length - 1))) {
                        linkHtml += ' or ';
                    } else if(i > 0) {
                        linkHtml += ', ';
                    }
                    linkHtml += '<a href="#" class="notify_action_"' + i + '">';
                    linkHtml += actions[i].getName() + "</a>";
                }
                linkHtml += '</span>';
                opts.message = linkHtml + opts.message;
            }
        }
    })(API.notify || (API.notify = {}));
    var notify = API.notify;
})(API || (API = {}));
var API;
(function (API) {
    (function (notify) {
        function showFeedback(message) {
            notify.newInfo(message).send();
        }
        notify.showFeedback = showFeedback;
        function updateAppTabCount(appId, tabCount) {
        }
        notify.updateAppTabCount = updateAppTabCount;
    })(API.notify || (API.notify = {}));
    var notify = API.notify;
})(API || (API = {}));
var API_content_data;
(function (API_content_data) {
    var DataId = (function () {
        function DataId(name, arrayIndex) {
            this.name = name;
            this.arrayIndex = arrayIndex;
            if(arrayIndex > 0) {
                this.refString = name + '[' + arrayIndex + ']';
            } else {
                this.refString = name;
            }
        }
        DataId.prototype.getName = function () {
            return this.name;
        };
        DataId.prototype.getArrayIndex = function () {
            return this.arrayIndex;
        };
        DataId.prototype.toString = function () {
            return this.refString;
        };
        DataId.from = function from(str) {
            var endsWithEndBracket = str.indexOf(']', str.length - ']'.length) !== -1;
            var containsStartBracket = str.indexOf('[') !== -1;
            if(endsWithEndBracket && containsStartBracket) {
                var firstBracketPos = str.indexOf('[');
                var nameStr = str.substring(0, firstBracketPos);
                var indexStr = str.substring(nameStr.length + 1, (str.length - 1));
                var index = parseInt(indexStr);
                return new DataId(nameStr, index);
            } else {
                return new DataId(str, 0);
            }
        };
        return DataId;
    })();
    API_content_data.DataId = DataId;    
})(API_content_data || (API_content_data = {}));
var API_content_data;
(function (API_content_data) {
    var Data = (function () {
        function Data(name) {
            this.name = name;
        }
        Data.prototype.setArrayIndex = function (value) {
            this.arrayIndex = value;
        };
        Data.prototype.setParent = function (parent) {
            this.parent = parent;
        };
        Data.prototype.getId = function () {
            return new API_content_data.DataId(this.name, this.arrayIndex);
        };
        Data.prototype.getName = function () {
            return this.name;
        };
        Data.prototype.getParent = function () {
            return this.parent;
        };
        Data.prototype.getArrayIndex = function () {
            return this.arrayIndex;
        };
        return Data;
    })();
    API_content_data.Data = Data;    
})(API_content_data || (API_content_data = {}));
var __extends = this.__extends || function (d, b) {
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
var API_content_data;
(function (API_content_data) {
    var DataSet = (function (_super) {
        __extends(DataSet, _super);
        function DataSet(name) {
                _super.call(this, name);
            this.dataById = {
            };
        }
        DataSet.prototype.nameCount = function (name) {
            var count = 0;
            for(var i in this.dataById) {
                var data = this.dataById[i];
                if(data.getName() === name) {
                    count++;
                }
            }
            return count;
        };
        DataSet.prototype.addData = function (data) {
            data.setParent(this);
            var index = this.nameCount(data.getName());
            data.setArrayIndex(index);
            var dataId = new API_content_data.DataId(data.getName(), index);
            this.dataById[dataId.toString()] = data;
        };
        DataSet.prototype.getData = function (dataId) {
            return this.dataById[API_content_data.DataId.from(dataId).toString()];
        };
        return DataSet;
    })(API_content_data.Data);
    API_content_data.DataSet = DataSet;    
})(API_content_data || (API_content_data = {}));
var API_content_data;
(function (API_content_data) {
    var ContentData = (function (_super) {
        __extends(ContentData, _super);
        function ContentData() {
                _super.call(this, "");
        }
        return ContentData;
    })(API_content_data.DataSet);
    API_content_data.ContentData = ContentData;    
})(API_content_data || (API_content_data = {}));
var API_content_data;
(function (API_content_data) {
    var Property = (function (_super) {
        __extends(Property, _super);
        function Property(name, value, type) {
                _super.call(this, name);
            this.value = value;
            this.type = type;
        }
        Property.from = function from(json) {
            return new Property(json.name, json.value, json.type);
        };
        Property.prototype.getValue = function () {
            return this.value;
        };
        Property.prototype.getType = function () {
            return this.type;
        };
        Property.prototype.setValue = function (value) {
            this.value = value;
        };
        return Property;
    })(API_content_data.Data);
    API_content_data.Property = Property;    
})(API_content_data || (API_content_data = {}));
var API_schema_content_form;
(function (API_schema_content_form) {
    var FormItem = (function () {
        function FormItem(name) {
            this.name = name;
        }
        FormItem.prototype.getName = function () {
            return this.name;
        };
        return FormItem;
    })();
    API_schema_content_form.FormItem = FormItem;    
})(API_schema_content_form || (API_schema_content_form = {}));
var API_schema_content_form;
(function (API_schema_content_form) {
    var InputType = (function () {
        function InputType(json) {
            this.name = json.name;
        }
        InputType.prototype.getName = function () {
            return this.name;
        };
        return InputType;
    })();
    API_schema_content_form.InputType = InputType;    
})(API_schema_content_form || (API_schema_content_form = {}));
var API_schema_content_form;
(function (API_schema_content_form) {
    var Input = (function (_super) {
        __extends(Input, _super);
        function Input(json) {
                _super.call(this, json.name);
            this.inputType = new API_schema_content_form.InputType(json.type);
            this.label = json.label;
            this.immutable = json.immutable;
            this.occurrences = new API_schema_content_form.Occurrences(json.occurrences);
            this.indexed = json.indexed;
            this.customText = json.customText;
            this.validationRegex = json.validationRegexp;
            this.helpText = json.helpText;
        }
        Input.prototype.getLabel = function () {
            return this.label;
        };
        Input.prototype.isImmutable = function () {
            return this.immutable;
        };
        Input.prototype.getOccurrences = function () {
            return this.occurrences;
        };
        Input.prototype.isIndexed = function () {
            return this.indexed;
        };
        Input.prototype.getCustomText = function () {
            return this.customText;
        };
        Input.prototype.getValidationRegex = function () {
            return this.validationRegex;
        };
        Input.prototype.getHelpText = function () {
            return this.helpText;
        };
        return Input;
    })(API_schema_content_form.FormItem);
    API_schema_content_form.Input = Input;    
})(API_schema_content_form || (API_schema_content_form = {}));
var API_schema_content_form;
(function (API_schema_content_form) {
    var Occurrences = (function () {
        function Occurrences(json) {
            this.minimum = json.minimum;
            this.maximum = json.maximum;
        }
        return Occurrences;
    })();
    API_schema_content_form.Occurrences = Occurrences;    
})(API_schema_content_form || (API_schema_content_form = {}));
Ext.Loader.setConfig({
    enabled: false,
    disableCaching: false
});
Ext.override(Ext.LoadMask, {
    floating: {
        shadow: false
    },
    msg: undefined,
    cls: 'admin-load-mask',
    msgCls: 'admin-load-text',
    maskCls: 'admin-mask-white'
});
//@ sourceMappingURL=api.js.map
