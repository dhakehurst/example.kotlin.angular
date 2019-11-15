import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSimple

val jsKotlin by configurations.creating {
    attributes {
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
    }
}

// define these locations because they are used in multiple places
val ngSrcDir = "${projectDir}/src/angular"
val ngOutDir = "${buildDir}/angular"

dependencies {

    jsKotlin(project(":information"))

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

tasks.create<DefaultTask>("ng_build") {
    group = "angular"
    dependsOn("unpack_kotlinJs")
    doLast {
        if (project.hasProperty("ng") && project.property("ng") == "false") {
        } else {
            YarnSimple.yarnExec(project.rootProject, file("${ngSrcDir}"), "ng_build", "run", "ng", "build", "--outputPath=${ngOutDir}/dist")
        }
    }
}