package com.lilli.gulliver.lilliprovider;

import android.net.Uri;

public class LilliContract {
    public static final int OBJECTS_ID = 1;
    public static final int OBJECTS = 2;
    public static final int EDGES_ID = 3;

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ALGORITHM = "algorithm";

    public static final class Objects {
        public static final String ID = "_ID";
        public static final String AUTHORITATIVE_LOCATION = "authoritative_location";
        public static final String DATA = "_data";
        public static final Uri CONTENT_URI = Uri.parse("content://com.lilli.gulliver.lilliprovider/objects");
    }

    public static final class Edges {
        public static final String ID = "_ID";
        public static final String LOCATION = "location";
        public static final Uri CONTENT_URI = Uri.parse("content://com.lilli.gulliver.lilliprovider/edges");
    }
}
