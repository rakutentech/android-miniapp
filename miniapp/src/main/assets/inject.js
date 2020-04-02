function init() {
    document.getElementById('uniqueId').textContent = "Retrieving..."
}

function printUniqueId() {
    console.log("printUniqueId")
    window.MiniApp.getUniqueId()
}

let MiniAppBridge = {}
var queue = []

MiniAppBridge.exec = function(action, onSuccess, onError) {
    const callback = {}
    callback.onSuccess = onSuccess;
    callback.onError = onError;
    callback.id = Math.random();
    queue.unshift(callback)
    localStorage.setItem(callback.id, callback)
    window.MiniAppAndroid.getUniqueId(JSON.stringify({action: action, id: callback.id}))
}

MiniAppBridge.execCallback = function(messageId, value) {
//    var queueObj = queue.find(callback => callback.id = messageId)
    var queueObj = localStorage.getItem(messageId)
    console.log(queueObj)
    queueObj.onSuccess(value);
    queue.shift(queueObj)
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

init()