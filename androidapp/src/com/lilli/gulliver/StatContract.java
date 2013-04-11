package com.lilli.gulliver;

import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/11/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatContract {
    public static abstract class StatEntry implements BaseColumns {
        public static final String TABLE_NAME = "stats";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_METHOD = "method";

        private StatEntry() {}
    }
}


