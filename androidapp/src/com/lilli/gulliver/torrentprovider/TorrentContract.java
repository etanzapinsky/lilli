package com.lilli.gulliver.torrentprovider;

import android.net.Uri;

public class TorrentContract {
    public static final int OBJECT_NAME = 1;

    public static final class Objects {
        public static final String ID = "_ID";
        public static final String AUTHORITATIVE_LOCATION = "authoritative_location";
        public static final String DATA = "_data";
        public static final Uri CONTENT_URI = Uri.parse("content://com.lilli.gulliver.torrentprovider/");
    }


}
