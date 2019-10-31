package nl.exl.doomidgamesarchive.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "images",
    foreignKeys = @ForeignKey(
        entity = File.class,
        parentColumns = "id",
        childColumns = "file_id"
    )
)
public class Image {
    @PrimaryKey
    @ColumnInfo(
        name = "id"
    )
    public int id;

    @ColumnInfo(
        name = "file_id",
        index = true
    )
    public int fileId;

    @ColumnInfo(
        name = "path"
    )
    public String path;
}
