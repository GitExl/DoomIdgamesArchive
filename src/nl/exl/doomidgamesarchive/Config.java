/**
 * Copyright (c) 2012, Dennis Meuwissen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

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
