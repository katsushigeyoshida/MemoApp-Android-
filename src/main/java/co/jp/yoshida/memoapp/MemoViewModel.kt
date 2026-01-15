package co.jp.yoshida.memoapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
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
    val memoTitle: MutableState<String> = mutableStateOf("")
    val memoText: MutableState<String> = mutableStateOf("")
    var textFontSize: MutableState<TextUnit> = mutableStateOf(16.sp)

    var memoList = mutableMapOf<String, String>()
    var memoTitleList = mutableListOf<String>()

    val fontSizeList = listOf(8.sp, 12.sp, 16.sp, 24.sp, 32.sp, 40.sp)
    val fontSizeListMenu = listOf("8.sp", "12.sp", "16.sp", "24.sp", "32.sp", "40.sp")
    val optionMenu = listOf("計算", "共有", "文字サイズ選択", "全データ削除")

    val database: DatabaseHelper = DatabaseHelper(context)
    val klib = KLib()

    /**
     * 初期化
     */
    fun init(context: Context) {
        //  テキストの文字サイズ
        var pos = klib.getIntPreferences("TEXTFONTSIZE", 2, myContext)
        pos = if (pos < 0) 2 else pos
        textFontSize = mutableStateOf(fontSizeList[pos])
        //  既存データの読込
        loadList()                          //  DBからの読込
        var n = 0
        //  共有データあり
        if (0 < memoText.value.length)
            n = newData(memoText.value)
        if (memoList.count() == 0)
            n = newData()                   //  既存データなし
        else if (memoText.value.length == 0){
            n = klib.getIntPreferences("CURRENTPAGENO", memoTitleList.count() - 1, myContext)
        }
        setDisplay(n)
    }

    /**
     * 終了処理
     */
    fun dbClose() {
        //  文字サイズ
        val n = fontSizeList.indexOf<TextUnit>(textFontSize.value)
        klib.setIntPreferences(n, "TEXTFONTSIZE", myContext)
        //  ページ番号保存
        klib.setIntPreferences(curPageNo(), "CURRENTPAGENO", myContext)
        //  内容をDBに保存
        save()              //  現ページを登録
        saveList()          //  全ページをDBに保存
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
        setDisplay(n + 1)
    }

    /**
     * 現在表示している前のデータ
     */
    fun prevDisplay() {
        save()
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
        var n = memoTitleList.indexOf(title)
        setDisplay(max(n - 1, 0))
    }

    /**
     * 表示しているデータの削除
     */
    fun remove() {
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
        if (memoList.containsKey(title))
            memoList.remove(title)
        var n = memoList.count() - 1
        if (n < 0)
            n = newData()
        else
            makeTitleList()
        setDisplay(n)
    }

    /**
     * オプションメニュー
     */
    fun optionMenu(){
        klib.setMenuDialog(myContext, "オプションメニュー", optionMenu, iOptionOperation)
    }

    /**
     * 指定番号のデータを画面に表示
     * n: 表示するデータの番号
     */
    fun setDisplay(n: Int = -1) {
        if (0 <= n && n < memoTitleList.count()) {
            memoTitle.value = memoTitleList[n] + " " + getCount(memoTitleList[n])
            memoText.value = memoList[memoTitleList[n]].toString()
        }
    }

    /**
     * 新規データ
     * return: 新規データの番号
     */
    fun newData(text: String = ""): Int {
        Toast.makeText(myContext, text, Toast.LENGTH_LONG)
        var title = klib.getNowDate()
        if (memoList.count() == 0 || !memoList.containsKey(title))
            memoList.put(title, text)
        makeTitleList()
        return memoTitleList.indexOf(title)
    }

    /**
     * データリストで表示データを更新
     */
    fun save() {
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
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
     * 全データ保存
     */
    fun saveList() {
        database.setAllData(memoList)
    }

    /**
     * 表示しているページ蛮行
     */
    fun curPageNo(): Int {
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
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
     * memoListからtitleListを作成
     */
    fun makeTitleList(){
        memoTitleList.clear()
        if (0 < memoList.count()) {
//            memoTitleList = memoList.keys.toList() as MutableList<String>
            for (title in memoList.keys)
                memoTitleList.add(title)
            memoTitleList.sort()
        }
    }

    /**
     * 文字サイズ変更
     */
    var iOptionOperation = Consumer<String> { s ->
        if (s.compareTo(optionMenu[0]) == 0) {
            //  計算処理
            calc()
        } else if (s.compareTo(optionMenu[1]) == 0) {
            //  共有
            klib.actionSend(memoText.value, myContext)
        } else if (s.compareTo(optionMenu[2]) == 0) {
            //  文字サイズ選択
            klib.setMenuDialog(myContext, "文字サイズ", fontSizeListMenu, iFontSizeOperation)
        } else if (s.compareTo(optionMenu[3]) == 0) {
            //  全データ削除
            klib.messageDialog(myContext,"確認", "すべてのデータを削除します", iRemoveDataAll)
        }
    }

    /**
     * 文字列の中に = がある時、そこまでの数値と演算子を抜き出して計算し = の後ろに計算結果を挿入
     */
    fun calc() {
        val scalc = SCalc()
        var text = memoText.value
        var start = if (0 <= text.indexOf("==", 0))
                        text.indexOf("==", 0) + 2 else 0
        var pos = text.indexOf('=', start)
        while (0 <= pos) {
            //  数式の抽出
            var express = klib.convertIntoHalfFromFull(text.substring(start, pos))
            //  数式を計算
            val result = scalc.expression(express).toString()
            //  =の後ろに計算むっかを挿入
            text = text.substring(0, pos + 1) + result +
                    if (pos  < text.length)  (" " + text.substring(pos + 1,text.length)) else ""
            //  次の数式の位置
            start = pos + 1 + result.length + 1
            start = if (0 <= text.indexOf("==", start))
                        text.indexOf("==", start) + 2 else start
            pos = text.indexOf('=', start)
        }
        memoText.value = text
    }

    /**
     * 文字サイズの選択変更
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

}
