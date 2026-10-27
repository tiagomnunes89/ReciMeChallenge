package com.recime.challenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.ui.navigation.HomeNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReciMeTheme {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.semantics { testTagsAsResourceId = true },
                ) {
                HomeNavHost()
                }
            }
        }
    }
}
