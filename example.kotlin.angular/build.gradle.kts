import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
plugins {
    kotlin("multiplatform") version ("1.3.50") apply false
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
  configure<KotlinMultiplatformExtension> {
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
