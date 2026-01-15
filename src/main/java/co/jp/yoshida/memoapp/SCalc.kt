package co.jp.yoshida.memoapp

import java.math.BigInteger
import kotlin.math.pow

/**
 * 簡単な計算処理
 * 四則演算(+,-,*,/)+剰余(%)+べき乗(^)+階乗(!n)
 */
class SCalc {

    val funcName = listOf("")    //  関数名予約

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
     * 計算処理に不要なものは除く
     * 数値が連続した場合には + 演算子を追加
     */
    fun expressFilter(expList: List<String>): List<String> {
        val expressList = mutableListOf<String>()
        val number = listOf<Char>( '0','1','2' ,'3','4','5','6','7','8','9','.')
        val operationCode = listOf( '+', '-', '*', '/','%','^','!')
        var index = 0
        while (index < expList.count()) {
            if (expList[index].toDoubleOrNull() != null) {
                if (0 < expressList.count() && expressList.last().toDoubleOrNull() != null)
                    expressList.add("+")
                expressList.add(expList[index])
            } else if (0 <= operationCode.indexOf(expList[index][0])) {
                expressList.add(expList[index])
            } else if (expList[index][0] == '(') {
                var buf = ""
                if (0 < expressList.count()) {
                    if(expressList.last().toDoubleOrNull() != null)
                        expressList.add("+")
                    if (0 < index && 0 <= funcName.indexOf(expList[index - 1]))
                        buf = expList[index - 1]
                }
                expressList.add(buf + expList[index])
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

    // 再帰的に階乗を計算（BigInteger対応）
    fun factorial(n: Int): BigInteger {
        return if (n <= 1) {
            BigInteger.ONE
        } else {
            BigInteger.valueOf(n.toLong()) * factorial(n - 1)
        }
    }
}