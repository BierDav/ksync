package at.quickme.ksync.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.quickme.ksync.example.db.Database
import at.quickme.ksync.example.entity.Todo
import at.quickme.ksync.example.repo.TodoRepo
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


// 2. The main composable that holds the state
@Composable
fun TodoApp() {
    // State for the text field
    var text by remember { mutableStateOf("") }
    val database = koinInject<Database>()
    val todoRepo = koinInject<TodoRepo>()
    val coroutineScope = rememberCoroutineScope()

    // State for the list of todo items
    // We use mutableStateListOf so the UI recomposes when the list changes
    val todoItems by remember {
        todoRepo.findAllFlow(database)
            .mapNotNull { it.getOrNull() }
    }.collectAsStateWithLifecycle(emptyList())

    val isConnected by database.isConnected.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val test = database.fetchAll("select * from todo").getOrThrow()
        println(test)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            Button(onClick = {
                if (isConnected)
                    database.disconnect()
                else
                    database.connect()

            }) {
                Text(if (isConnected) "Disconnect" else "Connect")
            }
        }
        // --- Input Area ---
        TodoInput(
            text = text,
            onTextChange = { newText -> text = newText },
            onAddItem = {
                if (text.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            database.transaction {
                                try {
                                    todoRepo.insert(this, Todo(task = text))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            text = "" // Clear the text field after adding
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- List Area ---
        TodoList(
            items = todoItems,
            onToggleItem = { item ->
                // Find the item and create a new one with the toggled 'isDone' state
                val index = todoItems.indexOf(item)
                if (index != -1) {
                    coroutineScope.launch {
                        database.transaction {
                            todoRepo.update(this, item.copy(isDone = !item.isDone))
                        }
                    }
                }
            },
            onRemoveItem = { item ->
                coroutineScope.launch {
                    database.transaction {
                        todoRepo.delete(this, item)
                    }
                }
            }
        )
    }
}

// 3. Composable for the text field and "Add" button
@Composable
fun TodoInput(
    text: String,
    onTextChange: (String) -> Unit,
    onAddItem: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("New task") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onAddItem) {
            Text("Add")
        }
    }
}

// 4. Composable for the list of items
@Composable
fun TodoList(
    items: List<Todo>,
    onToggleItem: (Todo) -> Unit,
    onRemoveItem: (Todo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            TodoListItem(
                item = item,
                onToggle = { onToggleItem(item) },
                onRemove = { onRemoveItem(item) }
            )
            HorizontalDivider()
        }
    }
}

// 5. Composable for a single row (item) in the list
@Composable
fun TodoListItem(
    item: Todo,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = { onToggle() }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.task,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                color = if (item.isDone) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Task"
            )
        }
    }
}
