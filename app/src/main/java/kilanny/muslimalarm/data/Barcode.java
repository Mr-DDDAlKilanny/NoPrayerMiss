package kilanny.muslimalarm.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "barcode", indices = {@Index("id")})
public class Barcode {

    public Barcode() {
    }

    @Ignore
    public Barcode(String code, String format) {
        this.code = code;
        this.format = format;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "format")
    private String format;

    @Ignore
    public boolean selected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getFormat() {
        return format;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
