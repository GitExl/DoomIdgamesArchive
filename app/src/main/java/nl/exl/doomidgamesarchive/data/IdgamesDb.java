package nl.exl.doomidgamesarchive.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
    version = 1,
    entities = {File.class, Image.class}
)
public abstract class IdgamesDb extends RoomDatabase {
    private static IdgamesDb INSTANCE;

    public abstract FileDao fileDao();
    public abstract ImageDao imageDao();

    public static IdgamesDb getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), IdgamesDb.class, "idgames")
                .createFromAsset("db/idgames.db")
                .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}

