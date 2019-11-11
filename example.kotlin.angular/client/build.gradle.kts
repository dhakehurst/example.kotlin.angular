import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages

val jsKotlin by configurations.creating {
    attributes {
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
    }
}

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