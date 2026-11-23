plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin { jvmToolchain(17) }

application { mainClass = "com.mercan.togu.robot.MainKt" }

dependencies {
    implementation(libs.jsoup)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}
