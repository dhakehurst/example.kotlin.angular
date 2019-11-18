package net.akehurst.kotlin.example.addressbook.gui2core

import net.akehurst.kotlin.json.JsonDocument
import net.akehurst.kotlin.kserialisation.json.KSerialiserJson
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
                    car firstName: String
                    car lastName: String
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
        this.kserialiser.registerModule("example.kotlin.angular-information")

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