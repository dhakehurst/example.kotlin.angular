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

package net.akehurst.kotlin.example.addressbook.gui2core

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import net.akehurst.kotlin.example.addressbook.information.Contact
import net.akehurst.kotlin.example.addressbook.information.UserSession
import net.akehurst.kotlin.example.addressbook.user.api.UserNotification
import net.akehurst.kotlin.example.addressbook.user.api.UserRequest

class Gui2Core : UserRequest {

    lateinit var userNotification: UserNotification
    lateinit var outgoingMessage: Channel<Triple<String, String, String>>

    val incomingMessage = Channel<Triple<String, String, String>>()

    private val serialiser = InformationSerialiser()
    private val messageActions = mutableMapOf<String, (UserSession, List<Any>) -> Unit>()

    private fun send(session: UserSession, messageId: String, vararg args: Any) {
        println("send: $messageId")
        val argsList = args.toList()
        val json = serialiser.toJson(argsList, argsList)
        val str = json.toJsonString()
        GlobalScope.async {
            outgoingMessage.send(Triple(session.sessionId, messageId, str))
        }.start()
    }

    fun start() {
        //TODO: use reflection here when kotlin JS reflection works
        messageActions["notifyCreatedAddressBook"] = { s, args -> this.userNotification.notifyCreatedAddressBook(s, args[0] as String) }
        messageActions["notifyReadAllAddressBookTitles"] = { s, args -> this.userNotification.notifyReadAllAddressBookTitles(s, args[0] as List<String>) }
        messageActions["notifyUpdatedAddressBook"] = { s, args -> this.userNotification.notifyUpdatedAddressBook(s, args[0] as String, args[1] as String) }
        messageActions["notifyDeletedAddressBook"] = { s, args -> this.userNotification.notifyDeletedAddressBook(s, args[0] as String) }

        messageActions["notifyCreatedContact"] = { s, args -> this.userNotification.notifyCreatedContact(s, args[0] as String) }
        messageActions["notifyReadAllContact"] = { s, args -> this.userNotification.notifyReadAllContact(s, args[0] as List<String>) }
        messageActions["notifyReadContact"] = { s, args -> this.userNotification.notifyReadContact(s, args[0] as Contact) }
        messageActions["notifyUpdatedContact"] = { s, args -> this.userNotification.notifyUpdatedContact(s, args[0] as String, args[1] as Contact) }
        messageActions["notifyDeletedContact"] = { s, args -> this.userNotification.notifyDeletedContact(s, args[0] as String) }

        GlobalScope.launch {
            incomingMessage.consumeEach { m ->
                val sessionId = m.first
                val messageId = m.second
                val message = m.third
                val data = serialiser.toData(message)
                val jargs = if (data is List<*>) data as List<Any> else throw RuntimeException("expected List<Any> got ${if (null==data) "null" else data::class}")
                val session = UserSession(sessionId)
                println("received: $messageId")
                messageActions[messageId]?.invoke(session, jargs)
            }
        }
    }

    override fun requestCreateAddressBook(session: UserSession, title: String) {
        this.send(session, "requestCreateAddressBook", title)
    }

    override fun requestReadAllAddressBookTitles(session: UserSession) {
        this.send(session, "requestReadAllAddressBookTitles")
    }

    override fun requestUpdateAddressBook(session: UserSession, oldTitle: String, newTitle: String) {
        this.send(session, "requestUpdateAddressBook", oldTitle, newTitle)
    }

    override fun requestDeleteAddressBook(session: UserSession, title: String) {
        this.send(session, "requestDeleteAddressBook", title)
    }

    override fun requestCreateContact(session: UserSession, addressBookTitle: String, alias: String) {
        this.send(session, "requestCreateContact", addressBookTitle, alias)
    }

    override fun requestReadAllContact(session: UserSession, addressBookTitle: String) {
        this.send(session, "requestReadAllContact", addressBookTitle)
    }

    override fun requestReadContact(session: UserSession, addressBookTitle: String, alias: String) {
        this.send(session, "requestReadContact", addressBookTitle, alias)
    }

    override fun requestUpdateContact(session: UserSession, addressBookTitle: String, oldAlias: String, updatedContact: Contact) {
        this.send(session, "requestUpdateContact", addressBookTitle, oldAlias, updatedContact)
    }

    override fun requestDeleteContact(session: UserSession, addressBookTitle: String, alias: String) {
        this.send(session, "requestDeleteContact", addressBookTitle, alias)
    }

}