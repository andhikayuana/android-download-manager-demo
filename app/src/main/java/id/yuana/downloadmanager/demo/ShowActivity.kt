package id.yuana.downloadmanager.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ShowActivity : ComponentActivity() {

    companion object {
        const val EXTRA_JOKES = "extra_jokes"

        fun createIntent(context: Context, jokes: Array<String>): Intent =
            Intent(context, ShowActivity::class.java).apply {
                putExtra(EXTRA_JOKES, jokes)
            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jokes = intent.getStringArrayExtra(EXTRA_JOKES) ?: emptyArray()

        setContent {
            MaterialTheme {
                screenShow(jokes) {
                    onBackPressed()
                }
            }
        }
    }
}

@Composable
fun screenShow(jokes: Array<String>, onBackPressed: (() -> Unit)) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Show Jokes") },
                navigationIcon = {
                    IconButton(onClick = {
                        onBackPressed()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        content = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = jokes, itemContent = { item ->
                    cardJoke(joke = item)
                })
            }
        }
    )
}

@Composable
fun cardJoke(joke: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.clickable {
                //do nothing
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(15.dp)
            ) {
                Text(text = joke, style = TextStyle(fontSize = 18.sp))
            }
        }

    }
}