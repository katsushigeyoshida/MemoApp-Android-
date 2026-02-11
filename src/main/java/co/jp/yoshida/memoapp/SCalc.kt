package co.jp.yoshida.memoapp

import java.math.BigInteger
import kotlin.math.acosh
import kotlin.math.asinh
import kotlin.math.atanh
import kotlin.math.pow

/**
 * 簡単な計算処理
 * 四則演算(+,-,*,/)+剰余(%)+べき乗(^)+階乗(!n)
 */
class SCalc {

    // val funcName = listOf("")    //  関数名予約
    //  関数名予約
    val funcName = listOf(
        "RAD", "DEG", "deg2hour", "hour2deg", "rad2hour", "hour2rad", "deg2dms", "dms2deg", "hour2hms", "hms2hour",
        "fact", "fib", "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
        "exp", "ln", "log", "sqrt", "abs", "ceil", "floor", "round", "rint", "sign",
        "pow", "mod", "atan2", "log", "max", "min", "gcd", "lcm", "combi", "permu", "equals", "gt", "lt", "compare",
    )

    //  簡単な四則演算(括弧は再起処理)
    //  express : 数式　(加減乗除剰余、括弧)
    //  return  : 演算結果
    fun expression(express: String): Double {
        val expList = expressFilter(expressionList(express))   //  数式をリストに分解
        var result: Double = 0.0
        var index: Int = 0
        var ope: String = ""
        while (0 <= index && index < expList.size) {
            var num: Double? = 0.0
            if (expList[index][0] == '(') {                     //  括弧
                //  括弧内を再起処理
                num = expression(expList[index].substring(1, expList[index].length - 1))
            } else if (0 < index && expList[index][0] == '!') { //  階乗
                //  一項演算子 (階乗)
                num = expList[index - 1].toDoubleOrNull()
                num = factorial(num!!.toInt()).toDouble()
            } else if (expList[index][0].isLetter()) {
                //  単項演算子の計算
                num = monadicExpression(expList[index])
            } else {
                //  文字列を数値に変換(数値以外はnull)
                num = expList[index].toDoubleOrNull()
            }
            if (num == null ) {
                //  演算コード
                ope = expList[index]
            } else  if (index == 1 && ope == "-") {
                //  文字列の先頭が"-"の場合
                result = num * -1
            } else {
                //  二項演算子
                if (ope == "+") {                               //  加算
                    val (i, num) = express2(index, num, expList)    //  剰余を優先処理
                    index = i
                    if (index < 0) break
                    result += num
                } else if (ope == "-") {                        //  減算
                    val (i, num) = express2(index, num, expList)    //  剰余を優先処理
                    index = i
                    if (index < 0) break
                    result -= num
                } else if (ope == "*") {                        //  乗算
                    result *= num
                } else if (ope == "/") {                        //  除算
                    result /= num
                } else if (ope == "%") {
                    result %= num                               //  剰余
                } else if (ope == "^") {
                    result = result.pow(num)                //  べき乗
                } else {
                    result = num
                }
            }
            index++
        }
        return result
    }

    //  乗除、剰余、べき乗の優先処理
    //  i : 処理位置
    //  x : 前回値
    //  explist : 数式リスト
    //  (Int,Double) : (処理位置,　優先処理結果
    private fun express2(i: Int, x: Double, expList: List<String>): Pair<Int, Double> {
        var index = i
        var result: Double = x
        if (index + 2 < expList.size) {
            var y = expression(expList[index])
            while (index + 2 < expList.size) {
                val ope = expList[index + 1]
                val z = expression(expList[index + 2])
                if (ope == "*") {
                    result = y * z
                } else if (ope == "/") {
                    result = y / z
                } else if (ope == "%") {
                    if (z == 0.0)
                        return Pair(-1, result)
                    result = y % z
                } else if (ope == "^") {
                    result = y.pow(z)
                } else
                    break
                y = result
                index += 2
            }
        }
        return Pair(index, result)
    }

    /**
     * 単項演算子の計算処理を行う
     * str          単項演算子の計算式
     * return       計算結果
     */
    private fun monadicExpression(str: String): Double {
        // System.out.println("monadic:"+str+" "+str.indexOf('(')+" "+str.length());
        var result = 0.0
        if (str.indexOf('(') < 0) {
            if (str.compareTo("PI")==0) {                   //  円周率
                result = Math.PI
            } else if (str.compareTo("E")==0) {             //  自然対数の底e
                result = Math.E
            } else {
                // mError = true
                // mErrorMsg = "未サポート定数 " + str
            }
            return result
        }
        var ope = str.substring(0,str.indexOf('('))
        var data = str.substring(str.indexOf('(')+1, str.length-1)
        // System.out.println(ope+" "+data);
        var datas = stringSeperate(data)
        if (1 == datas.size) {
            //  引数が1個の単項演算子
            var x = expression(datas[0])
            if (ope.compareTo("RAD") == 0) {                //  degree→radian
                result = x * Math.PI / 180.0
            } else if (ope.compareTo("DEG") == 0) {         //  radian→degree
                result = x * 180.0 / Math.PI
            } else if (ope.compareTo("deg2hour") == 0) {    //  度 → 時
                result = deg2hour(x)
            } else if (ope.compareTo("hour2deg") == 0) {    //  時 →度
                result = hour2deg(x)
            } else if (ope.compareTo("rad2hour") == 0) {    //  ラジアン → 時
                result = rad2hour(x)
            } else if (ope.compareTo("hour2rad") == 0) {    //  時 → ラジアン
                result = hour2rad(x)
            } else if (ope.compareTo("deg2dms") == 0) {     //  度 → 度分秒
                result = deg2dms(x)
            } else if (ope.compareTo("dms2deg") == 0) {     //  度分秒 → 度
                result = dms2deg(x)
            } else if (ope.compareTo("hour2hms") == 0) {    //  時 → 時分秒
                result = hour2hms(x)
            } else if (ope.compareTo("hms2hour") == 0) {    //  時分秒 → 時
                result = hms2hour(x)
            } else if (ope.compareTo("fact") == 0) {        //  階乗
                result = factorial(x.toInt())
            } else if (ope.compareTo("fib") == 0) {         //  フィボナッチ数列
                result = fibonacci(x.toInt())
            } else if (ope.compareTo("sin") == 0) {         //  正弦
                result = Math.sin(x)
            } else if (ope.compareTo("cos") == 0) {         //  余弦
                result = Math.cos(x)
            } else if (ope.compareTo("tan") == 0) {         //  正接
                result = Math.tan(x)
            } else if (ope.compareTo("asin") == 0) {        //  逆正弦
                result = Math.asin(x)
            } else if (ope.compareTo("acos") == 0) {        //  逆余弦
                result = Math.acos(x)
            } else if (ope.compareTo("atan") == 0) {        //  逆正接
                result = Math.atan(x)
            } else if (ope.compareTo("sinh") == 0) {        //  双曲線正弦
                result = Math.sinh(x)
            } else if (ope.compareTo("cosh") == 0) {        //  双曲線余弦
                result = Math.cosh(x)
            } else if (ope.compareTo("tanh") == 0) {        //  双曲線正接
                result = Math.tanh(x)
            } else if (ope.compareTo("asinh") == 0) {       //  逆双曲線正弦
                result = asinh(x)
            } else if (ope.compareTo("acosh") == 0) {       //  逆双曲線余弦
                result = acosh(x)
            } else if (ope.compareTo("atanh") == 0) {       //  逆双曲線正接
                result = atanh(x)
            } else if (ope.compareTo("exp") == 0) {         //  eの累乗値
                result = Math.exp(x)
            } else if (ope.compareTo("ln") == 0) {          //  eを底とする自然対数
                result = Math.log(x)
            } else if (ope.compareTo("log") == 0) {         //  10を底とする対数
                result = Math.log10(x)
            } else if (ope.compareTo("sqrt") == 0) {        //  平方根
                result = Math.sqrt(x)
            } else if (ope.compareTo("abs") == 0) {         //  絶対値
                result = Math.abs(x)
            } else if (ope.compareTo("ceil") == 0) {        //  (切上げ)最小の整数値
                result = Math.ceil(x)
            } else if (ope.compareTo("floor") == 0) {       //  (切捨て)小数点以下の数の内最大の整数値
                result = Math.floor(x)
            } else if (ope.compareTo("round") == 0) {       //  (四捨五入)最も近い整数値に丸める
                result = Math.round(x).toDouble()
            } else if (ope.compareTo("rint") == 0) {        //  浮動小数点の整数部を返す
                result = Math.rint(x)
            } else if (ope.compareTo("sign") == 0) {        //  符号を示す値を返す
                result = Math.signum(x)
            } else {
                // mError = true
                // mErrorMsg = "未サポート関数 " + ope
            }
        } else if (2 == datas.size) {
            //  引数が2個の単項演算子
            var x = expression(datas[0])
            var y = expression(datas[1])
            if (ope.compareTo("pow") == 0) {                //  累乗
                result = Math.pow(x, y)
            } else if (ope.compareTo("mod") == 0) {         //  剰余
                result = x % y
            } else if (ope.compareTo("atan2") == 0) {       //  逆正接atan2(y,x) (x,y座標)
                result = Math.atan2(x, y)
            } else if (ope.compareTo("log") == 0) {         //  指定した底の対数(log(x,y)
                result = Math.log(y) / Math.log(x)
            } else if (ope.compareTo("max") == 0) {         //  大きい方の値を返す
                result = Math.max(x, y)
            } else if (ope.compareTo("min") == 0) {         //  小さい方の値を返す
                result = Math.min(x, y)
            } else if (ope.compareTo("gcd") == 0) {         //  最大公約数
                result = Gcd(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("lcm") == 0) {         //  最小公倍数
                result = Lcm(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("combi") == 0) {       //  組合せの数
                result = combination(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("permu") == 0) {       //  順列の数
                result = permutation(x.toInt(), y.toInt()).toDouble()
            } else if (ope.compareTo("equals") == 0) {      //  比較  x==y ⇒ 1 x!=y ⇒ 0
                result = if (x == y) 1.0 else 0.0
            } else if (ope.compareTo("gt") == 0) {          //  比較  x < y ⇒ 1, x >= y ⇒ 0
                result = if (x < y) 1.0 else 0.0
            } else if (ope.compareTo("lt") == 0) {          //  比較  x > y ⇒ 1, x <= y ⇒ 0
                result = if (x > y) 1.0 else 0.0
            } else if (ope.compareTo("compare") == 0) {     //  比較  x > y ⇒ 1 x == y ⇒ 0 x < y ⇒ -1
                result = if (x > y) 1.0 else if (x < y) -1.0 else 0.0
            } else {
                // mError = true
                // mErrorMsg = "未サポート関数 " + ope
            }
        }
        return result
    }

    /**
     * 計算処理に不要なものは除く
     * 数値が連続した場合には + 演算子を追加
     */
    fun expressFilter(expList: List<String>): List<String> {
        val expressList = mutableListOf<String>()
        val number = listOf<Char>( '0','1','2' ,'3','4','5','6','7','8','9','.')
        val operationCode = listOf( '+', '-', '*', '/','%','^','!')
        val constVal = listOf("PI", "E")
        var index = 0
        while (index < expList.count()) {
            //  数値
            if (expList[index].toDoubleOrNull() != null) {
                if (0 < expressList.count() && expressList.last().toDoubleOrNull() != null)
                    expressList.add("+")
                expressList.add(expList[index])
            } else if (0 <= operationCode.indexOf(expList[index][0])) {
                //  演算子
                expressList.add(expList[index])
            } else if (expList[index][0] == '(') {
                //  括弧データ+関数名
                var buf = ""
                if (0 < expressList.count()) {
                    if(expressList.last().toDoubleOrNull() != null)
                        expressList.add("+")
                }
                if (0 < index && 0 <= funcName.indexOf(expList[index - 1]))
                    buf = expList[index - 1]
                expressList.add(buf + expList[index])
            } else if (0 <= constVal.indexOf(expList[index])) {
                //  定数
                expressList.add(expList[index])
            }
            index++
        }
        return expressList
    }


    //  数式を数値と演算コードと括弧内に分解してリスト化
    //  "1+24*3+(2+1)+pow(1,2)" => 1,+,24,*,3,+,(2+1),+,pow,(1,2)"
    //  express : 数式
    //  return  : 分解リスト
    fun expressionList(express: String): List<String> {
        val expList = mutableListOf<String>()
        val number = listOf<Char>( '0','1','2' ,'3','4','5','6','7','8','9','.')
        val operationCode = listOf( '+', '-', '*', '/','%','^','!')
//        val funcName = listOf("RAD", "DEG", "max", "min", "sin", "cos", "tan")    //  関数名予約
        var buf = ""
        var index = 0
        while(index < express.length) {
            var c = express[index++]
            if (0 <= number.indexOf(c)) {
                //  数値
                buf += c
            } else if (c == '(') {
                //	括弧内記述または関数
                if (0 < buf.length) {
                    expList.add(buf)
                    buf = ""
                }
                //  括弧内数式
                var pos = pairBracketPos(express, index - 1)
                buf += express.substring(index - 1, pos + 1)
                index = pos + 1
                if (0 < buf.length) expList.add(buf)
                buf = ""
            } else if (0 <= operationCode.indexOf(c)) {
                //  演算コード(1文字)
                if (0 < buf.length) {
                    expList.add(buf)
                }
                buf = ""
                expList.add(c.toString())
            } else if (c == ' ' || c == '\n' || c == '\t') {
                //	空白
                if (0 < buf.length)
                    expList.add(buf)
                buf = ""
            } else if (number.indexOf(c) < 0) {
                if (0 < buf.length && buf.toDoubleOrNull() != null) {
                    expList.add(buf)
                    buf = c.toString()
                } else
                    buf += c
            } else {
                //	その他
                buf += c
            }
        }
        if (0 < buf.length)
            expList.add(buf)
        return expList
    }

    /**
     * 文字列をカンマ(,)で分割する、括弧で囲まれている場合は分割しない(計算式用)
     * str          文字列
     * return       カンマで分解した文字列の配列
     */
    private fun stringSeperate(str: String): List<String> {
        var strList = mutableListOf<String>();
        var i = 0
        var bracketCount = 0
        var buf = ""
        while (i < str.length) {
            if (str[i] == '(') {
                bracketCount++
            } else if (str[i] == ')') {
                bracketCount--
            }
            if (bracketCount == 0 && str[i]== ',') {
                strList.add(buf)
                buf = ""
            } else {
                buf += str[i];
            }
            i++
        }
        if (0 < bracketCount) {
//            mError = true
//            mErrorMsg = "括弧があっていない"
        }
        if (0 < buf.length)
            strList.add(buf);
        return strList
    }

    //  最初に検出した括弧に対応する括弧の位置を求める
    //  express  : 文字列
    //  startPos : 検索開始位置
    private fun pairBracketPos(express: String, startPos: Int): Int {
        var index = express.indexOf('(',startPos)
        if (index < 0) return index
        var count = 1
        index++
        while (0 < count && index < express.length) {
            var c = express[index++]
            if (c == '(')
                count++
            else if (c == ')')
                count--
        }
        return index - 1
    }


    //  ======== 度分秒/時分秒　=======
    /**
     * 度(時)(ddd.dddd)を度分秒(時分秒)表記(ddd.mmss)にする
     * deg          ddd.dddddd
     * return       ddd.mmss
     */
    fun deg2dms(deg: Double):Double {
        var tmp = deg;
        var degree = Math.floor(tmp);
        tmp = (tmp - degree) * 60.0;
        var minutes = Math.floor(tmp);
        tmp = (tmp - minutes) * 60.0;
        return degree + minutes / 100.0 + tmp /10000.0;
    }

    /**
     * 度分秒(時分秒)表記(ddd.mmss)を度(時)(ddd.dddd)にする
     * dms          ddd.mmss
     * return       ddd.ddddd
     */
    fun dms2deg(dms: Double ):Double {
        var deg = Math.floor(dms);
        var tmp = (dms - deg) * 100.0;
        var min = Math.floor(tmp);
        var sec = (tmp - min) * 100.0;
        return deg + min / 60.0 + sec / 3600.0;
    }

    /**
     * 時(hh.hhhh)を時分秒(hh.mmss)に変換する
     * hour     時(hh.hhhh)
     * return   時分秒(hh.mmss)
     */
    fun hour2hms(hour: Double): Double {
        var tmp = hour
        val degree: Double = Math.floor(tmp)
        tmp = (tmp - degree) * 60.0
        val minutes: Double = Math.floor(tmp)
        tmp = (tmp - minutes) * 60.0
        return degree + minutes / 100.0 + tmp / 10000.0
    }

    /**
     * 時分秒(hh.mm.ss)を時(hh.hhhh)に変換する
     * hms      時分秒(hh.mmss)
     * return   時(hh.hhhh)
     */
    fun hms2hour(hms: Double): Double {
        val deg: Double = Math.floor(hms)
        val tmp = (hms - deg) * 100.0
        val min: Double = Math.floor(tmp)
        val sec = (tmp - min) * 100.0
        return deg + min / 60.0 + sec / 3600.0
    }

    /**
     * 度(ddd.dddd)から時(hh.hhhh)に変換
     * deg      度(dddd.dddd)
     * return   時(hh.hhhh)
     */
    fun deg2hour(deg: Double): Double {
        return deg * 24.0 / 360.0
    }

    /**
     * 時(hh.hhhh)から度(ddd.dddd)に変換
     * hour     時(hh.hhhh)
     * return   度(ddd.dddd)
     */
    fun hour2deg(hour: Double): Double {
        return hour * 360.0 / 24.0
    }

    /**
     * ラジアンからd時(hh.hhhhh)に変換
     * rad      ラジアン
     * return   時(hh.hhhh)
     */
    fun rad2hour(rad: Double): Double {
        return rad * 12.0 / Math.PI
    }

    /**
     * 時(hh.hhhh)からラジアンに変換
     * hour     時(hh.hhhh)
     * return   ラジアン
     */
    fun hour2rad(hour: Double): Double {
        return hour * Math.PI / 12.0
    }


    //  ======== 三角関数・指数関数　=======
    /**
     * 	逆双曲関数 sinh^-1 = log(x±√(x^2+1))
     *  x
     * return
     */
    fun asinh(x: Double): Double {
        return Math.log(x+Math.sqrt(x*x+1))
    }

    /**
     * 	逆双曲関数 cosh^-1 = log(x±√(x^2-1))
     * x
     * return
     */
    fun acosh(x: Double): Double {
        return Math.log(x+Math.sqrt(x*x-1))
    }

    /**
     * 	逆双曲関数 tanh^-1 = 1/2log((1+x)/(1-x))
     * x
     * return
     */
    fun atanh(x: Double): Double {
        return Math.log((1+x)/(1-x))/2.0;
    }


    //  ======== 組合せ　=======

    /**	階乗計算 (n!)
     * n
     * return
     */
    fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 1..n)
            result *= i.toDouble();
        return result
    }

    /**	順列(nPr)
     * 	異なる n個のものから r個を選んで並べる順列の総数 nPr を求めます。
     * n
     * r
     * return
     */
    fun permutation(n: Int, r: Int): Int {
        var result = 1;
        for (i in (n-r+1)..n)
            result *= i
        return result
    }

    /**	組合せ(nCr)=n!/(r!*(n-r)!)
     * 	異なる n個のものから r個を選ぶ組み合わせの総数 nCr を求めます。
     * 	nCr = n-1Cr-1 + n-1Cr
     * n
     * r
     * return
     */
    fun combination(n: Int, r: Int): Int {
        if (r==0 || r==n)
            return 1
        return combination(n - 1, r - 1) + combination(n-1, r)
    }

    /**
     * フィボナッチ数列を求める
     *  f(1) = f(2) =1, f(n+2) = f(n) + f(n+1)
     * n
     * return
     */
    fun fibonacci(n: Int): Double {
        if (n <= 2)
            return 1.0
        return fibonacci(n-2) + fibonacci(n-1)
    }

    // 再帰的に階乗を計算（BigInteger対応）
//    fun factorial(n: Int): BigInteger {
//        return if (n <= 1) {
//            BigInteger.ONE
//        } else {
//            BigInteger.valueOf(n.toLong()) * factorial(n - 1)
//        }
//    }

    /**
     * 最小公倍数
     * a
     * b
     * return
     */
    fun Lcm(a: Int, b:Int): Int {
        return a * b / Gcd(a, b)
    }

    /**
     * 最大公約数(ユークリッドの互除法)
     * a
     * b
     * return
     */
    fun Gcd(aa: Int, bb: Int): Int {
        var a = aa
        var b = bb
        if (a < b)
            return Gcd(b, a)
        while (b != 0) {
            var remainder = a % b
            a = b
            b = remainder
        }
        return a
    }
}