package co.jp.yoshida.memoapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.view.Display
import android.view.View
import android.widget.EditText
import androidx.core.util.Consumer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.JapaneseDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.sqrt

class KLib {

    //  ===  システム  ===

    /**
     * 共有処理(テキスト)
     */
    fun actionSend(text: String, context: Context) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    //  ===  文字列  ===

    /**
     * 全角から半角に変換
     */
    fun convertIntoHalfFromFull(target : String ):String {
        var str : String

        //アルファベット（大文字）の変換
        str = target.replace("Ａ","A" ).replace("Ｂ","B" ).replace("Ｃ","C" ).replace("Ｄ","D" ).replace("Ｅ","E" ).replace("Ｆ","F" ).replace("Ｇ","G" ).replace("Ｈ","H" ).replace("Ｉ","I" ).replace("Ｊ","J" ).replace("Ｋ","K" ).replace("Ｌ","L" ).replace("Ｍ","M" ).replace("Ｎ","N" ).replace("Ｏ","O" ).replace("Ｐ","P" ).replace("Ｑ","Q" ).replace("Ｒ","R" ).replace("Ｓ","S" ).replace("Ｔ","T" ).replace("Ｕ","U" ).replace("Ｖ","V" ).replace("Ｗ","W" ).replace("Ｘ","X" ).replace("Ｙ","Y" ).replace("Ｚ","Z" )
        //アルファベット（小文字）の変換
        str = str.replace("ａ","a" ).replace("ｂ","b" ).replace("ｃ","c" ).replace("ｄ","d" ).replace("ｅ","e" ).replace("ｆ","f" ).replace("ｇ","g" ).replace("ｈ","h" ).replace("ｉ","i" ).replace("ｊ","j" ).replace("ｋ","k" ).replace("ｌ","l" ).replace("ｍ","m" ).replace("ｎ","n" ).replace("ｏ","o" ).replace("ｐ","p" ).replace("ｑ","q" ).replace("ｒ","r" ).replace("ｓ","s" ).replace("ｔ","t" ).replace("ｕ","u" ).replace("ｖ","v" ).replace("ｗ","w" ).replace("ｘ","x" ).replace("ｙ","y" ).replace("ｚ","z" )
        //数値の変換
        str = str.replace("０","0" ).replace("１","1" ).replace("２","2" ).replace("３","3" ).replace("４","4" ).replace("５","5" ).replace("６","6" ).replace("７","7" ).replace("８","8" ).replace("９","9" )
        //カタカナの変換
        str = str.replace("ア","ｱ" ).replace("イ","ｲ" ).replace("ウ","ｳ" ).replace("エ","ｴ" ).replace("オ","ｵ" ).replace("カ","ｶ" ).replace("キ","ｷ" ).replace("ク","ｸ" ).replace("ケ","ｹ" ).replace("コ","ｺ" ).replace("サ","ｻ" ).replace("シ","ｼ" ).replace("ス","ｽ" ).replace("セ","ｾ" ).replace("ソ","ｿ" ).replace("タ","ﾀ" ).replace("チ","ﾁ" ).replace("ツ","ﾂ" ).replace("テ","ﾃ" ).replace("ト","ﾄ" ).replace("ナ","ﾅ" ).replace("ニ","ﾆ" ).replace("ヌ","ﾇ" ).replace("ネ","ﾈ" ).replace("ノ","ﾉ" ).replace("ハ","ﾊ" ).replace("ヒ","ﾋ" ).replace("フ","ﾌ" ).replace("ヘ","ﾍ" ).replace("ホ","ﾎ" ).replace("マ","ﾏ" ).replace("ミ","ﾐ" ).replace("ム","ﾑ" ).replace("メ","ﾒ" ).replace("モ","ﾓ" ).replace("ヤ","ﾔ" ).replace("ユ","ﾕ" ).replace("ヨ","ﾖ" ).replace("ラ","ﾗ" ).replace("リ","ﾘ" ).replace("ル","ﾙ" ).replace("レ","ﾚ" ).replace("ロ","ﾛ" ).replace("ワ","ﾜ" ).replace("ヲ","ｦ" ).replace("ン","ﾝ" ).replace("ァ","ｧ" ).replace("ィ","ｨ" ).replace("ゥ","ｩ" ).replace("ェ","ｪ" ).replace("ォ","ｫ" ).replace("ヵ","ｶ" ).replace("ヶ","ｹ" ).replace("ッ","ｯ" ).replace("ャ","ｬ" ).replace("ュ","ｭ" ).replace("ョ","ｮ" ).replace("ヮ","ﾜ" ).replace("ヴ","ｳﾞ" ).replace("ガ","ｶﾞ" ).replace("ギ","ｷﾞ" ).replace("グ","ｸﾞ" ).replace("ゲ","ｹﾞ" ).replace("ゴ","ｺﾞ" ).replace("ザ","ｻﾞ" ).replace("ジ","ｼﾞ" ).replace("ズ","ｽﾞ" ).replace("ゼ","ｾﾞ" ).replace("ゾ","ｿﾞ" ).replace("ダ","ﾀﾞ" ).replace("ヂ","ﾁﾞ" ).replace("ヅ","ﾂﾞ" ).replace("デ","ﾃﾞ" ).replace("ド","ﾄﾞ" ).replace("バ","ﾊﾞ" ).replace("ビ","ﾋﾞ" ).replace("ブ","ﾌﾞ" ).replace("ベ","ﾍﾞ" ).replace("ボ","ﾎﾞ" ).replace("パ","ﾊﾟ" ).replace("ピ","ﾋﾟ" ).replace("プ","ﾌﾟ" ).replace("ペ","ﾍﾟ" ).replace("ポ","ﾎﾟ" )
        //記号の変換
        str = str.replace("！","!" ).replace("＃","#" ).replace("＄","$" ).replace("％","%" ).replace("＆","&" ).replace("（","(" ).replace("）",")" ).replace("ー","ｰ" ).replace("－","-" ).replace("＝","=" ).replace("＾","^" ).replace("～","~" ).replace("｜","|" ).replace("＠","@" ).replace("‘","`" ).replace("「","｢" ).replace("［","[" ).replace("｛","{" ).replace("＋","+" ).replace("：",":" ).replace("＊","*" ).replace("」","｣" ).replace("］","]" ).replace("｝","}" ).replace("、","､" ).replace("，","," ).replace("＜","<" ).replace("。","｡" ).replace("．","." ).replace("＞",">" ).replace("・","･" ).replace("／","/" ).replace("？","?" ).replace("＿","_" )
        //スペース変換
        str = str.replace("　", " ")
        return str
    }

    /**
     * 文字列を16進表記文字列に変換(全角未対応)
     */
    fun text2HexString(text: String): String {
        var chars = text.toCharArray()
        var buf = ""
        for (c in chars)
            buf += c.code.toByte().toString(16) + " "
        return buf
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

    /**
     * 和暦の取得
     */
    fun getWareki(date: LocalDate = LocalDate.now()): String {
        // JapaneseDate に変換
        val japaneseDate = JapaneseDate.from(date)

        // 和暦フォーマット（元号＋年＋月＋日）
        val formatter = DateTimeFormatter
            .ofPattern("GGGGy年M月d日", Locale.JAPAN)

        return formatter.format(japaneseDate)
    }

    /**
     * 曜日を取得
     * locale : 形式　Locale.JAPANESE/Locale.ENGLISH
     * return : "月".../Mon...
     */
    fun getDayOfWeek(locale: Locale = Locale.JAPANESE, date: LocalDate = LocalDate.now()): String {
        val dayOfWeek = date.dayOfWeek // MONDAY, TUESDAY, ...
        return dayOfWeek.getDisplayName(TextStyle.SHORT, locale) // "月", "火" など
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
     * 文字入力ダイヤログ
     * 入力した文字は関数インタフェースを使って取得
     *     var iInputOperation = Consumer<String> { s ->
     *         Toast.makeText(this, s, Toast.LENGTH_LONG).show()
     *         Log.d(TAG,"inputDialog: " + s)
     *     }
     * @param c         コンテキスト
     * @param title     ダイヤログのタイトル
     * @param message   デフォルトメッセージ
     * @param operation 処理する関数(関数インターフェース)
     */
    fun setInputDialog(c: Context, title: String, message: String, operation: Consumer<String>) {
        val editText = EditText(c)
        editText.setText(message)
        val dialog = AlertDialog.Builder(c)
        dialog.setTitle(title)
        dialog.setView(editText)
        dialog.setPositiveButton("OK", DialogInterface.OnClickListener {
                dialog, which -> operation.accept(editText.text.toString())
        })
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
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
     *  URLのWeb表示をする
     *  c       コンテキスト
     *  url     表示するURLアドレス
     */
    fun webDisp(c: Context, url: String?) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        c.startActivity(intent)
    }

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

    /**
     * pixelからdpへの変換
     * @param px
     * @param context
     * @return float dp
     */
    fun convertPx2Dp(px: Int, context: Context): Float {
        val metrics = context.getResources().getDisplayMetrics()
        return px / metrics.density
    }

    /**
     * OSのバージョン取得
     * return : (e.g., "14", "13", "12")
     */
    fun getOSVersion(): String {
        return Build.VERSION.RELEASE
    }

//    fun getScreenSize(context: View):Point {
//        val display: Display = context.windowManager.defaultDisplay
//        val point = Point()
//        display.getSize(point)
//        return point
//    }

    //  ===  数値計算  ===

    /**
     * 2点間の距離を求める
     * @param ps    始点
     * @param pe    終点
     * @return      距離
     */
    fun disPoint2(ps: PointF, pe: PointF): Float {
        val dx = (ps.x - pe.x).toDouble()
        val dy = (ps.y - pe.y).toDouble()
        return sqrt(dx * dx + dy * dy).toFloat()
    }

    /**
     * 始点に対する角度(rad)
     * @param ps    始点
     * @param pe    終点
     * @return      角度(rad)
     */
    fun angle(ps: PointF, pe: PointF): Float {
        val dx = (ps.x - pe.x).toDouble()
        val dy = (ps.y - pe.y).toDouble()
        return atan2(dy, dx).toFloat()
    }

}