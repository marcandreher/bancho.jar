import java.time.Instant

plugins {
    java
    application
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.osuserverlist"
version = "1.1.4-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass.set("com.osuserverlist.bjar.App")
}

val lombokVersion = "1.18.46"
val javalinVersion = "7.2.2"
val okhttpVersion = "5.4.0"
val dotenvVersion = "5.2.2"
val toml4jVersion = "0.7.3"
val mysqlVersion = "9.7.0"
val hikariVersion = "7.1.0"
val jedisVersion = "7.5.3"
val ousuApiVersion = "2.1"
val osuNativeJarVersion = "0.0.9"
val bcprovVersion = "1.84"
val kotlinVersion = "2.3.21"
val classgraphVersion = "4.8.184"
val logbackVersion = "1.5.38"
val jacksonVersion = "3.2.1"
val mvelVersion = "2.5.2.Final"

dependencies {
    implementation(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    implementation("io.github.cdimascio:java-dotenv:$dotenvVersion")
    implementation("io.hotmoka:toml4j:$toml4jVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("io.github.classgraph:classgraph:$classgraphVersion")

    implementation("com.mysql:mysql-connector-j:$mysqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    implementation("com.squareup.okhttp3:okhttp-jvm")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("io.javalin:javalin:$javalinVersion")

    implementation("com.github.marcandreher:Ousu-Api:$ousuApiVersion")

    implementation("org.bouncycastle:bcprov-jdk18on:$bcprovVersion")

    implementation("io.github.7mochi:osu-native-jar:$osuNativeJarVersion")

    implementation("redis.clients:jedis:$jedisVersion")

    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")

    implementation("org.mvel:mvel2:$mvelVersion")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks.shadowJar) {
                classifier = "all"
            }
        }
    }
}

val generateBuildProperties by tasks.registering(WriteProperties::class) {
    destinationFile = layout.buildDirectory.file("generated/resources/main/build.properties")

    property("name", project.name)
    property("group", project.group)
    property("version", project.version)
    property("buildTime", Instant.now().toString())
    property("javaVersion", JavaVersion.current())
    property("gradleVersion", gradle.gradleVersion)
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated/resources/main"))
}

tasks.processResources {
    dependsOn(generateBuildProperties)
}

tasks.jar {
    archiveFileName.set("server.jar")

    manifest {
        attributes["Main-Class"] = "com.osuserverlist.bjar.App"
        attributes["Class-Path"] = configurations.runtimeClasspath.get()
            .files.joinToString(" ") { "lib/${it.name}" }
    }
}

tasks.shadowJar {
    archiveFileName.set("server-shaded.jar")

    manifest {
        attributes["Main-Class"] = "com.osuserverlist.bjar.App"
    }
}

tasks.register<Copy>("copyDeps") {
    from(configurations.runtimeClasspath)
    into("build/libs/lib")
}

tasks.build {
    dependsOn(tasks.jar, "copyDeps")
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("Xdoclint:none", true)
        addStringOption("encoding", "UTF-8")
        links("https://docs.oracle.com/en/java/javase/25/docs/api/")
    }

    isFailOnError = false
}