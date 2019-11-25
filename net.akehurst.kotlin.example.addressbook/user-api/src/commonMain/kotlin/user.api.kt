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

package net.akehurst.kotlin.example.addressbook.user.api

import net.akehurst.kotlin.example.addressbook.information.Contact
import net.akehurst.kotlin.example.addressbook.information.UserSession
import kotlin.js.JsName


interface UserRequest {
    @JsName("requestCreateAddressBook")
    fun requestCreateAddressBook(session: UserSession, title: String)
    @JsName("requestReadAllAddressBookTitles")
    fun requestReadAllAddressBookTitles(session: UserSession)
    @JsName("requestUpdateAddressBook")
    fun requestUpdateAddressBook(session: UserSession, oldTitle: String, newTitle: String)
    @JsName("requestDeleteAddressBook")
    fun requestDeleteAddressBook(session: UserSession, title: String)

    @JsName("requestCreateContact")
    fun requestCreateContact(session: UserSession, addressBookTitle: String, alias: String)
    @JsName("requestReadAllContact")
    fun requestReadAllContact(session: UserSession, addressBookTitle: String)
    @JsName("requestReadContact")
    fun requestReadContact(session: UserSession, addressBookTitle: String, alias: String)
    @JsName("requestUpdateContact")
    fun requestUpdateContact(session: UserSession, addressBookTitle: String, oldAlias: String, updatedContact: Contact)
    @JsName("requestDeleteContact")
    fun requestDeleteContact(session: UserSession, addressBookTitle: String, alias: String)
}

interface UserNotification {
    @JsName("notifyCreatedAddressBook")
    fun notifyCreatedAddressBook(session: UserSession, title: String)
    @JsName("notifyReadAllAddressBookTitles")
    fun notifyReadAllAddressBookTitles(session: UserSession, titles: List<String>)
    @JsName("notifyUpdatedAddressBook")
    fun notifyUpdatedAddressBook(session: UserSession, oldTitle: String, newTitle: String)
    @JsName("notifyDeletedAddressBook")
    fun notifyDeletedAddressBook(session: UserSession, title: String)

    @JsName("notifyCreatedContact")
    fun notifyCreatedContact(session: UserSession, alias: String)
    @JsName("notifyReadAllContact")
    fun notifyReadAllContact(session: UserSession, all: List<String>)
    @JsName("notifyReadContact")
    fun notifyReadContact(session: UserSession, contact: Contact)
    @JsName("notifyUpdatedContact")
    fun notifyUpdatedContact(session: UserSession, oldAlias: String, updatedContact: Contact)
    @JsName("notifyDeletedContact")
    fun notifyDeletedContact(session: UserSession, alias: String)
}