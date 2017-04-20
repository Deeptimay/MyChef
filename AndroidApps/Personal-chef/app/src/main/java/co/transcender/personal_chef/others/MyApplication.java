package co.transcender.personal_chef.others;

import android.app.Application;
import android.content.Context;

/**
 * Created by Deeptimay on 4/12/2017.
 */

public class MyApplication extends Application {


    static Context mContext;
    static MyApplication sInstance;
    private String TAG = "MyApplication";

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }

    public static synchronized MyApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        sInstance = this;
    }
}
