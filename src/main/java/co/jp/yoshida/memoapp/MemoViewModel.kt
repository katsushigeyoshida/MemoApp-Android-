package co.jp.yoshida.memoapp

import android.graphics.Point
import android.util.Log
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * MemoApp の実行処理
 */
class MemoViewModel(activity: ComponentActivity): ViewModel() {
    val TAG = "MemoViewModel"

    val myActivity = activity
    val memoTitle: MutableState<String> = mutableStateOf("")            //  メモタイトル(作成日時)
    val memoText: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue())   //  メモ本文
    var textFontSize: MutableState<TextUnit> = mutableStateOf(16.sp)    //  メモ本文の文字サイズ

    var memoList = mutableMapOf<String, String>()           //  メモのタイトルと本文のマップ
    var memoTitleList = mutableListOf<String>()             //  メモのタイトルリスト
    var mContentList = mutableListOf<List<String>>()        //  目次リスト(メニュー用の一時的なリスト)

    val fontSizeList = listOf(8.sp, 10.sp, 12.sp, 14.sp, 16.sp, 20.sp, 24.sp, 32.sp, 40.sp)
    val fontSizeListMenu = listOf("8.sp", "10.sp", "12.sp", "14.sp", "16.sp", "20.sp", "24.sp", "32.sp", "40.sp")
    val optionMenu = listOf("計算", "挿入", "目次", "共有", "検索フィルタ", "最新日付に変更","文字サイズ選択", "全データ削除")
    val insertMenu = listOf("日付(YYYY年MM月DD日)", "日付(YYYY/MM/DD)", "日付(令和YY年MM月DD日)",
                            "曜日(SUN)", "曜日(日)","時間(HH時MM分SS秒)", "時間(HH:MM:SS")


    val database: DatabaseHelper = DatabaseHelper(activity)
    val klib = KLib()

    /**
     * 初期化
     */
    fun init() {
        //  テキストの文字サイズ
        var pos = klib.getIntPreferences("TEXTFONTSIZE", 2, myActivity)
        pos = if (pos < 0) 2 else pos
        textFontSize = mutableStateOf(fontSizeList[pos])
        //  既存データの読込
        loadList()                          //  DBからの読込
        var n = 0
        //  共有データあり
        if (0 < memoText.value.text.length) {
            n = newData(memoText.value.text)
        } else if (memoList.count() == 0) {
            n = newData()                   //  既存データなし
        } else if (memoText.value.text.length == 0) {
            n = klib.getIntPreferences("CURRENTPAGENO", memoTitleList.count() - 1, myActivity)
        }
        setDisplay(n)
        Log.d(TAG, "init "+memoList.count())
    }

    /**
     * 終了処理
     */
    fun dbClose() {
        //  文字サイズ
        val n = fontSizeList.indexOf<TextUnit>(textFontSize.value)
        klib.setIntPreferences(n, "TEXTFONTSIZE", myActivity)
        //  ページ番号保存
        klib.setIntPreferences(curPageNo(), "CURRENTPAGENO", myActivity)
        //  内容をDBに保存
        save()              //  現ページを登録
        saveList()          //  全ページをDBに保存
        database.close()
        memoTitle.value = ""
        memoText.value = memoText.value.copy("")
    }

    /**
     * 新規データ
     */
    fun newDisplay() {
        save()
        var n = newData()
        setDisplay(n)
        makeTitleList()
    }

    /**
     * 現在表示している次のデータ
     */
    fun nextDisplay() {
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
        klib.setMenuDialog(myActivity, "オプションメニュー", optionMenu, iOptionOperation)
    }

    /**
     * 指定番号のデータを画面に表示
     * n: 表示するデータの番号
     */
    fun setDisplay(n: Int = -1) {
        memoText.value = memoText.value.copy("")
        if (0 <= n && n < memoTitleList.count()) {
            memoTitle.value = memoTitleList[n] + " " + getCount(memoTitleList[n])
            memoText.value = memoText.value.copy(memoList[memoTitleList[n]].toString())
        } else {
            var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
            var n = memoTitleList.indexOf(title)
            memoTitle.value = memoTitleList[n] + " " + getCount(memoTitleList[n])
            memoText.value = memoText.value.copy(memoList[memoTitleList[n]].toString())
        }
    }

    /**
     * 新規データ
     * return: 新規データの番号
     */
    fun newData(text: String = ""): Int {
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
            memoList[title] = memoText.value.text
        } else {
            memoList.put(title, memoText.value.text)
        }
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
    fun makeTitleList(searchWord: String = "") {
        memoTitleList.clear()
        if (0 < memoList.count()) {
//            memoTitleList = memoList.keys.toList() as MutableList<String>
            for (memo in memoList) {
                if (0 <= memo.value.indexOf(searchWord))
                    memoTitleList.add(memo.key)
            }
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
            //  挿入
            insertMenu()
        } else if (s.compareTo(optionMenu[2]) == 0) {
            //  目次
            contentListSelect()
        } else if (s.compareTo(optionMenu[3]) == 0) {
            //  共有
            klib.actionSend(memoText.value.text, myActivity)
        } else if (s.compareTo(optionMenu[4]) == 0) {
            //  検索フィルタ
            findFilter()
        } else if (s.compareTo(optionMenu[5]) == 0) {
            //  最新日付に変更
            val n = updateDate()
            setDisplay(n)
        } else if (s.compareTo(optionMenu[6]) == 0) {
            //  文字サイズ選択
            klib.setMenuDialog(myActivity, "文字サイズ", fontSizeListMenu, iFontSizeOperation)
        } else if (s.compareTo(optionMenu[7]) == 0) {
            //  全データ削除
            klib.messageDialog(myActivity,"確認", "すべてのデータを削除します", iRemoveDataAll)
        }
    }

    /**
     * 文字列の中に = がある時、そこまでの数値と演算子を抜き出して計算し = の後ろに計算結果を挿入
     */
    fun calc() {
        val scalc = SCalc()
        var text = memoText.value.text
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
        memoText.value = memoText.value.copy(text)
    }

    /**
     * 挿入単語の選択メニュー表示
     */
    fun insertMenu() {
        klib.setMenuDialog(myActivity, "文字列挿入メニュー", insertMenu, iInsertOperation)
    }

    /**
     * 単語の挿入処理
     */
    var iInsertOperation = Consumer<String> { s ->
        var insertWord = ""
        if (s.compareTo(insertMenu[0]) == 0) {
            //  日付
            insertWord = klib.getNowDate("yyyy年MM月dd日")
        } else if (s.compareTo(insertMenu[1]) == 0) {
            //  日付
            insertWord = klib.getNowDate("yyyy/MM/dd")
        } else if (s.compareTo(insertMenu[2]) == 0) {
            //  日付(和暦)
            insertWord = klib.getWareki()
        } else if (s.compareTo(insertMenu[3]) == 0) {
            //  曜日(Sun)
            insertWord = "(" + klib.getDayOfWeek(Locale.ENGLISH) + ")"
        } else if (s.compareTo(insertMenu[4]) == 0) {
            //  曜日(日)
            insertWord = "(" + klib.getDayOfWeek(Locale.JAPANESE) + ")"
        } else if (s.compareTo(insertMenu[5]) == 0) {
            //  時間(HH時MM分SS秒)
            insertWord = klib.getNowDate("HH時mm分ss秒")
        } else if (s.compareTo(insertMenu[6]) == 0) {
            //  時間(HH:MM:SS)
            insertWord = klib.getNowDate("HH:mm:ss")
        }

        var text = memoText.value.text
        var n = memoText.value.selection.start
        text  = text.substring(0, n) + insertWord + text.substring(n, text.length)
        memoText.value = memoText.value.copy(text)
    }

    /**
     * 各ページの先頭文字列を目次として表示し選択ページに移動する
     */
    fun contentListSelect() {
        val menuList = mutableListOf<String>()
        var n = 0
        for (memo in memoList) {
            val bufList = mutableListOf<String>()
            bufList.add(memo.key)
            bufList.add(getMemoTitle(memo.value))
            mContentList.add(bufList)
            menuList.add((memoList.count() - n++).toString() + "." + getMemoTitle(memo.value))
        }
        menuList.reverse()
        klib.setMenuDialog(myActivity, "目次(ページタイトル)", menuList, iContentListSelect)
    }

    /**
     * 目次の選択でページ表示
     */
    var iContentListSelect = Consumer<String> { s ->
        val n = s.substring(0, s.indexOf('.')).toInt()
        if (0 < n) {
            setDisplay(memoList.count() - n)
        }
    }


    /**
     * メモページから空行を除く最初の1行目をタイトルとして抽出
     */
    fun getMemoTitle(text: String, maxLength: Int = 16): String {
        var title = ""
        var start = 0
        var pos = text.indexOf('\n', start)
        if (pos < 0) {
            title = text.trim()
        } else {
            title = text.substring(0, pos).trim()
            while (title.length == 0 && 0 <= pos && pos < text.length) {
                start = pos + 1
                pos = text.indexOf('\n', start)
                if (0 < pos)
                    title = text.substring(0, pos).trim()
            }
        }
        return  title.substring(0, min(title.length, maxLength))
    }

    /**
     * 選択文字数の取得
     */
    fun getSelectCount(): Int {
        return memoText.value.getSelectedText().length
    }

    /**
     * メモの日付を最新に更新する
     */
    fun updateDate():Int {
        var title = memoTitle.value.substring(0, memoTitle.value.indexOf(' '))
        var n = memoTitleList.indexOf(title)
        if (memoList.containsKey(title))
            memoList.remove(title)
        title = klib.getNowDate()
        if (memoList.count() == 0 || !memoList.containsKey(title))
            memoList.put(title, memoText.value.text)
        makeTitleList()
        return memoTitleList.indexOf(title)
    }

    /**
     * 検索ワード入力ダイヤログ
     */
    fun findFilter() {
        klib.setInputDialog(myActivity, "検索文字列", "",iFindFilter)
    }

    /**
     * 検索ワードフィルタリング
     */
    var iFindFilter = Consumer<String> {s ->
        makeTitleList(s)
        setDisplay(memoTitleList.lastIndex)
    }

    /**
     * 文字サイズの選択変更
     */
    var iFontSizeOperation = Consumer<String> { s ->
        textFontSize = mutableStateOf(fontSizeList[fontSizeListMenu.indexOf(s)])
        setDisplay()
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

    /**
     * メモ入力フィールドの高さ
     */
    fun memoFieldHeight(): Float {
        //  スクリーンサイズの取得
        val point = getScreenSize(myActivity)
        //  入力フィールドの高さを求める
        var h = klib.convertPx2Dp(point.y, myActivity)
        val osver = klib.getOSVersion().toInt()
        if (point.x< point.y)
            h -= if (14 < osver) 150f else if (10 < osver) 80f else 100f
        else
            h -= 100f
        Log.d(TAG, "memoFieldHeight "+osver+" "+h)
        return h
    }

    /**
     * スクリーンサイズの取得
     */
    fun getScreenSize(activity: ComponentActivity):Point {
        val display: Display = activity.windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point
    }

}
