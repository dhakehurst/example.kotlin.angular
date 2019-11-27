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

import net.akehurst.kotlin.kt2ts.plugin.gradle.GenerateDeclarationsTask
import net.akehurst.kotlin.kt2ts.plugin.gradle.GeneratePackageJsonTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSimple

plugins {
    id("net.akehurst.kotlin.kt2ts") version "1.1.0"
}

val version_coroutines:String by project

dependencies {

    // only need a direct dependency on these, the rest are transitively discovered
    "ngKotlin"(project(":gui2core"))
    "ngKotlin"(project(":websocketClient"))

    //add this so that the kt2ts plugin can find the jvm classes
    // it needs the jvm modules/classes because it uses jvm reflection to generate .d.ts files
    jvm8MainImplementation(project(":gui2core"))
}


// define these locations because they are used in multiple places
val ngSrcDir = project.layout.projectDirectory.dir("src/angular")
val ngOutDir = project.layout.buildDirectory.dir("angular")

kt2ts {
    ngSrcDirectory.set(ngSrcDir)
    ngOutDirectory.set(ngOutDir)
    ngBuildAdditionalArguments.set(listOf(if (project.hasProperty("ngProd")) "--prod" else ""))

    localJvmName.set("jvm8")

    generateThirdPartyModules {
        register("org.jetbrains.kotlinx:kotlinx-coroutines-core:$version_coroutines") {
            includeOnly.set(listOf("kotlinx-coroutines-core"))
            moduleGroup.set("")
            moduleName.set("kotlinx-coroutines-core")
            mainFileName.set("kotlinx-coroutines-core.js")
            tgtName.set("kotlinx-coroutines-core")
            classPatterns.set(listOf(
                    "kotlinx.coroutines.internal.OpDescriptor",
                    "kotlinx.coroutines.internal.AtomicOp",
                    "kotlinx.coroutines.internal.AtomicDesc",
                    "kotlinx.coroutines.DisposableHandle",
                    "kotlinx.coroutines.channels.Channel",
                    "kotlinx.coroutines.channels.SendChannel",
                    "kotlinx.coroutines.channels.ReceiveChannel",
                    "kotlinx.coroutines.channels.ChannelIterator",
                    "kotlinx.coroutines.selects.SelectInstance",
                    "kotlinx.coroutines.selects.SelectClause1",
                    "kotlinx.coroutines.selects.SelectClause2"
            ))
        }
    }

}

// attach the build angular code as a 'resource' so it is added to the jar
kotlin {
    sourceSets {
        val jvm8Main by getting {
            resources.srcDir(ngOutDir)
        }
    }
}