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

package net.akehurst.kotlin.example.addressbook.websocket.client.ktor

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.js.JsName

class WebsocketClientKtor<T : Any>(
        val endPointId: T,
        val host: String,
        val port: Int,
        val path: String
) {

    companion object {
        val DELIMITER = "|"
    }

    @JsName("websocket")
    private var websocket: WebSocketSession? = null
    lateinit var outgoingMessage: Channel<Triple<String, String, String>>
    val incomingMessage = Channel<Triple<String, String, String>>()

    fun start() {
        GlobalScope.launch {
            incomingMessage.consumeEach { m ->
                val sessionId = m.first
                val messageId = m.second
                val message = m.third
                val frame = Frame.Text("${messageId}${DELIMITER}${message}")
                websocket?.outgoing?.offer(frame)
            }
        }
        GlobalScope.launch {
            val client = HttpClient() {
                install(WebSockets)
            }
            client.ws(
                    method = HttpMethod.Get,
                    host = host,
                    port = port,
                    path = path
            ) {
                handleWebsocketConnection(endPointId, this)
            }
        }
    }

    private suspend fun handleWebsocketConnection(session: T, ws: WebSocketSession) {
        websocket = ws
        val sessionId = "?"
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
                        outgoingMessage.send(Triple(sessionId, messageId, message))
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
            websocket = null
            println("Websocket Connection closed from $session")
        }
    }
}