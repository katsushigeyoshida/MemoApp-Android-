package co.jp.yoshida.memoapp

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.yoshida.memoapp.ui.theme.MyApplication4Theme


private  val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val viewModel = MemoViewModel(this)

    val klib = KLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplication4Theme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyMemo(viewModel)
                }
            }
        }
        //  共有を受け取る
        if (intent != null) {
            val type = intent.type
            if (type != null && type.startsWith("text/"))
                viewModel.memoText.value = viewModel.memoText.value.copy(intent.getStringExtra((Intent.EXTRA_TEXT)).toString())
        }
    }

    /**
     * 画面再表示
     */
    override fun onResume() {
        super.onResume()
        viewModel.init()
        viewModel.setDisplay()
    }

    /**
     * 他の画面表示
     */
    override fun onPause() {
        viewModel.dbClose()
        super.onPause()
    }
}

@Composable
fun MyMemo(viewModel: MemoViewModel) {

    var memoTitle: String by viewModel.memoTitle
    var memoText: TextFieldValue by viewModel.memoText
    var textFontSize: TextUnit by viewModel.textFontSize
    var offsetX by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            //  スワイプ処理
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount
                    if (1 > viewModel.getSelectCount()) {
                        //  文字選択がない状態でページ切り替え
                        if (200 < offsetX) {
                            viewModel.nextDisplay()
                            offsetX = 0f
                        } else if (-200 > offsetX) {
                            viewModel.prevDisplay()
                            offsetX = 0f
                        }
                    }
                }
            }
    ) {
        //  タイトル
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
                .background(color = Color.Gray),
            textAlign = TextAlign.Center,
            text = memoTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Yellow,
        )
        //  本文入力
        TextField(
            value = memoText,
            onValueChange = { memoText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(viewModel.memoFieldHeight().dp)
            ,
            textStyle = TextStyle(fontSize = textFontSize)
        )
        //  操作ボタン
        Row(
            modifier = Modifier
                .padding(top = 0.dp)
                .offset(y = 0.dp)
        ) {
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { viewModel.newDisplay() }
            ) {
                Text(text = "新規")
            }
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { viewModel.nextDisplay() }
            ) {
                Text(text = "次")
            }
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { viewModel.prevDisplay() }
            ) {
                Text(text = "前")
            }
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { viewModel.remove() }
            ) {
                Text(text = "削除")
            }
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { viewModel.optionMenu() }
            ) {
                Text(text = "他")
            }
        }
    }
}

