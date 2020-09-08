import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.konan.properties.Properties

plugins {

    id("com.android.application")
    id("io.fabric")
    id("com.google.gms.google-services")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)

    val versionPropsFile = file("version.properties")

    if (versionPropsFile.canRead()) {
        val versionProps = Properties()

        versionProps.load(versionPropsFile.inputStream())
        val name = versionProps.getProperty("VERSION_NAME")
        val code = versionProps.getProperty("VERSION_CODE").toInt() + 1
        val build = versionProps.getProperty("VERSION_BUILD").toInt() + 1

        defaultConfig {
            applicationId = "cu.control.queue"
            minSdkVersion(19)
            targetSdkVersion(28)
            versionCode = code
            versionName = "$name.$build"
            testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
            multiDexEnabled = true
            vectorDrawables.useSupportLibrary = true
        }
    } else {
        throw GradleException("Could not read version.properties!")
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.properties["KEYSTORE_PORTERO"] ?: "")
            storePassword = project.properties["KEYSTORE_PASSWORD_PORTERO"]?.toString() ?: ""
            keyAlias = project.properties["KEY_ALIAS_PORTERO"]?.toString() ?: ""
            keyPassword = project.properties["KEY_PASSWORD_PORTERO"]?.toString() ?: ""
        }
    }

    buildTypes {

        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")

            signingConfig = signingConfigs.getByName("release")
            resValue("string", "content_provider", "cu.control.queue.android.provider")

            buildConfigField(
                "String",
                "BASE_URL",
                project.properties["BASE_URL"]?.toString() ?: ""
            )
            buildConfigField(
                "String",
                "PORTER_SERIAL_KEY",
                "\"${project.properties["PORTER_SERIAL_KEY"]?.toString() ?: ""}\""
            )
        }

        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")

            buildConfigField(
                "String",
                "BASE_URL",
                project.properties["BASE_URL"]?.toString() ?: ""
            )
            buildConfigField(
                "String",
                "PORTER_SERIAL_KEY",
                "\"${project.properties["PORTER_SERIAL_KEY"]?.toString() ?: ""}\""
            )
        }
    }

    applicationVariants.all {
        println("applicationVariants: ${this.name}")

        if (buildType.name == "release") {
            updateVersionCode()
        }

        outputs.map { it as BaseVariantOutputImpl }.forEach {
            it.outputFileName = it.outputFileName
                .replace("app-", "Porter@-")
                .replace(".apk", "-${versionName}.${versionCode}.apk")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

kapt {
    generateStubs = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.preference:preference:1.1.1")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")

    implementation("com.google.android.material:material:1.3.0-alpha01")

    implementation("com.karumi:dexter:6.0.2")

    implementation("androidx.room:room-runtime:2.2.5")
    kapt("androidx.room:room-compiler:2.2.5")
    implementation("androidx.room:room-ktx:2.2.5")
    implementation("androidx.room:room-rxjava2:2.2.5")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")

    implementation("com.google.code.gson:gson:2.8.6")

    implementation("com.google.dagger:dagger-android:2.27")
    implementation("com.google.dagger:dagger-android-support:2.27")
    kapt("com.google.dagger:dagger-android-processor:2.27")
    kapt("com.google.dagger:dagger-compiler:2.27")

    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.navigation:navigation-runtime-ktx:2.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")

    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation("org.ocpsoft.prettytime:prettytime:4.0.3.Final")

    implementation("io.reactivex.rxjava3:rxjava:3.0.0")

    /**RX*/
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    /**retrofit*/
    implementation("com.squareup.okhttp3:okhttp:4.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.8.1")
    implementation("com.squareup.retrofit2:converter-gson:2.8.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.4.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.1.0")

    /**Circular progress bar*/
    implementation("com.github.castorflex.smoothprogressbar:library-circular:1.3.0")

    /** Logger Timber */
    implementation("com.jakewharton.timber:timber:4.7.1")

    /** Glide */
    implementation("com.github.bumptech.glide:glide:4.11.0")

    /** Qr Reader */
    implementation("me.dm7.barcodescanner:zxing:1.9.8")

    /**circularProgressBar*/
    implementation("com.github.castorflex.smoothprogressbar:library-circular:1.3.0")

    /**circular imagenView*/
    implementation("com.mikhaellopez:circularimageview:3.2.0")

    /**Rounded Buttons*/
    implementation("com.github.medyo:fancybuttons:1.9.1")

    /**PDF*/
    implementation("com.itextpdf:itextpdf:5.0.6")

    /**App intro*/
    implementation("com.github.AppIntro:AppIntro:5.1.0")

    /** Fragmentation */
    implementation("me.yokeyword:fragmentationx:1.0.1")
    implementation("me.yokeyword:fragmentationx-swipeback:1.0.1")

    /**Preferences*/
    implementation("com.takisoft.preferencex:preferencex:1.1.0")

    /** Firebase */
    implementation("com.google.firebase:firebase-analytics:17.4.4")
    implementation("com.crashlytics.sdk.android:crashlytics:2.10.1")

    /** ScrollView */
    implementation("com.github.turing-tech:MaterialScrollBar:13.3.2")

    /** Range Bar */
    implementation("me.bendik.simplerangeview:simplerangeview:0.2.0")

   /**download*/
    implementation ("com.download.library:Downloader:4.1.4")
    /**PrDownload*/
    implementation ("com.mindorks.android:prdownloader:0.6.0")

    /**searchSpinner*/
    implementation ("com.toptoche.searchablespinner:searchablespinnerlibrary:1.3.1")
    implementation ("com.github.jrizani:JRSpinner:1.0.0")
    implementation ("com.github.jrizani:JRSpinner:androidx-SNAPSHOT")



}

fun updateVersionCode() {

    val versionPropsFile = file("version.properties")

    if (versionPropsFile.canRead()) {
        val versionProps = Properties()

        versionProps.load(versionPropsFile.inputStream())
        val name = versionProps.getProperty("VERSION_NAME")
        val code = versionProps.getProperty("VERSION_CODE").toInt() + 1
        val build = versionProps.getProperty("VERSION_BUILD").toInt() + 1

        versionProps["VERSION_NAME"] = name
        versionProps["VERSION_BUILD"] = build.toString()
        versionProps["VERSION_CODE"] = code.toString()
        versionProps.store(versionPropsFile.outputStream(), null)
    } else {
        throw GradleException("Could not read version.properties!")
    }
}
