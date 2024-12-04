plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.piperbloom.proxyvpn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.piperbloom.proxyvpn"
        minSdk = 21
        targetSdk = 34

//        Release Version
        versionCode = 7
        versionName = "1.0.7"

//        TEST VERSION
//        versionCode = 7
//        versionName = "1.0.7"

        multiDexEnabled = true
        splits.abi {
            reset()
            include(
                "arm64-v8a",
                "armeabi-v7a",
                "x86_64",
                "x86"
            )
        }

        buildConfigField("String", "BASE_URL", "\"https://vmi2148783.contaboserver.net/\"")
        buildConfigField("String", "BASE_URL1", "\"https://letslearntogetherenglish.online/\"")
        buildConfigField("String", "BASE_URL2", "\"https://hostyourbiggestideasandlearnnewthings.online/\"")
        buildConfigField("String", "BASE_URL3", "\"https://unlimitedlearningopportunitieswithhosting.store/\"")
        buildConfigField("String", "ACTIVATION_URL", "\"api/v1/regular-users/activation\"")
        buildConfigField("String", "SERVER_BATCH_URL", "\"api/v1/server-manager/batch\"")
        buildConfigField("String", "SOCIAL_URL", "\"api/v1/social\"")
        buildConfigField("String", "SUPPORT_URL", "\"api/v1/support\"")
        buildConfigField("String", "VERSION_URL", "\"api/v1/version\"")
        buildConfigField("String", "PRE_VERSION_URL", "\"api/v1/pre-version\"")

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isMinifyEnabled = false

        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }


    splits {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }

        splits {
            abi {
                isEnable = true
                isUniversalApk = true
            }
        }

        applicationVariants.all {
            val variant = this
            val versionCodes =
                mapOf("armeabi-v7a" to 4, "arm64-v8a" to 4, "x86" to 4, "x86_64" to 4, "all" to 4)

            variant.outputs
                .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
                .forEach { output ->
                    val abi = if (output.getFilter("ABI") != null)
                        output.getFilter("ABI")
                    else
                        "all"

                    output.outputFileName = "Piper_${variant.versionName}_${abi}.apk"
                    if (versionCodes.containsKey(abi)) {
                        output.versionCodeOverride =
                            (1000000 * versionCodes[abi]!!).plus(variant.versionCode)
                    } else {
                        return@forEach
                    }
                }
        }

        buildFeatures {
            viewBinding = true
            buildConfig = true
        }

        packaging {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }

    dependencies {
        implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
        testImplementation("junit:junit:4.13.2")

        // Androidx
        implementation("androidx.constraintlayout:constraintlayout:2.2.0")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("androidx.preference:preference-ktx:1.2.1")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.fragment:fragment-ktx:1.8.5")
        implementation("androidx.multidex:multidex:2.0.1")
        implementation("androidx.viewpager2:viewpager2:1.1.0")

        // Androidx ktx
        implementation("androidx.activity:activity-ktx:1.9.3")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

        //kotlin
        implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.20")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

        implementation("com.tencent:mmkv-static:1.3.9")
        implementation("com.google.code.gson:gson:2.11.0")
        implementation("io.reactivex:rxjava:1.3.8")
        implementation("io.reactivex:rxandroid:1.2.1")
        implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
        implementation("io.reactivex.rxjava3:rxjava:3.1.5")
        implementation("com.github.tbruyelle:rxpermissions:0.12@aar")
        implementation("me.drakeet.support:toastcompat:1.1.0")
        implementation("com.blacksquircle.ui:editorkit:2.9.0")
        implementation("com.blacksquircle.ui:language-base:2.9.0")
        implementation("com.blacksquircle.ui:language-json:2.9.0")
        implementation("io.github.g00fy2.quickie:quickie-bundled:1.10.0")
        implementation("com.google.zxing:core:3.5.3")

        implementation("androidx.work:work-runtime-ktx:2.8.1")
        implementation("androidx.work:work-multiprocess:2.8.1")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")

        implementation("com.google.guava:guava:31.1-android")

        implementation("com.github.bumptech.glide:glide:4.15.1")
        annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

        implementation("com.caverock:androidsvg:1.4")
        implementation("com.airbnb.android:lottie:6.0.0")

        implementation("cat.ereza:customactivityoncrash:2.3.0")

        // Network interceptor
        debugImplementation ("com.github.chuckerteam.chucker:library:4.0.0")
        releaseImplementation ("com.github.chuckerteam.chucker:library-no-op:4.0.0")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")

    }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("org.chromium.net:cronet-embedded:119.6045.31")
}
