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

plugins {
    id("net.akehurst.kotlin.kt2ts") version "1.0.0"
}

val version_kserialisation = "1.4.0"
val version_coroutines = "1.3.2-1.3.60"
val version_kotlinx ="1.2.0"

dependencies {
    commonMainApi(project(":user-api"))
    commonMainImplementation("net.akehurst.kotlin.kserialisation:kserialisation-json:${version_kserialisation}")

    commonMainImplementation("net.akehurst.kotlinx:kotlinx-reflect:$version_kotlinx") //TODO: remove

    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$version_coroutines")
    jvm8MainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$version_coroutines")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$version_coroutines")
}

val tsdDir ="${buildDir}/tmp/jsJar/ts"

kotlin {
    sourceSets {
        val jsMain by getting {
            resources.srcDir("${tsdDir}")
        }
    }
}

kt2ts {
    localJvmName.set("jvm8")
    modulesConfigurationName.set("jvm8RuntimeClasspath")
    outputDirectory.set(file("${tsdDir}"))
    classPatterns.set(listOf(
            "net.akehurst.kotlin.example.addressbook.gui2core.*"
    ))
    moduleNameMap.set(mapOf(
            "org.jetbrains.kotlinx:kotlinx-coroutines-core" to "kotlinx-coroutines-core"
    ))
}
tasks.getByName("generateTypescriptDefinitionFile").dependsOn("jvm8MainClasses")
tasks.getByName("jsJar").dependsOn("generateTypescriptDefinitionFile")