package net.akehurst.kotlin.example.addressbook.server

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.default
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Routing
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach

fun main() {
    val app = AddressBookApplication()
    app.start()
}

class AddressBookApplication {

    val server = Server()
    val store = Store()

    fun start() {
        store.start()
        server.start()
    }

}

class Store {

    fun start() {
    }

}

class Server {

    fun start() {
        val server = embeddedServer(Jetty, port = 9999, host = "0.0.0.0") {
            install(DefaultHeaders)
            install(CallLogging)
            install(Routing)
            install(WebSockets)
            install(Sessions) {
                cookie<String>("SESSION")
            }
            intercept(ApplicationCallPipeline.Features) {
                call.sessions.set<String>(generateNonce())
            }
            routing {
                webSocket("/ws") {
                    handleWebsocketConnection(this)
                }
                static("/") {
                    resources("/dist")
                    default("index.html")
                }
            }
        }

        server.start(true)
    }

    suspend fun handleWebsocketConnection(ws: WebSocketServerSession) {
        val session = ws.call.sessions.get<String>()
        if (session == null) {
            //this should not happen
            ws.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
        } else {
            //connections[session] = ws
            println("Websocket Connection opened from $session")
            //messageChannel.newEndPoint(session) ?
            try {
                ws.incoming.consumeEach { frame ->
                    println("Websocket Connection message from $session, $frame")
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                           // val channelId = ChannelIdentity(text.substringBefore(MessageChannel.DELIMITER))
                           // val message = text.substringAfter(MessageChannel.DELIMITER)
                           // this.receiveActions[channelId]?.invoke(session, message)
                        }
                        is Frame.Binary -> {
                        }
                        is Frame.Ping -> {
                        }
                        is Frame.Pong -> {
                        }
                        is Frame.Close -> {
                            // handled in finally block
                        }
                    }
                }
            } finally {
              //  connections.remove(session)
                println("Websocket Connection closed from $session")
            }
        }
    }

}