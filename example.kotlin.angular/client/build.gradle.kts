import net.akehurst.kotlin.kt2ts.plugin.gradle.GenerateDeclarationsTask
import net.akehurst.kotlin.kt2ts.plugin.gradle.GeneratePackageJsonTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSimple

plugins {
    id("net.akehurst.kotlin.kt2ts-plugin") version "1.0.0"
}

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

    jsKotlin(project(":gui2core"))
    jsKotlin(project(":websocketClient"))

    //add this so that the kt2ts plugin can find the jvm classes
    jvm8MainImplementation(project(":gui2core"))
}

val moduleNameMaping = mapOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-js" to "kotlinx-coroutines-core",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core" to "kotlinx-coroutines-core",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-common" to "kotlinx-coroutines-core-common",
        "org.jetbrains.kotlinx:kotlinx-coroutines-io-js" to "kotlinx-io-kotlinx-coroutines-io",
        "org.jetbrains.kotlinx:kotlinx-coroutines-io" to "kotlinx-io-kotlinx-coroutines-io",
        "org.jetbrains.kotlinx:kotlinx-io-js" to "kotlinx-io",
        "org.jetbrains.kotlinx:kotlinx-io" to "kotlinx-io",
        "org.jetbrains.kotlinx:atomicfu-common" to "kotlinx-atomicfu",
        "org.jetbrains.kotlinx:atomicfu-js" to "kotlinx-atomicfu",
        "org.jetbrains.kotlinx:atomicfu" to "kotlinx-atomicfu",
        "io.ktor:ktor-http-cio-js" to "ktor-ktor-http-cio",
        "io.ktor:ktor-http-cio" to "ktor-ktor-http-cio",
        "io.ktor:ktor-client-core-js" to "ktor-ktor-client-core",
        "io.ktor:ktor-client-core" to "ktor-ktor-client-core",
        "io.ktor:ktor-client-websockets-js" to "ktor-ktor-client-websockets",
        "io.ktor:ktor-client-websockets" to "ktor-ktor-client-websockets",
        "io.ktor:ktor-http-js" to "ktor-ktor-http",
        "io.ktor:ktor-http" to "ktor-ktor-http",
        "io.ktor:ktor-utils-js" to "ktor-ktor-utils",
        "io.ktor:ktor-utils" to "ktor-ktor-utils"
)

kt2ts {
    kotlinStdlibJsDir.set(file("${ngSrcDir}/node_modules/kotlin"))
    nodeModulesDirectoryPath.set("${ngSrcDir}/node_modules")
    unpackConfigurationName.set("jsKotlin")
    moduleNameMap.set(moduleNameMaping)
}

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

/*
tasks.create<Copy>("unpack_kotlinJs") {
    group = "angular"
    //dependsOn(jsKotlin, "yarn_install")
    jsKotlin.resolvedConfiguration.resolvedArtifacts.forEach { dep ->
        println("unpacking ${dep.name}")
        val dn = dep.name.substringBeforeLast("-")
        val tgtName = thirdPartyDeps[dep.name]?.get("tgtName") as String? ?: "${dep.moduleVersion.id.group}-${dn}"
        from(zipTree(dep.file)) {
            includeEmptyDirs = false
            include { fileTreeElement ->
                val path = fileTreeElement.path
                (path.endsWith(".js") || path.endsWith("d.ts") || path.endsWith("package.json"))
                        && (path.startsWith("META-INF/resources/") || !path.startsWith("META-INF/"))
            }
            into(tgtName)
        }
        into("${ngSrcDir}/node_modules")
    }
}
*/
//convert kotlinx.coroutins into a ts module
jsKotlin.resolvedConfiguration.resolvedArtifacts.forEach { dep ->
    if (thirdPartyDeps.containsKey(dep.name)) {
        val dn = dep.name.substringBeforeLast("-")
        val tgtName = thirdPartyDeps[dep.name]?.get("tgtName") as String? ?: "${dep.moduleVersion.id.group}-${dn}"
        tasks.create<GenerateDeclarationsTask>("generateDeclarationsFor_${dep.name}") {
            group ="generate"
            dependsOn("unpack_kotlin_js")
            if (null!=thirdPartyDeps[dep.name]!!["group"]) moduleGroup.set(thirdPartyDeps[dep.name]!!["group"] as String)
            if (null!=thirdPartyDeps[dep.name]!!["name"]) moduleName.set(thirdPartyDeps[dep.name]!!["name"] as String)
            moduleNameMap.set(moduleNameMaping)
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

