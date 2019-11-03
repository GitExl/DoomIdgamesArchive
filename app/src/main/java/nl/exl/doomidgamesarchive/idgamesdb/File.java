package nl.exl.doomidgamesarchive.idgamesdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "files"
)
public class File {

    @PrimaryKey
    @ColumnInfo(
        name = "id"
    )
    public int id;

    @ColumnInfo(
        name = "path",
        index = true
    )
    public String path;
}
