dependencies {
    // Ktor Networking
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Koin Dependency Injection
    implementation("io.insert-koin:koin-core:3.5.3")
    
    // Compose ViewModel Lifecycle (Needed for ViewModel support in commonMain)
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
}
