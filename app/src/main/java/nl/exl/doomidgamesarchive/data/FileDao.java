package nl.exl.doomidgamesarchive.data;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface FileDao {
    @Query("SELECT * FROM files WHERE path = :path LIMIT 1")
    File findByPath(String path);
}
