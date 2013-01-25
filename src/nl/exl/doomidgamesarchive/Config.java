package nl.exl.doomidgamesarchive;

import nl.exl.doomidgamesarchive.idgamesapi.Request;

/**
 * Contains configuration constants used in other areas of this app.
 */
public final class Config {
    // Maximum cache ages, in milliseconds.
    public static final long MAXAGE_DEFAULT = 1000 * 60 * 60 * 60 * 24;
    public static final long MAXAGE_BROWSE = 1000 * 60 * 60 * 60 * 24;
    public static final long MAXAGE_NEWFILES = 1000 * 60 * 60 * 60 * 16;
    public static final long MAXAGE_NEWVOTES = 1000 * 60 * 60 * 60 * 1;
    public static final long MAXAGE_DETAILS = 1000 * 60 * 60 * 60 * 8;
    public static final long MAXAGE_SEARCH = 1000 * 60 * 60 * 60 * 24;
    
    // Limit of items returned from the idgames API where appropriate.
    public static final int LIMIT_DEFAULT = 20;
    public static final int LIMIT_NEWFILES = 20;
    public static final int LIMIT_NEWVOTES = 20;
    
    // Default search category.
    public static final int CATEGORY_DEFAULT = Request.CATEGORY_FILENAME;
    
    // Official HTTP mirrors.
    public static final String IDGAMES_MIRROR_GREECE = "http://ftp.ntua.gr/pub/vendors/idgames/";
    public static final String IDGAMES_MIRROR_RUSSIA = "http://ftp.chg.ru/pub/games/idgames/";
    public static final String IDGAMES_MIRROR_TEXAS = "http://ftp.mancubus.net/pub/idgames/";
    
    // Unsupported HTTP mirrors.
    public static final String IDGAMES_MIRROR_SWEDEN = "http://ftp.sunet.se/pub/pc/games/idgames/";
    public static final String IDGAMES_MIRROR_PORTUGAL = "http://ftp.netc.pt/pub/idgames/";
}
