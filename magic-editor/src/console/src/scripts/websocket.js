import bus from "@/scripts/bus";
import ReconnectingWebSocket from './reconnecting-websocket'
function MagicWebSocket(url) {
    this.listeners = {};
    this.socket = new ReconnectingWebSocket(url);
    this.socket.onmessage = this.messageReceived;
    bus.$on('message', (msgType, content) => {
        if (content) {
            this.socket.send(`${msgType},${content}`)
        } else {
            this.socket.send(msgType)
        }
    })
    this.socket.onopen = ()=> {
        bus.$emit('ws_open')
    }
}

MagicWebSocket.prototype.on = function (msgType, callback) {
    this.listeners[msgType] = this.listeners[msgType] || []
    this.listeners[msgType].push(callback)
}

MagicWebSocket.prototype.messageReceived = function (e) {
    let payload = e.data
    let index = payload.indexOf(",")
    let msgType = index === -1 ? payload : payload.substring(0, index)
    let args = []
    while (index > -1) {
        payload = payload.substring(index + 1)
        if (payload.startsWith('[') || payload.startsWith('{')) {
            args.push(JSON.parse(payload))
            break;
        }
        let newIndex = payload.indexOf(",", index + 1)
        args.push(newIndex === -1 ? payload : payload.substring(index + 1, newIndex))
        index = newIndex
    }
    bus.$emit('ws_' + msgType, args)
}
MagicWebSocket.prototype.close = function () {
    this.socket.close()
}

export default MagicWebSocket