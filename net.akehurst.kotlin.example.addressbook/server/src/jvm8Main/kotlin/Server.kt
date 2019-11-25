/**
 * Copyright (C) 2019 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.kotlin.example.addressbook.server

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.*
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
        val host = "localhost"
        val port = 9999
    }

    lateinit var outgoingMessage: Channel<Triple<String, String, String>>
    val incomingMessage = Channel<Triple<String, String, String>>()

    private val connections = mutableMapOf<String, WebSocketSession>()

    fun start() {

        GlobalScope.launch {
            incomingMessage.consumeEach { m ->
                val sessionId = m.first
                val messageId = m.second
                println("incoming: $messageId")
                val message = m.third
                val frame = Frame.Text("$messageId$DELIMITER$message")
                val ws = connections[sessionId]!!
                ws.send(frame)
            }
        }

        val server = embeddedServer(Jetty, port = port, host = host) {
            install(DefaultHeaders)
            install(CallLogging)
            install(Routing)
            install(Sessions) {
                cookie<String>("SESSION_ID")
            }
            install(WebSockets) {

            }
            intercept(ApplicationCallPipeline.Features) {
                call.sessions.set<String>(generateNonce())
            }
            install(SinglePageApplication) {
                defaultPage = "index.html"
                folderPath = "/dist"
                spaRoute = ""
                useFiles = false
            }
            routing {
                webSocket("/ws") {
                    handleWebsocketConnection(this)
                }
                /*
                static("/") {
                    resources("/dist")
                    default("index.html")
                }
                */
            }
        }

        server.start(true)
    }

    suspend fun handleWebsocketConnection(ws: WebSocketServerSession) {
        val sessionId = ws.call.sessions.get<String>()
        if (sessionId == null) {
            //this should not happen
            ws.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
        } else {
            connections[sessionId] = ws
            println("Websocket Connection opened from $sessionId")
            //messageChannel.newEndPoint(session) ?
            try {
                ws.incoming.consumeEach { frame ->
                    println("Websocket Connection message from $sessionId, $frame")
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            val messageId = text.substringBefore(DELIMITER)
                            val message = text.substringAfter(DELIMITER)
                            println("outgoing: $messageId")
                            this.outgoingMessage.send(Triple(sessionId, messageId, message))
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
                connections.remove(sessionId)
                println("Websocket Connection closed from $sessionId")
            }
        }
    }

}