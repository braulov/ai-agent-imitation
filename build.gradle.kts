plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "com.braulov.aiagent"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}
application{
    mainClass.set("MainKt")
}
tasks.test {
    useJUnitPlatform()
}
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
kotlin {
    jvmToolchain(17)
}