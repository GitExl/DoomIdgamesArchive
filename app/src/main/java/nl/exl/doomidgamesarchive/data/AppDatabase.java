package nl.exl.doomidgamesarchive.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
    version = 1,
    entities = {File.class, Image.class}
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FileDao fileDao();
    public abstract ImageDao imageDao();
}

