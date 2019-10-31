package nl.exl.doomidgamesarchive.data;

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

    @ColumnInfo(
        name = "title"
    )
    public String title;
}
