var MiniAppBridge = {};
MiniAppBridge.messageQueue = [];
var uniqueId = Math.random();

var isPlatform = {
  Android: function() {
    return navigator.userAgent.match(/Android/i);
  },
  iOS: function() {
    return navigator.userAgent.match(/iPhone|iPad|iPod/i);
  },
};

/**
 * Method to call the native interface methods for respective platforms
 * such as iOS & Android
 * @param  {[String]} action Action command/interface name that native side need to execute
 * @param  {[Function]} onSuccess Success callback function
 * @param  {[Function]} onError Error callback function
 */
MiniAppBridge.exec = function(action, onSuccess, onError) {
  var callback = {};
  callback.onSuccess = onSuccess;
  callback.onError = onError;
  callback.id = String(++uniqueId);
  MiniAppBridge.messageQueue.unshift(callback);
  if (isPlatform.iOS()) {
    webkit.messageHandlers.MiniAppiOS.postMessage(
      JSON.stringify({ action: action, id: callback.id })
    );
  } else {
    window.MiniAppAndroid.postMessage(
      JSON.stringify({ action: action, id: callback.id })
    );
  }
};

/**
 * Success Callback method that will be called from native side
 * to this bridge. This method will send back the value to the
 * mini apps that uses promises
 * @param  {[String]} messageId Message ID which will be used to get callback object from messageQueue
 * @param  {[String]} value Response value sent from the native on invoking the action command
 */
MiniAppBridge.execSuccessCallback = function(messageId, value) {
  var queueObj = MiniAppBridge.messageQueue.filter(
    function(callback) { return callback.id == messageId }
  )[0];
  if (value) {
    queueObj.onSuccess(value);
  } else {
    queueObj.onError("Unknown Error");
  }
  removeFromMessageQueue(queueObj);
};

/**
 * Error Callback method that will be called from native side
 * to this bridge. This method will send back the error message to the
 * mini apps that uses promises
 * @param  {[String]} messageId Message ID which will be used to get callback object from messageQueue
 * @param  {[String]} errorMessage Error message sent from the native on invoking the action command
 */
MiniAppBridge.execErrorCallback = function(messageId, errorMessage) {
  var queueObj = MiniAppBridge.messageQueue.filter(
    function(callback) { return callback.id == messageId }
  )[0];
  if (!errorMessage) {
    errorMessage = "Unknown Error";
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
  var messageObjIndex = MiniAppBridge.messageQueue.indexOf(queueObj);
  if (messageObjIndex != -1) {
    MiniAppBridge.messageQueue.splice(messageObjIndex, 1);
  }
}

/**
 * Associating getUniqueId function to MiniAppBridge object
 */
MiniAppBridge.getUniqueId = function() {
  return new Promise(function(resolve, reject) {
    return MiniAppBridge.exec(
      "getUniqueId",
      function(id) { return resolve(id) },
      function (error) { return reject(error) }
    );
  });
};
window.MiniAppBridge = MiniAppBridge;

// Exported for unit testing
if (typeof exports==="object" && typeof module!=="undefined") {
  module.exports = MiniAppBridge;
}
