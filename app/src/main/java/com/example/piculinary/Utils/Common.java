package com.example.piculinary.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Common {

    protected static boolean internetConnection(Context context) {
        boolean retVal;
        try {
            ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = null;

            if (connMan != null) {
                netInfo = connMan.getActiveNetworkInfo();
            }
            retVal = (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            retVal = false;
        }
        return retVal;
    }
}
