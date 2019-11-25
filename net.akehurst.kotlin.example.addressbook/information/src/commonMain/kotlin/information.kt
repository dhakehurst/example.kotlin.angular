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