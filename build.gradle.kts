plugins {
    kotlin("jvm") version "2.1.20"
}

group = "org.example"
version = "1.0"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://repo.lucko.me") // ← dépôt de LuckPerms
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
    compileOnly("net.luckperms:api:5.4") // ← API officielle
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}


