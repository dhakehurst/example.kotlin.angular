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

val version_coroutines: String by project

dependencies {

    // only need a direct dependency on these, the rest are transitively discovered
    nodeKotlin(project(":gui2core"))
    nodeKotlin(project(":websocketClient"))

}

// define these locations because they are used in multiple places
val ngSrcDir = project.layout.projectDirectory.dir("src/angular")
val ngOutDir = project.layout.buildDirectory.dir("angular")

// need a newer version of node than the default used by kotlin-js.
// the default version runs out of memory with obscure error message:-
//     "An unhandled exception occurred: Call retries were exceeded"
project.rootProject.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
    nodeVersion = "13.2.0"
}

kt2ts {
    nodeSrcDirectory.set(ngSrcDir)
    nodeOutDirectory.set(ngOutDir)

    // adding -PngProd to the gradle build command gives us a production build of the angular code
    nodeBuildCommand.set(
            if (project.hasProperty("prod")) {
                listOf("ng", "build", "--prod", "--outputPath=${ngOutDir}/dist")
            } else {
                listOf("ng", "build", "--outputPath=${ngOutDir}/dist")
            }
    )

    // we use a different (to default) name for the kotlin-jvm target
    jvmTargetName.set("jvm8")

    // the ':information' module and classes are accessed by reflection (during de/- serialisation)
    dynamicImport.set(listOf(
            "${project.group}:information"
    ))

    // generate .d.ts for coroutines
    // required because coroutines are exposed by classes in gui2core
    generateThirdPartyModules {
        register("org.jetbrains.kotlinx:kotlinx-coroutines-core:$version_coroutines") {
            includeOnly.set(listOf("org.jetbrains.kotlinx:kotlinx-coroutines-core"))
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
project.tasks.getByName("jvm8ProcessResources").dependsOn("nodeBuild")
kotlin {
    sourceSets {
        val jvm8Main by getting {
            resources.srcDir(ngOutDir)
        }
    }
}