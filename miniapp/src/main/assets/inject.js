window.MiniAppBridge = {}

var queue = []
var isPlatform = {
    Android: function() {
        return navigator.userAgent.match(/Android/i);
    },
    iOS: function() {
        return navigator.userAgent.match(/iPhone|iPad|iPod/i);
    }
};

MiniAppBridge.exec = function(action, onSuccess, onError) {
    const callback = {}
    callback.onSuccess = onSuccess;
    callback.onError = onError;
    callback.id = Math.random();
    queue.unshift(callback)
    if(isPlatform.iOS()){
        webkit.messageHandlers.MiniApp.postMessage(JSON.stringify({action: action, id: callback.id}));
    } else {
        window.MiniAppAndroid.getUniqueId(JSON.stringify({action: action, id: callback.id}))
    }
}

MiniAppBridge.execCallback = function(messageId, value) {
    var queueObj = queue.find(callback => callback.id = messageId)
    queueObj.onSuccess(value);
    var messageObjIndex = queue.indexOf(queueObj)
    if(messageObjIndex != -1) {
        queue.splice(messageObjIndex, 1);
    }
}

window.MiniApp = {
  getUniqueId: () => {
      return new Promise((resolve, reject) => {
          return MiniAppBridge.exec(
              "getUniqueId",
              id => resolve(id),
              error => reject(error)
          );
      })
  }
}