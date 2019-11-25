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

import net.akehurst.kotlin.json.JsonDocument
import net.akehurst.kotlin.kserialisation.json.KSerialiserJson
import net.akehurst.kotlinx.reflect.ModuleRegistry
import kotlin.js.JsName

class InformationSerialiser {

    companion object {
        val KOMPOSITE = """
            namespace net.akehurst.kotlin.example.addressbook.information {
                datatype AddressBook {
                    val title: String
                    car contacts: Map<String,Contact>
                }
                datatype Contact {
                    val alias: String
                    var firstName: String
                    var lastName: String
                    car phoneNumbers: Map<String, PhoneNumber>
                }
                datatype PhoneNumber {
                    val label: String
                    val number: String
                }
            }
        """.trimIndent()
    }

    internal val kserialiser = KSerialiserJson()

    init {
        //this.kserialiser.registerModule("example.kotlin.angular-information")
        //TODO: replace with above
        ModuleRegistry.register("example.kotlin.angular-information")
        this.kserialiser.confgureDatatypeModel(KOMPOSITE)

        this.kserialiser.registerKotlinStdPrimitives();
    }

    @JsName("toKList")
    fun toKList(arr: Array<Any>) = arr.toList()

    @JsName("toData")
    fun toData(jsonString: String): Any? = this.kserialiser.toData(jsonString)

    @JsName("toJson")
    fun toJson(root: Any, data: Any): JsonDocument = this.kserialiser.toJson(root, data)


}