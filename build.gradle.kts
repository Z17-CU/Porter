// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion = "1.4.32"
    repositories {

        google()  // Google's Maven repository
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }

//        mavenCentral()
//        google()
//        jcenter()

//        maven { url = uri("https://maven.aliyun.com/repository/central") }
//        maven { url = uri("https://maven.aliyun.com/repository/google") }
//        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
//        maven { url = uri("https://jitpack.io") }

        //firebase
//        maven {
//            url = uri("https://s3.amazonaws.com/fabric-artifacts/public")
//        }
        //maven { url = uri("https://maven.fabric.io/public") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        //firebase
//        classpath("com.google.gms:google-services:4.3.3")
//        classpath("io.fabric.tools:gradle:1.31.2")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {

        google()  // Google's Maven repository
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
//        mavenCentral()
//        google()
////        jcenter()
//
//        maven { url = uri("https://maven.aliyun.com/repository/central") }
//        maven { url = uri("https://maven.aliyun.com/repository/google") }
//        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
//        maven { url = uri("https://jitpack.io") }

    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
