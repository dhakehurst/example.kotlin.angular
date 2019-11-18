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

kt2ts {
    kotlinStdlibJsDir.set(file("${ngSrcDir}/node_modules/kotlin"))
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

tasks.create<Copy>("unpack_kotlinJs") {
    group = "angular"
    dependsOn(jsKotlin, "yarn_install")
    jsKotlin.resolvedConfiguration.resolvedArtifacts.forEach { dep ->
        println("unpacking ${dep.name}")
        val dn = dep.name.substringBeforeLast("-")
        val tgtName = "${dep.moduleVersion.id.group}-${dn}"   // this name may not be correct for some modules but it appears to be somewhat of a default
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

val thirdPartyDeps = mapOf(
        "ktor-http-cio-js" to mapOf(
                "classPatterns" to listOf<String>(),
                "moduleOnly" to listOf("ktor-http-cio-jvm")
        ),
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
                "group" to "org.jetbrains.kotlinx",
                "name" to "kotlinx-coroutines-core",
                "mainFileName" to "kotlinx-coroutines-core.js",
                "tgtName" to "org.jetbrains.kotlinx-kotlinx-coroutines-core"
        )
)
//convert kotlinx.coroutins into a ts module
jsKotlin.resolvedConfiguration.resolvedArtifacts.forEach { dep ->
    if (thirdPartyDeps.containsKey(dep.name)) {
        val dn = dep.name.substringBeforeLast("-")
        val tgtName = thirdPartyDeps[dep.name]!!["tgtName"] as String? ?: "${dep.moduleVersion.id.group}-${dn}"
        tasks.create<GenerateDeclarationsTask>("generateDeclarationsFor_${dep.name}") {
            group ="generate"
            dependsOn("unpack_kotlinJs")
            if (null!=thirdPartyDeps[dep.name]!!["group"]) moduleGroup.set(thirdPartyDeps[dep.name]!!["group"] as String)
            if (null!=thirdPartyDeps[dep.name]!!["name"]) moduleName.set(thirdPartyDeps[dep.name]!!["name"] as String)
            //overwrite.set(false)
            localOnly.set(false)
            moduleOnly.set(thirdPartyDeps[dep.name]!!["moduleOnly"] as List<String>)
            modulesConfigurationName.set("jvm8RuntimeClasspath")
            declarationsFile.set(file("${ngSrcDir}/node_modules/${tgtName}/${dep.moduleVersion.id.group}-${dn}-js.d.ts"))
            classPatterns.set(thirdPartyDeps[dep.name]!!["classPatterns"] as List<String>)
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
    dependsOn("unpack_kotlinJs")
    dependsOn("addKotlinStdlibDeclarations")
    doLast {
        if (project.hasProperty("ng") && project.property("ng") == "false") {
        } else {
            YarnSimple.yarnExec(project.rootProject, file("${ngSrcDir}"), "ng_build", "run", "ng", "build", "--outputPath=${ngOutDir}/dist")
        }
    }
}

tasks.getByName("jvm8ProcessResources").dependsOn("ng_build")

