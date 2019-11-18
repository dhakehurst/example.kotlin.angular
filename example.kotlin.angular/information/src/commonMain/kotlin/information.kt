package net.akehurst.kotlin.example.addressbook.information

data class UserSession(val sessionId:String)

data class AddressBook(val title: String) {
    var contacts = mutableMapOf<String, Contact>()
}

data class Contact(val alias: String) {
    var firstName: String? = null
    var lastName: String? = null
    var phoneNumbers = mutableMapOf<String, PhoneNumber>()
}

data class PhoneNumber(val label: String, val number: String)