package co.jp.yoshida.memoapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlin.collections.iterator

/**
 * データベースヘルパー(SQLiteopenHelperを継承)
 * context : Context
 */
class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val TAG = "DatabaseHelper"
    companion object {
        private const val DATABASE_NAME = "memo_data"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "memo_table"
    }

    /**
     * データベースの初期化
     * 指定したデータベースが存在しない時に1回だけ実行される
     * データベースを再設定するときはアプリを１度アンインストールする
     */
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate")
        try {
            //  テーブル作成用SQL分
            val query = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    "userId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "memoTitle TEXT, " +
                    "memoText TEXT)")
            //  SQL 実行
            db.execSQL(query)
        } catch(e: Exception) {
            Log.d(TAG,"onCreate: "+e.message)
        }
    }

    /**
     * データ更新
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            //  テーブルを削除する
            db.execSQL("DROP TABLE IF EXISTS $DATABASE_NAME")
            //  テーブルの新規作成
            onCreate(db)
        } catch(e: Exception) {
            Log.d(TAG,"onUpgrade: "+e.message)
        }
    }


    /**
     * すべてのデータをSQLデータへースに登録する
     * dataList: Map<title, text>
     */
    fun setAllData(dataList: Map<String, String>) {
        Log.d(TAG,"setAllData")
        try {
            val db = this.writableDatabase
            db.delete(TABLE_NAME, null, null)
            for (data in dataList) {
                val values = ContentValues()
                values.put("memoTitle", data.key)
                values.put("memoText", data.value)
                //  データを新規登録
                db.insert(TABLE_NAME, null, values)
            }
            db.close()
        } catch(e: Exception) {
            Log.d(TAG,"setAllData: "+e.message)
        }
    }

    /**
     * SQLデータベースからすべてのデータを読みだす
     * return: Map<title, text>
     */
    fun getAllData(): Map<String, String> {
        Log.d(TAG,"getAllData")
        val memoList = mutableMapOf<String, String>()
        try {
            val db = this.readableDatabase
            val cursor: Cursor = db.query(TABLE_NAME, null,null,null,null,null, null)
            while (cursor.moveToNext()) {
                var titleIndex = cursor.getColumnIndex("memoTitle")
                var title = cursor.getString(titleIndex)
                var textIndex = cursor.getColumnIndex("memoText")
                var text = cursor.getString(textIndex)
                Log.d(TAG,"getAllData "+title+" "+text)
                memoList.put(title, text)
            }
            db.close()
        } catch(e: Exception) {
            Log.d(TAG,"getAllData: "+e.message)
        }
        return memoList
    }
}