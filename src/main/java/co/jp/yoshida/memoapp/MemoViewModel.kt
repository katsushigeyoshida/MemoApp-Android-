package co.jp.yoshida.memoapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

/**
 * MemoApp の実行処理
 */
class MemoViewModel(context: Context): ViewModel() {
    val TAG = "MemoViewModel"

    val myContext = context
    val memoTitle: MutableState<String> = mutableStateOf("2026/01/01 01:01:01")
    val memoText: MutableState<String> = mutableStateOf("Memo Text")
    var textFontSize: MutableState<TextUnit> = mutableStateOf(16.sp)

    var memoList = mutableMapOf<String, String>()
    var memoTitleList = mutableListOf<String>()

    val fontSizeList = listOf(8.sp, 12.sp, 16.sp, 24.sp, 32.sp, 40.sp)
    val fontSizeListMenu = listOf("8.sp", "12.sp", "16.sp", "24.sp", "32.sp", "40.sp")
    val optionMenu = listOf("文字サイズ選択", "全データ削除")

    val database: DatabaseHelper = DatabaseHelper(context)

    /**
     * 初期化
     */
    fun init(context: Context) {
        //  テキストの文字サイズ
        var pos = getIntPreferences("TEXTFONTSIZE", 2, myContext)
        pos = if (pos < 0) 2 else pos
        textFontSize = mutableStateOf(fontSizeList[pos])
        //  既存データの読込
        loadList()
        var n = 0
        if (memoList.count() == 0)
            n = newData()                   //  既存データなし
        else
            n = memoTitleList.count() - 1   //  最新データ
        setDisplay(n)
    }

    /**
     * 終了処理
     */
    fun dbClose() {
        val n = fontSizeList.indexOf<TextUnit>(textFontSize.value)
        setIntPreferences(n, "TEXTFONTSIZE", myContext)
        save()
        saveList()
        database.close()
    }

    /**
     * 新規データ
     */
    fun newDisplay() {
        save()
        var n = newData()
        setDisplay(n)
    }

    /**
     * 現在表示している次のデータ
     */
    fun nexeDisplay() {
        save()
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
        var n = memoTitleList.indexOf(title)
        Log.d(TAG,"nextDisplay "+n+" "+memoList.count()+" "+memoTitle.value+" "+memoText.value)
        setDisplay(n + 1)
    }

    /**
     * 現在表示している前のデータ
     */
    fun prevDisplay() {
        save()
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
        var n = memoTitleList.indexOf(title)
        Log.d(TAG,"prevDisplay "+n+" "+memoList.count()+" "+memoTitle.value+" "+memoText.value)
        setDisplay(max(n - 1, 0))
    }

    /**
     * 表示しているデータの削除
     */
    fun remove() {
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
        if (memoList.containsKey(title)) {
            memoList.remove(title)
            memoTitleList.remove(title)
        }
        var n = memoList.count() - 1
        if (n < 0)
            n = newData()
        setDisplay(n)
    }

    /**
     * オプションメニュー
     */
    fun optionMenu(){
        setMenuDialog(myContext, "オプションメニュー", optionMenu, iOptionOperation)
    }

    /**
     * 指定番号のデータを画面に表示
     * n: 表示するデータの番号
     */
    fun setDisplay(n: Int = -1) {
        Log.d(TAG,"setDisplay "+n)
        if (0 <= n && n < memoTitleList.count()) {
            memoTitle.value = memoTitleList[n] + " " + getCount(memoTitleList[n])
            memoText.value = memoList[memoTitleList[n]].toString()
        }
    }

    /**
     * 新規データ
     * return: 新規データの番号
     */
    fun newData(): Int {
        var title = getNowDate()
        Log.d(TAG,"newData "+title)
        if (memoList.count() == 0)
            memoTitleList.clear()
        if (!memoList.containsKey(title))
            memoList.put(title,"")
        makeTitleList()
        return memoTitleList.indexOf(title)
    }

    /**
     * データの数と位置を文字列に変換
     */
    fun getCount(title: String): String {
        val n = memoTitleList.indexOf(title) + 1
        val count = memoTitleList.count()
        return "[$n / $count]"
    }

    /**
     * データリストで表示データを更新
     */
    fun save() {
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
        Log.d(TAG,"save ["+title+"] "+memoTitle.value+" "+memoText.value)
        if (memoList.containsKey(title)) {
            memoList[title] = memoText.value
        } else {
            memoList.put(title, memoText.value)
        }
        makeTitleList()
    }

    /**
     * 全データを読みだす
     */
    fun loadList() {
        memoList = database.getAllData() as MutableMap<String, String>
        makeTitleList()
    }

    /**
     * memoListからtitleListを作成
     */
    fun makeTitleList(){
        if (0 < memoList.count()) {
            memoTitleList = memoList.keys.toList() as MutableList<String>
            memoTitleList.sort()
        }
    }

    /**
     * 全データ保存
     */
    fun saveList() {
        database.setAllData(memoList)
    }

    /**
     * 文字サイズ変更
     */
    var iOptionOperation = Consumer<String> { s ->
        if (s.compareTo(optionMenu[0]) == 0) {
            setMenuDialog(myContext, "文字サイズ", fontSizeListMenu, iFontSizeOperation)
        } else if (s.compareTo(optionMenu[1]) == 0) {
            messageDialog(myContext,"確認", "すべてのデータを削除します", iRemoveDataAll)
        }
    }

    /**
     * 表示中のメモの位置
     */
    var iFontSizeOperation = Consumer<String> { s ->
        textFontSize = mutableStateOf(fontSizeList[fontSizeListMenu.indexOf(s)])
    }

    /**
     * 全データ削除
     */
    var iRemoveDataAll = Consumer<String> { s ->
        memoList.clear()
        memoTitleList.clear()
        var n = newData()
        setDisplay(n)
    }


    //  ===  時間・日付処理  ===

    /**
     * 現在の年を取得
     */
    fun getNowDate(form:String = "yyyyMMdd_HHmmss"): String {
        val ldt = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(form)
        return ldt.format(formatter)
    }

    //  === ダイヤログ関数 ===

    /**
     * メッセージをダイヤログ表示し、OKの時に指定の関数にメッセージの内容を渡して処理するを処理する
     * 関数インターフェースの例
     *  var iDelListOperation = new Consumer<String> { s ->
     *      mDataMap.remove(s)      //  ダイヤログで指定された文字列をリストから削除
     *  }
     * 関数の呼び出し方法
     *      ylib.messageDialog(mC, "計算式の削除",mTitleBuf, iDelListOperation);
     * c            コンテキスト
     * title        ダイヤログのタイトル
     * message      メッセージ
     * operation    処理する関数インタフェース
     */
    fun messageDialog(c: Context, title: String, message: String, operation: Consumer<String>) {
        AlertDialog.Builder(c)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton( "OK") {
                    dialog, which -> operation.accept("OK")
            }
            .setNegativeButton( "Cancel") {
                    dialog, which ->
            }
            .show()
    }

    /**
     * メニュー選択ダイヤログ
     * 選択したメニューは関数インタフェースを使って取得
     *    var iPostionSelectOperation = Consumer<String> { s ->
     *        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
     *        Log.d(TAG,"inputDialog: " + s)
     *    }
     * @param title     ダイヤログのタイトル
     * @param menu      メニューデータ(配列)
     * @param operation 処理する関数(関数インターフェース)
     */
    fun setMenuDialog(c: Context, title: String, menu: List<String>, operation: Consumer<String>) {
        AlertDialog.Builder(c)
            .setTitle(title)
            .setItems(menu.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
//                    Toast.makeText(c, which.toString() + " " + menu[which] + " が選択", Toast.LENGTH_LONG).show()
                operation.accept(menu[which])
            })
            .create()
            .show()
    }

    //  === システム関連 ===

    /**
     * プリファレンスから数値(int)を取得
     * @param key
     * @param default
     * @param context
     * @return
     */
    fun getIntPreferences(key: String, default: Int, context: Context): Int {
        val prefs: SharedPreferences
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(key, default)
    }

    /**
     * プリファレンスに数値(int)を設定
     * @param value
     * @param key
     * @param context
     */
    fun setIntPreferences(value: Int, key: String, context: Context?) {
        val prefs: SharedPreferences
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.commit()
    }
}
