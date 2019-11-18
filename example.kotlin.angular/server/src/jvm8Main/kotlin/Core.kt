package net.akehurst.kotlin.example.addressbook.server

import net.akehurst.kotlin.example.addressbook.information.AddressBook
import net.akehurst.kotlin.example.addressbook.information.Contact
import net.akehurst.kotlin.example.addressbook.information.UserSession
import net.akehurst.kotlin.example.addressbook.user.api.UserNotification
import net.akehurst.kotlin.example.addressbook.user.api.UserRequest


class Core : UserRequest {

    lateinit var userNotification: UserNotification

    // This is just an example/tutorial so we just use an in-memory store
    private val addressBooks = mutableMapOf<String, AddressBook>()

    override fun requestCreateAddressBook(session: UserSession, title: String) {
        val ab = AddressBook(title)
        addressBooks[title] = ab
        this.userNotification.notifyCreatedAddressBook(session,title)
    }

    override fun requestReadAllAddressBookTitles(session: UserSession) {
        val titles = this.addressBooks.keys.toList()
        this.userNotification.notifyReadAllAddressBookTitles(session,titles)
    }

    override fun requestUpdateAddressBook(session: UserSession, oldTitle: String, newTitle: String) {
        TODO("not implemented")
    }

    override fun requestDeleteAddressBook(session: UserSession, title: String) {
        this.addressBooks.remove(title)
        this.userNotification.notifyDeletedAddressBook(session,title)
    }

    override fun requestCreateContact(session: UserSession, addressBookTitle: String, alias: String) {
        val ab = this.addressBooks[addressBookTitle]
        if (null != ab) {
            val contact = Contact(alias)
            ab.contacts[alias] = contact
        }
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
                this.userNotification.notifyReadContact(session,contact)
            }
        }
    }

    override fun requestUpdateContact(session: UserSession, addressBookTitle: String, oldAlias: String, contact: Contact) {
        TODO("not implemented")
    }

    override fun requestDeleteContact(session: UserSession, addressBookTitle: String, alias: String) {
        val ab = this.addressBooks[addressBookTitle]
        if (null != ab) {
            ab.contacts.remove(alias)
            this.userNotification.notifyDeletedContact(session,alias)
        }
    }


}