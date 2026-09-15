plugins {
    // AGP 8.5 / Kotlin 1.9 (the original scaffold) can't run on the Gradle 9.3 + JDK 25
    // toolchain this machine has. Kotlin 2.x moves the Compose compiler into its own
    // plugin, so composeOptions.kotlinCompilerExtensionVersion goes away in app/.
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20" apply false
}
