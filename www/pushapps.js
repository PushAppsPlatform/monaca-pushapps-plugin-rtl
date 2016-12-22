var exec = cordova.require('cordova/exec');

/**
 * PushNotification empty constructor
 */

var PushNotification = function() {};

PushNotification.prototype.onDeviceReady = function (sdkKey, outbrainKey) {
    exec(null, null, 'PushApps', 'onDeviceReady', [ sdkKey, outbrainKey ]);
};

PushNotification.prototype.registerToPushNotifications = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'PushApps', 'registerToPushNotifications', []);
};

PushNotification.prototype.getDeviceId = function (successCallback) {
    exec(successCallback, null, 'PushApps', 'getDeviceId', []);
};

PushNotification.prototype.isPushEnabled = function (successCallback) {
    exec(successCallback, null, 'PushApps', 'isPushEnabled', []);
};

PushNotification.prototype.enablePush = function (enable) {
    exec(null, null, 'PushApps', 'enablePush', [ enable ]);
};

PushNotification.prototype.isSoundEnabled = function (successCallback) {
    exec(successCallback, null, 'PushApps', 'isSoundEnabled', []);
};

PushNotification.prototype.enableSound = function (enable) {
    exec(null, null, 'PushApps', 'enableSound', [ enable ]);
};

PushNotification.prototype.isVibrateEnabled = function (successCallback) {
    exec(successCallback, null, 'PushApps', 'isVibrateEnabled', []);
};

PushNotification.prototype.enableVibrate = function (enable) {
    exec(null, null, 'PushApps', 'enableVibrate', [ enable ]);
};

PushNotification.prototype.trackUrlView = function (url, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'PushApps', 'trackUrlView', [ url ]);
};

PushNotification.prototype.getSdkVersion = function (successCallback) {
    exec(successCallback, null, 'PushApps', 'getSdkVersion', []);
};

PushNotification.prototype.addTag = function (name, value) {
    exec(null, null, 'PushApps', 'addTag', [ name, value ]);
};

PushNotification.prototype.removeTag = function (name) {
    exec(null, null, 'PushApps', 'removeTag', [ name ]);
};

PushNotification.prototype.getTags = function (successCallback) {
    exec(successCallback, null, 'PushApps', 'getTags', []);
};

// Event spawned when a notification is clicked by the user
PushNotification.prototype.messageClicked = function (messageParams) {

    // The notification object
    var notification = JSON.parse(messageParams);

    var devicePlatform = device.platform;
    if (devicePlatform === "Android") {

        for (var key in notification) {
            if (notification.hasOwnProperty(key)) {
                var txt = document.createElement("textarea");
                txt.innerHTML = notification[key];
                notification[key] = txt.value;
            }
        }

    }

    var ev = document.createEvent('HTMLEvents');
    ev.notification = notification;
    ev.initEvent('pushapps.message-clicked', true, true, arguments);
    document.dispatchEvent(ev);
};

/**
 * Push Notification Plugin
 */

module.exports = new PushNotification();
