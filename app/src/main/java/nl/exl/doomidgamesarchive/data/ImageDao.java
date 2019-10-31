package nl.exl.doomidgamesarchive.data;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ImageDao {
    @Query("SELECT * FROM images WHERE file_id = :fileId")
    Image findByFile(int fileId);
}
