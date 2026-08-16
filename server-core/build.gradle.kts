plugins {
    java
    application
}

application {
    mainClass.set("com.blazingmc.server.BlazingServer")
}

dependencies {
    implementation(project(":protocol"))
    implementation(project(":api-compat"))
    implementation(project(":world"))
    implementation(project(":plugin-loader"))
    
    implementation("io.netty:netty-all:4.1.115.Final")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.yaml:snakeyaml:2.3")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.blazingmc.server.BlazingServer"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}