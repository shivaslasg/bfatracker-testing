package sg.onemap.bfatracker

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import net.danlew.android.joda.JodaTimeAndroid
import sg.onemap.bfatracker.utilities.BfaTrackerModule

class BfaTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Realm. Should only be done once when the application starts.
        JodaTimeAndroid.init(this);
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .name("bfatracker.realm")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .modules(BfaTrackerModule())
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
    }
}