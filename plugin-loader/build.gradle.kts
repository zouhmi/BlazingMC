plugins {
    java
}

dependencies {
    implementation(project(":api-compat"))
    
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.yaml:snakeyaml:2.3")
}