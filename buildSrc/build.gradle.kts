plugins {
    `kotlin-dsl`
    "groovy"
    "java-gradle-plugin"
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

repositories {
    jcenter()
}

dependencies {
    api("net.researchgate:gradle-release:2.6.0")
}
