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
        super(context, "AppDB2", null, 2);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE + "("
                + "level integer,"      //ユーザーレベル
                + "data1 integer,"      //連続再生回数
                + "data2 integer,"      //未使用　リワード視聴日付
                + "data3 integer,"      //オプション音（爆竹音）
                + "data4 integer,"      //評価ポップアップ
                + "data5 integer,"
                + "normal integer,"     //通常音   タイプ
                + "emergency integer,"  //緊急音   タイプ
                + "interval integer,"   //間隔     連続再生時
                + "volume1 integer,"    //音量１    （通常音）
                + "volume2 integer,"    //音量２    （緊急音）
                + "light1 integer,"     //ライト１  （消灯、点灯、点滅）
                + "light2 integer,"     //ライト２  （消灯、点灯、点滅）
                + "data6 integer,"
                + "data7 integer,"
                + "data8 integer,"
                + "data9 integer,"
                + "data10 integer);");
    }
    /*    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE + "("
                + "level integer,"  //ユーザーレベル
                + "data1 integer,"  //連続再生回数
                + "data2 integer,"  //未使用　リワード視聴日付
                + "data3 integer,"  //オプション音（爆竹音）
                + "data4 integer,"  //評価ポップアップ
                + "data5 integer);");
    }*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
