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

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    application
}

val version_ktor: String by project
val version_ktor_spa: String = "1.1.4"

repositories {
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {

    // need this so that the gradle application-plugin can find the module built by the kotlin-plugin
    runtime(project(path = ":server", configuration = "jvm8RuntimeElements"))

    // to put the client module (angular code) on the classpath
    jvm8MainImplementation(project(":client-angular"))

    // ktor server modules
    jvm8MainImplementation("io.ktor:ktor-websockets:$version_ktor")
    jvm8MainImplementation("io.ktor:ktor-server-core:$version_ktor")
    jvm8MainImplementation("io.ktor:ktor-server-jetty:$version_ktor")
    jvm8MainImplementation("com.github.lamba92:ktor-spa:$version_ktor_spa")

    commonMainImplementation(project(":gui2core"))

    jvm8MainImplementation("org.slf4j:slf4j-simple:1.7.29")
}

// so that the application plugin can find the jars from the kotlin-plugin jvm configuration
val runtimeClasspath by configurations.getting {
    attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
}

application {
    mainClassName = "net.akehurst.kotlin.example.addressbook.server.ServerKt"

}