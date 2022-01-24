group = "com.jacobtread.mck.authlib"
version = "1.0.0"

plugins {

}

dependencies {
    implementation(project(":utils"))
    implementation(project(":logger"))
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.guava:guava:31.0.1-jre")
}
