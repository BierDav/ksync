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
import at.quickme.ksync.at.quickme.ksync.example.entity.Todo


// 2. The main composable that holds the state
@Composable
fun TodoApp() {
    // State for the text field
    var text by remember { mutableStateOf("") }

    // State for the list of todo items
    // We use mutableStateListOf so the UI recomposes when the list changes
    val todoItems = remember { mutableStateListOf<Todo>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Input Area ---
        TodoInput(
            text = text,
            onTextChange = { newText -> text = newText },
            onAddItem = {
                if (text.isNotBlank()) {
                    todoItems.add(Todo(task = text))
                    text = "" // Clear the text field after adding
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
                    todoItems[index] = item.copy(isDone = !item.isDone)
                }
            },
            onRemoveItem = { item ->
                todoItems.remove(item)
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
        items(items, key = { it.task }) { item ->
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
