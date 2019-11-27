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
    id("net.akehurst.kotlin.kt2ts") version "1.1.0"
}

val version_kserialisation = "1.4.0"
val version_coroutines:String by project
val version_kotlinx ="1.2.0"

dependencies {
    commonMainApi(project(":user-api"))
    commonMainImplementation("net.akehurst.kotlin.kserialisation:kserialisation-json:${version_kserialisation}")

    commonMainImplementation("net.akehurst.kotlinx:kotlinx-reflect:$version_kotlinx") //TODO: remove

    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$version_coroutines")
    jvm8MainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$version_coroutines")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$version_coroutines")
}

kt2ts {
    localJvmName.set("jvm8")
    classPatterns.set(listOf(
            "net.akehurst.kotlin.example.addressbook.gui2core.*"
    ))
    moduleNameMap.set(mapOf(
            "org.jetbrains.kotlinx:kotlinx-coroutines-core" to "kotlinx-coroutines-core"
    ))
}

project.tasks.create("xxx") {

    doLast {
        val commonMainApi = this.project.configurations.findByName("commonMainApi") ?: throw RuntimeException("Cannot find 'commonMainApi' configuration")
        val commonMainImplementation = this.project.configurations.findByName("commonMainImplementation") ?: throw RuntimeException("Cannot find 'commonMainImplementation' configuration")
        val commonRuntime = this.project.configurations.create("commonRuntime")
        commonMainApi.dependencies.forEach {
            commonRuntime.dependencies.add(it)
        }
        commonMainImplementation.dependencies.forEach {
            commonRuntime.dependencies.add(it)
        }
        val c = project.configurations.create("kt2ts_jvmRuntimeConfiguration") {
            attributes{
                attribute(org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute, org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm)
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.KOTLIN_RUNTIME))
            }
        }
        commonRuntime.dependencies.forEach {
            if (it.name=="kotlinx-coroutines-core-common") {
                val ver = it.version
                c.dependencies.add(project.dependencies.create("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver"))
            } else {
                c.dependencies.add(it)
            }
        }
        c.resolvedConfiguration.resolvedArtifacts.forEach {
            println(it)
        }
    }

}
