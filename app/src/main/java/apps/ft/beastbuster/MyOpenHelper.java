package apps.ft.beastbuster;

/**
 * Created by FURUKAWA on 2017/11/03.
 */

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper
{
    private static final String TABLE = "appinfo";
    public MyOpenHelper(Context context) {
        super(context, "AppDB", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE + "("
                + "level integer,"
                + "data1 integer,"
                + "data2 integer,"
                + "data3 integer,"
                + "data4 integer,"
                + "data5 integer);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
