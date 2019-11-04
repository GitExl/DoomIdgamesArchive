package nl.exl.doomidgamesarchive.idgamesdb;

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

    @ColumnInfo(
        name = "width"
    )
    public int width;

    @ColumnInfo(
        name = "height"
    )
    public int height;

    @ColumnInfo(
        name = "color"
    )
    public int color;
}
