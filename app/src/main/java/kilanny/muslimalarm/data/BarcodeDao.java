package kilanny.muslimalarm.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface BarcodeDao {

    @Query("SELECT * FROM barcode ORDER BY id")
    Barcode[] getAll();

    @Query("SELECT * FROM barcode WHERE id = :id")
    Barcode getById(int id);

    @Insert
    long insert(Barcode barcode);

    @Delete
    void delete(Barcode barcode);

    @Update
    void update(Barcode barcode);

    @Query("SELECT COUNT(*) FROM barcode")
    int count();
}
