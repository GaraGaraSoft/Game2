package to.msn.wings.game2;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SimpleDatabaseHelper extends SQLiteOpenHelper {
    static final private String DBNAME = "sample.sqlite";
    static final private int VERSION = 3;

    SimpleDatabaseHelper(Context context){
        super(context,DBNAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        if(db != null){
            db.execSQL("CREATE TABLE pdata (id INTEGER PRIMARY KEY AUTOINCREMENT,data TEXT)"); //テーブル作成
            db.execSQL("CREATE TABLE records(id integer primary key autoincrement,qnum integer,turn integer)"); //記録データテーブル作成
            String[] initialdata = { "0000000100011100010000000","1101110001000001000111011"}; //初期データ

            db.beginTransaction();

            try{
                SQLiteStatement sql = db.compileStatement(
                        "INSERT INTO pdata(data) values(?)"
                );
                for(int i=0;i<initialdata.length;i++){
                    sql.bindString(1,initialdata[i]);
                    sql.executeInsert();
                }

                db.setTransactionSuccessful();
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                db.endTransaction();
            }
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        if(db != null){
            db.execSQL("DROP TABLE IF EXISTS pdata");
            db.execSQL("DROP TABLE IF EXISTS records");
            onCreate(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }

}
