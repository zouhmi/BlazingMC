plugins {
    java
}

dependencies {
    implementation(project(":api-compat"))
    
    implementation("io.netty:netty-all:4.1.115.Final")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.12")
}