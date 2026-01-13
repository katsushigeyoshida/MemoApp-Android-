package co.jp.yoshida.memoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Clip
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.yoshida.memoapp.ui.theme.MyApplication4Theme

class MainActivity : ComponentActivity() {
    private  val TAG = "MainActivity"
    private val viewModel = MemoViewModel(this)

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
                viewModel.memoText.value = intent.getStringExtra((Intent.EXTRA_TEXT)).toString()
        }
    }

    /**
     * 画面再表示
     */
    override fun onResume() {
        super.onResume()
        viewModel.init(this)
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
    var memoText: String by viewModel.memoText
    var textFontSize: TextUnit by viewModel.textFontSize

    Column() {
        Row(
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { viewModel.newDisplay() }
            ) {
                Text(text = "新規")
            }
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { viewModel.nexeDisplay() }
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
        Text(
            modifier = Modifier.fillMaxWidth()
                .padding(all = 8.dp)
                .background(color = Color.Gray),
            textAlign = TextAlign.Start,
            maxLines = 10,
            overflow = Clip,
            text = memoTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        TextField(
            value = memoText,
            onValueChange = { memoText = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
                minLines = 20,
            textStyle = TextStyle(fontSize = textFontSize)
        )
    }

}

