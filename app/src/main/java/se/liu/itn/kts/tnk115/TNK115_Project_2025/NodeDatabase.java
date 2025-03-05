package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Node.class}, version = 1)
public abstract class NodeDatabase extends RoomDatabase {
    public abstract NodeDao userDao();
}
