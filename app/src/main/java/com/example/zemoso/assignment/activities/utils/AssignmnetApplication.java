package com.example.zemoso.assignment.activities.utils;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by zemoso on 7/11/17.
 */

public class AssignmnetApplication extends Application {

    public static final String TAG = AssignmnetApplication.class.getSimpleName();

    public static RefWatcher getRefWatcher(Context context){
        AssignmnetApplication application = (AssignmnetApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;


    @Override
    public void onCreate() {
        super.onCreate();
        if(LeakCanary.isInAnalyzerProcess(this))
        {
            return;
        }
        refWatcher = LeakCanary.install(this);
        Realm.init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("VideoInfo.realm")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
