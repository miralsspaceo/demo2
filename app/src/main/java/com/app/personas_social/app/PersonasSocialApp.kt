package com.app.personas_social.app

import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions


class PersonasSocialApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
/*        val options = FirebaseOptions.Builder()
            .setApplicationId("1:61701853821:android:22033f1f9449ae0cfb5714")
            .setDatabaseUrl("https://personas-music.firebaseio.com/")
            .build()
        FirebaseApp.initializeApp(this, options, "personas-music")*/

    }


}