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

import net.akehurst.kotlin.example.addressbook.information.AddressBook
import net.akehurst.kotlin.example.addressbook.information.Contact
import net.akehurst.kotlin.example.addressbook.information.PhoneNumber
import net.akehurst.kotlin.example.addressbook.information.UserSession
import net.akehurst.kotlin.example.addressbook.user.api.UserNotification
import net.akehurst.kotlin.example.addressbook.user.api.UserRequest


class Core : UserRequest {

    lateinit var userNotification: UserNotification

    // This is just an example/tutorial so we just use an in-memory store
    private val addressBooks = mutableMapOf<String, AddressBook>()

    init {
        //initialise with some default data
        val dab = AddressBook("Default")
        addressBooks[dab.title] = dab
        val aa = Contact("Adam")
        dab.contacts[aa.alias] = aa
        aa.firstName = "Adam"
        aa.lastName = "Ant"
        aa.phoneNumbers["Work"] = PhoneNumber("Work", "0123456789")
        val bb = Contact("Bri")
        dab.contacts[bb.alias] = bb
        bb.firstName = "Brian"
        bb.lastName = "Blessed"
        bb.phoneNumbers["Work"] = PhoneNumber("Work", "0123456789")


        val ab2 = AddressBook("Address Book 2")
        val c21 = Contact("Angie")
        c21.firstName = "Angelina"
        c21.lastName = "Jolie"
        c21.phoneNumbers["Home"] = PhoneNumber("Home", "0123456789")
        ab2.contacts[c21.alias] = c21
        addressBooks[ab2.title] = ab2
    }

    override fun requestCreateAddressBook(session: UserSession, title: String) {
        val ab = AddressBook(title)
        addressBooks[title] = ab
        this.userNotification.notifyCreatedAddressBook(session, title)
    }

    override fun requestReadAllAddressBookTitles(session: UserSession) {
        val titles = this.addressBooks.keys.toList()
        this.userNotification.notifyReadAllAddressBookTitles(session, titles)
    }

    override fun requestUpdateAddressBook(session: UserSession, oldTitle: String, newTitle: String) {
        val oldAb = this.addressBooks[oldTitle]
        if (null != oldAb) {
            val newAb = AddressBook(newTitle)
            newAb.contacts = oldAb.contacts
            this.addressBooks[newAb.title] = newAb
            this.addressBooks.remove(oldTitle)
            this.userNotification.notifyUpdatedAddressBook(session, oldTitle, newTitle)
        }
    }

    override fun requestDeleteAddressBook(session: UserSession, title: String) {
        this.addressBooks.remove(title)
        this.userNotification.notifyDeletedAddressBook(session, title)
    }

    override fun requestCreateContact(session: UserSession, addressBookTitle: String, alias: String) {
        val ab = this.addressBooks[addressBookTitle]
        if (null != ab) {
            val contact = Contact(alias)
            ab.contacts[alias] = contact
        }
        this.userNotification.notifyCreatedContact(session, alias)
    }

    override fun requestReadAllContact(session: UserSession, addressBookTitle: String) {
        val ab = this.addressBooks[addressBookTitle]
        if (null != ab) {
            val all = ab.contacts.keys.toList()
            this.userNotification.notifyReadAllContact(session, all)
        }
    }

    override fun requestReadContact(session: UserSession, addressBookTitle: String, alias: String) {
        val ab = this.addressBooks[addressBookTitle]
        if (null != ab) {
            val contact = ab.contacts[alias]
            if (null != contact) {
                this.userNotification.notifyReadContact(session, contact)
            }
        }
    }

    override fun requestUpdateContact(session: UserSession, addressBookTitle: String, oldAlias: String, updatedContact: Contact) {
        val ab = this.addressBooks[addressBookTitle]
        if (null != ab) {
            if (updatedContact.alias == oldAlias) {
                ab.contacts[oldAlias] = updatedContact
            } else {
                ab.contacts.remove(oldAlias)
                ab.contacts[updatedContact.alias] = updatedContact
            }
            this.userNotification.notifyUpdatedContact(session, oldAlias, updatedContact)
        }
    }

    override fun requestDeleteContact(session: UserSession, addressBookTitle: String, alias: String) {
        val ab = this.addressBooks[addressBookTitle]
        if (null != ab) {
            ab.contacts.remove(alias)
            this.userNotification.notifyDeletedContact(session, alias)
        }
    }


}