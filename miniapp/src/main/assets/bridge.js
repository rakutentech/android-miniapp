(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
"use strict";
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
var commonBridge = __importStar(require("../common-bridge"));
/* tslint:disable:no-any */
var uniqueId = Math.random();
commonBridge.MiniAppBridge.prototype.exec = function (action, param, onSuccess, onError) {
    var callback = {};
    callback.onSuccess = onSuccess;
    callback.onError = onError;
    callback.id = String(++uniqueId);
    commonBridge.mabMessageQueue.unshift(callback);
    window.MiniAppAndroid.postMessage(JSON.stringify({ action: action, param: param, id: callback.id }));
};
window.MiniAppBridge = commonBridge.MiniAppBridge.prototype;

},{"../common-bridge":2}],2:[function(require,module,exports){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
/* tslint:disable:no-any */
var mabMessageQueue = [];
exports.mabMessageQueue = mabMessageQueue;
var MiniAppBridge = /** @class */ (function () {
    function MiniAppBridge() {
    }
    return MiniAppBridge;
}());
exports.MiniAppBridge = MiniAppBridge;
MiniAppBridge.prototype.execSuccessCallback = function (messageId, value) {
    var queueObj = mabMessageQueue.filter(function (callback) { return callback.id === messageId; })[0];
    if (value) {
        queueObj.onSuccess(value);
    }
    else {
        queueObj.onError('Unknown Error');
    }
    removeFromMessageQueue(queueObj);
};
MiniAppBridge.prototype.execErrorCallback = function (messageId, errorMessage) {
    var queueObj = mabMessageQueue.filter(function (callback) { return callback.id === messageId; })[0];
    if (!errorMessage) {
        errorMessage = 'Unknown Error';
    }
    queueObj.onError(errorMessage);
    removeFromMessageQueue(queueObj);
};
/**
 * Method to remove the callback object from the message queue after successfull/error communication
 * with the native application
 * @param  {[Object]} queueObj Queue Object that holds the references of callback informations
 */
function removeFromMessageQueue(queueObj) {
    var messageObjIndex = mabMessageQueue.indexOf(queueObj);
    if (messageObjIndex !== -1) {
        mabMessageQueue.splice(messageObjIndex, 1);
    }
}
MiniAppBridge.prototype.getUniqueId = function () {
    return new Promise(function (resolve, reject) {
        return MiniAppBridge.prototype.exec('getUniqueId', null, function (id) { return resolve(id); }, function (error) { return reject(error); });
    });
};
/**
 * Associating requestPermission function to MiniAppBridge object
 * @param {String} permissionType Type of permission that is requested. For eg., location
 */
MiniAppBridge.prototype.requestPermission = function (permissionType) {
    return new Promise(function (resolve, reject) {
        return MiniAppBridge.prototype.exec('requestPermission', { permission: permissionType }, function (success) { return resolve(success); }, function (error) { return reject(error); });
    });
};

},{}]},{},[1]);
