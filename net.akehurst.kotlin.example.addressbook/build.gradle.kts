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

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    kotlin("multiplatform") version("1.3.60") apply false
    id("net.akehurst.kotlin.kt2ts") version("1.3.0") apply false
}

allprojects {

    repositories {
        mavenCentral()
        jcenter()
    }

    val version_project: String by project
    val group_project = "${rootProject.name}"

    group = group_project
    version = version_project

    buildDir = File(rootProject.projectDir, ".gradle-build/${project.name}")

}


subprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "net.akehurst.kotlin.kt2ts")

    configure<KotlinMultiplatformExtension> {
        sourceSets {
            all {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                }
            }
        }

        // we want to build for a JS target
        js("js") {
            browser()
        }
        // we want to build for a jvm target
        jvm("jvm8") {
            // by default kotlin uses JavaVersion 1.6
            val main by compilations.getting {
                kotlinOptions {
                    jvmTarget = JavaVersion.VERSION_1_8.toString()
                    freeCompilerArgs = listOf("-Xinline-classes")
                }
            }
            val test by compilations.getting {
                kotlinOptions {
                    jvmTarget = JavaVersion.VERSION_1_8.toString()
                }
            }
        }
    }

    dependencies {
        "commonMainImplementation"(kotlin("stdlib"))
        //"commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$version_coroutines")
        "commonTestImplementation"(kotlin("test"))
        "commonTestImplementation"(kotlin("test-annotations-common"))

        "jvm8MainImplementation"(kotlin("stdlib-jdk8"))
        //"jvm8MainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$version_coroutines")
        "jvm8TestImplementation"(kotlin("test-junit"))

        "jsMainImplementation"(kotlin("stdlib-js"))
        //"jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$version_coroutines")
        "jsTestImplementation"(kotlin("test-js"))
    }

}
