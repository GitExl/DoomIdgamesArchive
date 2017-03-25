package nl.exl.doomidgamesarchive;

import nl.exl.doomidgamesarchive.idgamesapi.Request;

/**
 * Contains configuration constants used in other areas of this app.
 */
public final class Config {
    // Maximum cache ages, in milliseconds.
    public static final long MAXAGE_DEFAULT = 1000L * 60 * 60 * 60 * 24;
    public static final long MAXAGE_BROWSE = 1000L * 60 * 60 * 60 * 24;
    public static final long MAXAGE_NEWFILES = 1000L * 60 * 60 * 60 * 16;
    public static final long MAXAGE_NEWVOTES = 1000L * 60 * 60 * 60;
    public static final long MAXAGE_DETAILS = 1000 * 60 * 60 * 60 * 8;
    public static final long MAXAGE_SEARCH = 1000L * 60 * 60 * 60 * 24;
    
    // Limit of items returned from the idgames API where appropriate.
    public static final int LIMIT_DEFAULT = 20;
    public static final int LIMIT_NEWFILES = 20;
    public static final int LIMIT_NEWVOTES = 20;
    
    // Default search category.
    public static final int CATEGORY_DEFAULT = Request.CATEGORY_FILENAME;
    
    // Default HTTP idgames mirror URL.
    public static final String IDGAMES_MIRROR_DEFAULT = "http://ftp.ntua.gr/pub/vendors/idgames/";
}
