plugins {
    kotlin("jvm") version "1.3.72"
    id("com.vanniktech.maven.publish") version "0.12.0"
}

group = "dev.talkischeap"
version = "0.0.1-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
}
