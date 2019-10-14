package nl.exl.doomidgamesarchive;

import nl.exl.doomidgamesarchive.idgamesapi.Request;

/**
 * Contains configuration constants used in other areas of this app.
 */
public final class Config {

    // Maximum cache ages, in seconds.
    public static final long MAXAGE_DEFAULT = 60 * 60 * 12;
    public static final long MAXAGE_BROWSE = 60 * 60 * 12;
    public static final long MAXAGE_NEWFILES = 60 * 60 * 4;
    public static final long MAXAGE_NEWVOTES = 60 * 60 * 4;
    public static final long MAXAGE_DETAILS = 60 * 60 * 24;
    public static final long MAXAGE_SEARCH = 60 * 60 * 12;
    
    // Limit of items returned from the idgames API where appropriate.
    public static final int LIMIT_DEFAULT = 30;
    public static final int LIMIT_NEWFILES = 30;
    public static final int LIMIT_NEWVOTES = 30;
    
    // Default search category.
    public static final int CATEGORY_DEFAULT = Request.CATEGORY_FILENAME;
    
    // Default HTTP idgames mirror URL.
    public static final String IDGAMES_MIRROR_DEFAULT = "https://www.quaddicted.com/files/idgames/";
}
