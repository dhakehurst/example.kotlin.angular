package net.akehurst.kotlin.example.addressbook.server

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.*
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import net.akehurst.kotlin.example.addressbook.gui2core.Core2Gui
import net.akehurst.kotlin.example.addressbook.user.api.UserRequest

fun main() {
    val app = AddressBookApplication()
    app.start()
}

class AddressBookApplication {

    val server = Server()
    val core2gui = Core2Gui()
    val core = Core()

    fun start() {
        core.userNotification = core2gui
        core2gui.userRequest = core
        core2gui.outgoingMessage = server.incomingMessage
        server.outgoingMessage = core2gui.incomingMessage
        core2gui.start()
        server.start()
    }

}


class Server {

    companion object {
        val DELIMITER = "|"
    }

    lateinit var outgoingMessage: Channel<Triple<String, String, String>>
    val incomingMessage = Channel<Triple<String, String, String>>()

    private val connections = mutableMapOf<String, WebSocketSession>()

    fun start() {

        GlobalScope.launch {
            incomingMessage.consumeEach { m ->
                val sessionId = m.first
                val messageId = m.second
                val message = m.third
                val frame = Frame.Text("$messageId$DELIMITER$message")
                val ws = connections[sessionId]!!
                ws.send(frame)
            }
        }

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
            connections[session] = ws
            println("Websocket Connection opened from $session")
            //messageChannel.newEndPoint(session) ?
            try {
                ws.incoming.consumeEach { frame ->
                    println("Websocket Connection message from $session, $frame")
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            val messageId = text.substringBefore(DELIMITER)
                            val message = text.substringAfter(DELIMITER)
                            this.outgoingMessage.send(Triple(session, messageId, message))
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
                connections.remove(session)
                println("Websocket Connection closed from $session")
            }
        }
    }

}