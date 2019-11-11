package net.akehurst.kotlin.example.addressbook.server

import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.websocket.WebSockets

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
            install(WebSockets) {

            }
        }

        server.start(true)
    }

}