package com.example.androidapp.provider;

import android.net.Uri;

public class LilliContract {
    public static final int OBJECTS_ID = 1;

    public static final class Objects {
        public static final String ID = "_ID";
        public static final String AUTHORITATIVE_LOCATION = "authoritative_location";
        public static final String DATA = "_data";
        public static final Uri CONTENT_URI = Uri.parse("content://com.example.androidapp.provider/objects");
    }
}
