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
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSimple

plugins {
    id("net.akehurst.kotlin.kt2ts") version "1.0.0"
}

// define and configure a gradle-dependency-configuration for the kotlin modules that need to
// be unpacked and added to the angular build
val jsKotlin by configurations.creating {
    attributes {
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_RUNTIME))
    }
}

// define these locations because they are used in multiple places
val ngSrcDir = "${projectDir}/src/angular"
val ngOutDir = "${buildDir}/angular"

dependencies {

    // only need a direct dependency on these, the rest are transitively discovered
    jsKotlin(project(":gui2core"))
    jsKotlin(project(":websocketClient"))

    //add this so that the kt2ts plugin can find the jvm classes
    // it needs the jvm modules/classes because it uses jvm reflection to generate .d.ts files
    jvm8MainImplementation(project(":gui2core"))
}

kt2ts {
    kotlinStdlibJsDir.set(file("${ngSrcDir}/node_modules/kotlin"))
    nodeModulesDirectoryPath.set("${ngSrcDir}/node_modules")
    unpackConfigurationName.set("jsKotlin")
}

// attach the build angular code as a 'resource' so it is added to the jar
kotlin {
    sourceSets {
        val jvm8Main by getting {
            resources.srcDir("${ngOutDir}")
        }
    }
}

// the kotlin plugin provides the code for these tasks, but we need to instantiate them
tasks.create<NodeJsSetupTask>(NodeJsSetupTask.NAME) { group = "nodejs" }
tasks.create<YarnSetupTask>(YarnSetupTask.NAME) {
    group = "nodejs"
    if (this.destination.exists()) {
        // assume that already downloaded
    } else {
        this.setup()
    }
}

// use yarn to install the node_modules required by the angular code
tasks.create<DefaultTask>("yarn_install") {
    group = "angular"
    dependsOn(NodeJsSetupTask.NAME, YarnSetupTask.NAME)
    doLast {
        val nodeJs = NodeJsRootPlugin.apply(project.rootProject)
        YarnSimple.yarnExec(project.rootProject, file("${ngSrcDir}"), "yarn_install", "install", "--cwd", "--no-bin-links")
    }
}
tasks.getByName("unpack_kotlin_js").dependsOn(jsKotlin, "yarn_install")

// we need a .d.ts file for 'kotlinx-coroutines-core' because it is referenced by
// Core2Gui and Gui2Core
val thirdPartyDeps = mapOf(
        "kotlinx-coroutines-core-js" to mapOf(
                "classPatterns" to listOf(
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
                ),
                "moduleOnly" to listOf("kotlinx-coroutines-core"),
                "group" to "",
                "name" to "kotlinx-coroutines-core",
                "mainFileName" to "kotlinx-coroutines-core.js",
                "tgtName" to "kotlinx-coroutines-core"
        )
)

jsKotlin.resolvedConfiguration.resolvedArtifacts.forEach { dep ->
    if (thirdPartyDeps.containsKey(dep.name)) {
        val dn = dep.name.substringBeforeLast("-")
        val tgtName = thirdPartyDeps[dep.name]?.get("tgtName") as String? ?: "${dep.moduleVersion.id.group}-${dn}"
        tasks.create<GenerateDeclarationsTask>("generateDeclarationsFor_${dep.name}") {
            group ="generate"
            dependsOn("unpack_kotlin_js")
            if (null!=thirdPartyDeps[dep.name]!!["group"]) moduleGroup.set(thirdPartyDeps[dep.name]!!["group"] as String)
            if (null!=thirdPartyDeps[dep.name]!!["name"]) moduleName.set(thirdPartyDeps[dep.name]!!["name"] as String)
            classPatterns.set(thirdPartyDeps[dep.name]!!["classPatterns"] as List<String>)
            //overwrite.set(false)
            localOnly.set(false)
            moduleOnly.set(thirdPartyDeps[dep.name]!!["moduleOnly"] as List<String>)
            modulesConfigurationName.set("jvm8RuntimeClasspath")
            declarationsFile.set(file("${ngSrcDir}/node_modules/${tgtName}/${dep.moduleVersion.id.group}-${dn}-js.d.ts"))
        }
        tasks.create<GeneratePackageJsonTask>("ensurePackageJsonFor_${dep.name}") {
            group ="generate"
            dependsOn("generateDeclarationsFor_${dep.name}")
            packageJsonDir.set(project.file("${ngSrcDir}/node_modules/${tgtName}"))
            moduleGroup.set(dep.moduleVersion.id.group)
            moduleName.set(dep.name)
            if (null!=thirdPartyDeps[dep.name]!!["mainFileName"]) mainFileName.set((thirdPartyDeps[dep.name]!!["mainFileName"] as String))
            moduleVersion.set(dep.moduleVersion.id.version)
        }
    }
}

// finally, add and connect into the build a task that initiates the angular build
tasks.create<DefaultTask>("ng_build") {
    group = "angular"
    dependsOn("unpack_kotlin_js")
    dependsOn("addKotlinStdlibDeclarations")
    doLast {
        if (project.hasProperty("ng") && project.property("ng") == "false") {
        } else {
            YarnSimple.yarnExec(project.rootProject, file("${ngSrcDir}"), "ng_build", "run", "ng", "build", "--outputPath=${ngOutDir}/dist")
        }
    }
}

tasks.getByName("jvm8ProcessResources").dependsOn("ng_build")

