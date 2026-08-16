plugins {
    java
}

dependencies {
    implementation(project(":protocol"))
    implementation(project(":api-compat"))
    
    implementation("io.netty:netty-all:4.1.115.Final")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("org.slf4j:slf4j-api:2.0.16")
}