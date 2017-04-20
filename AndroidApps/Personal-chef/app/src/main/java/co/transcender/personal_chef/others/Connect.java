package co.transcender.personal_chef.others;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Connect {

    static final String TAG = "Connect";
    Context context;

    public Connect(Context context) {
        this.context = context;
    }

    public boolean isConnect() {
        ConnectivityManager cm;
        NetworkInfo netInfo;
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            netInfo = cm.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected())
                return true;

        } catch (Exception e) {
            Log.w(TAG, "e.getMessage()-->> " + e.getMessage());
        }
        return false;
    }
}