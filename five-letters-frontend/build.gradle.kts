val gradleNodePluginVersion = "1.3.1"

group = "com.kilchichakov"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
}
dependencies {
    
    implementation("com.moowork.gradle:gradle-node-plugin:$gradleNodePluginVersion")
}

plugins {
    java
    id("base")
    id("com.moowork.node") version "1.3.1"
}

node {
    // Version of node to use.

    version = "10.16.0"

    // Version of npm to use.

    npmVersion = "6.9.0"

    // If true, it will download node using above parameters.

    // If false, it will try to use globally installed node.

    download = true

}