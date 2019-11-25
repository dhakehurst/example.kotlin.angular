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
import net.akehurst.kotlin.example.addressbook.information.AddressBook
import net.akehurst.kotlin.example.addressbook.information.Contact
import net.akehurst.kotlin.example.addressbook.information.UserSession
import net.akehurst.kotlin.example.addressbook.user.api.UserNotification
import net.akehurst.kotlin.example.addressbook.user.api.UserRequest

class Core2Gui : UserNotification {

    lateinit var userRequest: UserRequest
    lateinit var outgoingMessage: Channel<Triple<String, String, String>>

    val incomingMessage = Channel<Triple<String, String, String>>()

    private val serialiser = InformationSerialiser()
    private val messageActions = mutableMapOf<String, (UserSession, List<Any>) -> Unit>()

    private fun send(session: UserSession, messageId: String, vararg args: Any) {
        println("send: $messageId")
        val args = args.toList()
        val json = serialiser.toJson(args, args)
        val str = json.toJsonString()
        GlobalScope.async {
            outgoingMessage.send(Triple(session.sessionId, messageId, str))
        }.start()
    }

    fun start() {
        //TODO: use reflection here when kotlin JS reflection works
        messageActions["requestCreateAddressBook"] = { s, args -> this.userRequest.requestCreateAddressBook(s, args[0] as String) }
        messageActions["requestReadAllAddressBookTitles"] = { s, args -> this.userRequest.requestReadAllAddressBookTitles(s) }
        messageActions["requestUpdateAddressBook"] = { s, args -> this.userRequest.requestUpdateAddressBook(s, args[0] as String, args[1] as String) }
        messageActions["requestDeleteAddressBook"] = { s, args -> this.userRequest.requestDeleteAddressBook(s, args[0] as String) }

        messageActions["requestCreateContact"] = { s, args -> this.userRequest.requestCreateContact(s, args[0] as String, args[1] as String) }
        messageActions["requestReadAllContact"] = { s, args -> this.userRequest.requestReadAllContact(s, args[0] as String) }
        messageActions["requestReadContact"] = { s, args -> this.userRequest.requestReadContact(s, args[0] as String, args[1] as String) }
        messageActions["requestUpdateContact"] = { s, args -> this.userRequest.requestUpdateContact(s, args[0] as String, args[1] as String, args[2] as Contact) }
        messageActions["requestDeleteContact"] = { s, args -> this.userRequest.requestDeleteContact(s, args[0] as String, args[1] as String) }

        GlobalScope.launch {
            incomingMessage.consumeEach { m ->
                val sessionId = m.first
                val messageId = m.second
                val message = m.third
                val jargs = serialiser.toData(message) as List<Any>
                val session = UserSession(sessionId)
                println("received: $messageId")
                messageActions[messageId]?.invoke(session, jargs)
            }
        }
    }

    override fun notifyCreatedAddressBook(session: UserSession, title: String) {
        this.send(session, "notifyCreatedAddressBook", title)
    }

    override fun notifyReadAllAddressBookTitles(session: UserSession, titles: List<String>) {
        this.send(session, "notifyReadAllAddressBookTitles", titles)
    }

    override fun notifyUpdatedAddressBook(session: UserSession, oldTitle: String, newTitle: String) {
        this.send(session, "notifyUpdatedAddressBook", newTitle)
    }

    override fun notifyDeletedAddressBook(session: UserSession, title: String) {
        this.send(session, "notifyDeletedAddressBook", title)
    }

    override fun notifyCreatedContact(session: UserSession, alias: String) {
        this.send(session, "notifyCreatedContact", alias)
    }

    override fun notifyReadAllContact(session: UserSession, all: List<String>) {
        this.send(session, "notifyReadAllContact", all)
    }

    override fun notifyReadContact(session: UserSession, contact: Contact) {
        this.send(session, "notifyReadContact", contact)
    }

    override fun notifyUpdatedContact(session: UserSession, oldAlias: String, updatedContact: Contact) {
        this.send(session, "notifyUpdatedContact", updatedContact)
    }

    override fun notifyDeletedContact(session: UserSession, alias: String) {
        this.send(session, "notifyDeletedContact", alias)
    }

}