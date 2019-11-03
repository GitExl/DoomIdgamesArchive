package nl.exl.doomidgamesarchive.idgamesdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
    version = 2,
    entities = {File.class, Image.class}
)
public abstract class IdgamesDb extends RoomDatabase {
    private static IdgamesDb INSTANCE;

    public abstract FileDao files();
    public abstract ImageDao images();

    public static IdgamesDb getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), IdgamesDb.class, "idgames")
                .fallbackToDestructiveMigration()
                .createFromAsset("db/idgames.db")
                .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
