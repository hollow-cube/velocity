import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    application
    id("velocity-init-manifest")
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("com.velocitypowered.proxy.Velocity")
    applicationDefaultJvmArgs += listOf("-Dvelocity.packet-decode-logging=true");
}

tasks {
    withType<Checkstyle> {
        exclude("**/com/velocitypowered/proxy/protocol/packet/**")
    }

    jar {
        manifest {
            attributes["Implementation-Title"] = "Velocity"
            attributes["Implementation-Vendor"] = "Velocity Contributors"
            attributes["Multi-Release"] = "true"
        }
    }

    shadowJar {
        transform(Log4j2PluginsCacheFileTransformer::class.java)

        relocate("org.bstats", "com.velocitypowered.proxy.bstats")

        // Include Configurate 3
        val configurateBuildTask = project(":deprecated-configurate3").tasks.named("shadowJar")
        dependsOn(configurateBuildTask)
        from(zipTree(configurateBuildTask.map { it.outputs.files.singleFile }))
    }

    runShadow {
        workingDir = file("run").also(File::mkdirs)
        standardInput = System.`in`
    }
    named<JavaExec>("run") {
        workingDir = file("run").also(File::mkdirs)
        standardInput = System.`in` // Doesn't work?
    }
}

dependencies {
    implementation(project(":velocity-api"))
    implementation(project(":velocity-native"))
    implementation(project(":velocity-proxy-log4j2-plugin"))

    implementation(libs.bundles.log4j)
    implementation(libs.kyori.ansi)
    implementation(libs.netty.codec)
    implementation(libs.netty.codec.haproxy)
    implementation(libs.netty.codec.http)
    implementation(libs.netty.handler)
    implementation(libs.netty.transport.native.epoll)
    implementation(variantOf(libs.netty.transport.native.epoll) { classifier("linux-x86_64") })
    implementation(variantOf(libs.netty.transport.native.epoll) { classifier("linux-aarch_64") })
    implementation(libs.netty.transport.native.kqueue)
    implementation(variantOf(libs.netty.transport.native.kqueue) { classifier("osx-x86_64") })
    implementation(variantOf(libs.netty.transport.native.kqueue) { classifier("osx-aarch_64") })

    implementation(libs.jopt)
    implementation(libs.terminalconsoleappender)
    runtimeOnly(libs.jline)
    runtimeOnly(libs.disruptor)
    implementation(libs.fastutil)
    implementation(platform(libs.adventure.bom))
    implementation("net.kyori:adventure-nbt")
    implementation(libs.adventure.facet)
    implementation(libs.completablefutures)
    implementation(libs.nightconfig)
    implementation(libs.bstats)
    implementation(libs.lmbda)
    implementation(libs.asm)
    implementation(libs.bundles.flare)
    compileOnly(libs.spotbugs.annotations)
    compileOnly(libs.auto.service.annotations)
    testImplementation(libs.mockito)

    annotationProcessor(libs.auto.service)
}
