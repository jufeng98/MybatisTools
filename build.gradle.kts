import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "org.javamaster.mybatis.tools"
version = "3.0.0"

repositories {
    maven { url = URI("https://maven.aliyun.com/nexus/content/groups/public/") }
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

allprojects {
    repositories {
        maven { url = URI("https://maven.aliyun.com/nexus/content/groups/public/") }
        mavenCentral()
    }
}

dependencies {
    intellijPlatform {
        create("IU", "2024.3")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        bundledModule("org.intellij.intelliLang")
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.database")
        bundledPlugin("com.intellij.spring")
        bundledPlugin("com.intellij.spring.boot")
        bundledPlugin("org.jetbrains.kotlin")
    }

    implementation("com.softwareloop:mybatis-generator-lombok-plugin:1.0")
    implementation("io.github.kostaskougios:cloning:1.13.0")
    implementation("org.mybatis.generator:mybatis-generator-core:1.4.0")
    implementation("org.freemarker:freemarker:2.3.35")
    implementation("com.itranswarp:compiler:1.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("org.jetbrains:annotations:26.1.0")


    compileOnly("org.projectlombok:lombok:1.18.48")

    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testImplementation("junit:junit:4.13.1")
    testImplementation("commons-io:commons-io:2.22.0")
}

sourceSets["main"].java.srcDirs("src/main/gen")

intellijPlatform {
    buildSearchableOptions = false
    autoReload = false
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "232"
            untilBuild = provider { null }
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    runIde {
        autoReload = false
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
