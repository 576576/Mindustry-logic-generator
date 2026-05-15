plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "9.3.0"
}

val version: String by project

group = "cn.sumitm.mdtc"

// ==================== Mindustry Mod 属性 ====================
val mindustryVersion = "v157"  // 目标 Mindustry 构建版本号
val useLatestMindustry = false // 设为 true 则始终依赖最新 BE 构建
val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    // Mindustry 依赖：从 GitHub Releases 下载 JAR，不使用传统 Maven 仓库
    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/[revision]/dependencies.jar")
        }
        metadataSources { artifact() }
    }

    // Mindustry BE (Bleeding Edge) 最新构建
    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/master/[revision].jar")
        }
        metadataSources { artifact() }
    }
}

dependencies {
    implementation("info.picocli:picocli:4.7.7")

    // Mindustry 编译时依赖（mod 开发用，不打包进最终产物）
    if (useLatestMindustry) {
        compileOnly("Anuken:MindustryBuilds:latest")
    } else {
        compileOnly("Anuken:Mindustry:$mindustryVersion")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("cn.sumitm.mdtc.cli.Main")
}

// ==================== CLI 工具构建 ====================

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "cn.sumitm.mdtc.cli.Main"
    }
    archiveFileName.set("${project.name}-${project.version}-all.jar")
}

tasks.processResources {
    filesMatching("**/version.properties") {
        expand("version" to project.version)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

// ==================== Mindustry Mod 构建任务 ====================

/**
 * 构建 Desktop 版 Mindustry mod JAR。
 * 包含：编译产物、运行时依赖、mod.hjson、assets/
 */
tasks.register<Jar>("jarMod") {
    group = "mindustry mod"
    description = "Build desktop Mindustry mod JAR (includes mod.hjson + assets/)"
    archiveFileName.set("${project.name}Desktop.jar")

    from(sourceSets.main.get().output)

    // 打包运行时依赖
    from({
        configurations.runtimeClasspath.get()
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
    })

    // 打包 mod.hjson（位于项目根目录）
    from(rootDir) {
        include("mod.hjson")
    }

    // 打包 assets 目录
    from(rootDir.resolve("assets")) {
        into("assets")
        include("**")
    }
}

/**
 * 构建 Android 版 Mindustry mod JAR。
 * 使用 d8 进行 dex 脱糖处理，需要 Android SDK 且 d8 在 PATH 中。
 */
tasks.register("jarAndroidMod") {
    group = "mindustry mod"
    description = "Build Android Mindustry mod JAR (requires Android SDK + d8 in PATH)"
    dependsOn("jarMod")

    doLast {
        if (sdkRoot == null || !File(sdkRoot).exists()) {
            throw GradleException(
                "No valid Android SDK found. Ensure that ANDROID_HOME " +
                "is set to your Android SDK directory."
            )
        }

        // 查找已安装的 Android platform（取最高版本）
        val platformsDir = File("$sdkRoot/platforms/")
        val platformRoot = platformsDir.listFiles()
            ?.sortedByDescending { it.name }
            ?.firstOrNull { File(it, "android.jar").exists() }
            ?: throw GradleException(
                "No android.jar found. Ensure that you have an Android platform installed."
            )

        // 收集 d8 所需 classpath
        val classpathFiles = configurations.compileClasspath.get().files +
            configurations.runtimeClasspath.get().files +
            listOf(File(platformRoot, "android.jar"))

        val d8 = if (isWindows) "d8.bat" else "d8"

        val pb = ProcessBuilder(
            listOf(d8) +
                classpathFiles.flatMap { listOf("--classpath", it.absolutePath) } +
                listOf(
                    "--min-api", "30",
                    "--output", "${project.name}Android.jar",
                    "${project.name}Desktop.jar"
                )
        )
        pb.directory(File("build/libs"))
        pb.inheritIO()
        val exitCode = pb.start().waitFor()
        if (exitCode != 0) {
            throw GradleException("d8 dexing failed with exit code $exitCode")
        }
    }
}

/**
 * 部署 Mindustry mod：合并 Desktop + Android JAR 为最终产物。
 */
tasks.register<Jar>("deployMod") {
    group = "mindustry mod"
    description = "Deploy combined Mindustry mod JAR (Desktop + Android)"
    dependsOn("jarMod", "jarAndroidMod")
    archiveFileName.set("${project.name}.jar")

    from({
        listOf(
            zipTree("build/libs/${project.name}Desktop.jar"),
            zipTree("build/libs/${project.name}Android.jar")
        )
    })

    doLast {
        // 清理中间 Android JAR
        delete("build/libs/${project.name}Android.jar")
    }
}

