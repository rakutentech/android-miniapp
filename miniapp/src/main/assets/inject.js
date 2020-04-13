var messageQueue = []
window.MiniAppBridge = {};
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
    messageQueue.unshift(callback)
    if(isPlatform.iOS()){
        webkit.messageHandlers.MiniAppiOS.postMessage(JSON.stringify({action: action, id: callback.id}));
    } else {
        window.MiniAppAndroid.postMessage(JSON.stringify({action: action, id: callback.id}))
    }
}

MiniAppBridge.execCallback = function(messageId, value) {
    var queueObj = messageQueue.find(callback => callback.id = messageId)
    queueObj.onSuccess(value);
    var messageObjIndex = messageQueue.indexOf(queueObj)
    if(messageObjIndex != -1) {
        messageQueue.splice(messageObjIndex, 1);
    }
}

MiniAppBridge.getUniqueId = function(messageId, value) {
    return new Promise((resolve, reject) => {
        return MiniAppBridge.exec(
            "getUniqueId",
            id => resolve(id),
            error => reject(error)
        );
    })
}