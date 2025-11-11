package at.quickme.ksync.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import at.quickme.ksync.example.di.appModule
import at.quickme.ksync.example.di.appRepoModule
import at.quickme.ksync.example.di.repoModule
import org.koin.compose.KoinApplication

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        alwaysOnTop = true,
        title = "composedemo",
    ) {
       KoinApplication({
           modules(repoModule, appRepoModule,appModule)
       }){

        MaterialTheme {
            TodoApp()
        }
       }
    }
}