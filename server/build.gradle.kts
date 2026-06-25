plugins {
    java
    application
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.osuserverlist"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("com.osuserverlist.bjar.App")
}

val lombokVersion = "1.18.46"
val javalinVersion = "7.2.2"
val okhttpVersion = "5.3.0"
val dotenvVersion = "5.2.2"
val toml4jVersion = "0.7.3"
val mysqlVersion = "9.7.0"
val hikariVersion = "7.0.2"
val jedisVersion = "7.5.2"
val ousuApiVersion = "2.1"
val osuNativeJarVersion = "0.0.9"
val bcprovVersion = "1.80"
val kotlinVersion = "1.9.24"
val classgraphVersion = "4.8.184"
val logbackVersion = "1.5.33"
val jacksonVersion = "2.21.2"
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

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    implementation("org.mvel:mvel2:$mvelVersion")
}

tasks.jar {
    archiveFileName.set("server.jar")

    manifest {
        attributes["Main-Class"] = "com.osuserverlist.bjar.App"
        attributes["Class-Path"] = configurations.runtimeClasspath.get()
            .files.joinToString(" ") { "lib/${it.name}" }
    }
}

tasks.register<Copy>("copyDeps") {
    from(configurations.runtimeClasspath)
    into("build/libs/lib")
}

tasks.build {
    dependsOn(tasks.jar, "copyDeps")
}