package com.example.shoppinglist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// data class to represent a shopping item
data class ShoppingItem(val id: Int,
                        var name: String,
                        var quantity: Int,
                        var isEditing: Boolean = false
)

// main composable function
@Composable
fun ShoppingListApp() {

    // state to manage the list of shopping items
    var sItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    // state to manage the visibility of the Add Item dialog
    var showDialog by remember { mutableStateOf(false) }
    // state to manage the input for the item name
    var itemName by remember { mutableStateOf("")}
    // state to manage the input for the item quantity
    var itemQuantity by remember { mutableStateOf("")}
    var itemQuantityError by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Name") }
    var expanded by remember { mutableStateOf(false) }

    // layout to arrange the UI elements vertically
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement =  Arrangement.Center
    ) {
        // button to show the Add Item dialog
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add Item")
        }

        // sorting dropdown menu
        Column(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            Button(onClick = { expanded = true }) {
                Text("Sorty by $sortOption")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = {
                    Text("Name")
                }, onClick = {
                    sortOption = "Name"
                    expanded = false
                })
                DropdownMenuItem(text = {
                    Text("Quantity")
                }, onClick = {
                    sortOption = "Quantity"
                    expanded = false
                })
            }
        }


        // LazyColumn to display the list of shopping items
        LazyColumn(
            // it will push the "add item" button towards the top
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val sortedItems = when (sortOption) {
                "Name" -> sItems.sortedBy { it.name }
                "Quantity" -> sItems.sortedBy { it.quantity }
                else -> sItems
            }
            // iterate through the shopping items
            items(sortedItems) { item ->
                if (item.isEditing) {
                    // show item in editing mode
                    ShoppingItemEditor(item = item, onEditComplete = {
                            editedName, editedQuantity ->
                        sItems = sItems.map{ it.copy(isEditing = false )}
                        val editedItem = sItems.find{ it.id == item.id }
                        editedItem?.let {
                            it.name = editedName
                            it.quantity = editedQuantity
                        }
                    })
                } else {
                    // show item in view mode
                    ShoppingListItem(item = item, onEditClick = {
                        // enable editing mode for the selected item
                        sItems = sItems.map{it.copy(isEditing = it.id==item.id)}
                    }, onDeleteClick = {
                        // remove the selected item from the list
                        sItems = sItems - item
                    })
                }
            }
        }
    }

    // show the Add Item dialog if showDialog is trye
    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog= false },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // button to add the new item
                    Button(onClick = {
                        if (itemName.isNotBlank()) {
                            val newItem = ShoppingItem(
                                id = sItems.size+1,
                                name = itemName,
                                quantity = itemQuantity.toInt()
                            )
                            sItems = sItems + newItem
                            showDialog = false
                            itemName = ""
                            itemQuantity = ""
                            itemQuantityError = false
                        } else {
                            itemQuantityError = itemQuantity.toIntOrNull() == null || itemQuantity.toInt() <= 0
                        }
                    }) {
                        Text("Add")
                    }
                    // button to cancel adding the new item
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            title = { Text("Add Shopping Item")},
            text = {
                Column {
                    // input field for the item name
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text("Item Name") }
                    )
                    // input field for the item quantity
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text("Quantity") },
                        isError = itemQuantityError
                    )
                    if (itemQuantityError) {
                        Text(
                            text = "Please enter a valid quantity.",
                            color = Color.Red,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        )
    }

}

// composable function to edit a shopping item
@Composable
fun ShoppingItemEditor(item: ShoppingItem, onEditComplete: (String, Int) ->  Unit) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.quantity.toString()) }
    var isEditing by remember { mutableStateOf(item.isEditing) }

    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        Column {
            // input field for the edited item name
            BasicTextField(
                value = editedName,
                onValueChange = {editedName = it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
            // input field for the edited item quantity
            BasicTextField(
                value = editedQuantity,
                onValueChange = {editedQuantity = it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
        }

        // button to save the edited item
        Button(
            onClick = {
                isEditing = false
                onEditComplete(editedName, editedQuantity.toIntOrNull() ?: 1)
            }
        ) {
            Text("Save")
        }
    }
}

// composable function to display a shopping item
@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    // a lambda function gets executed when the edit action is triggered
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(2.dp, Color(0xFF00FFFF)),
                shape = RoundedCornerShape(20)
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // display item name
        Text(text = item.name, modifier =  Modifier.padding(8.dp))
        // display item quantity
        Text(text = "Qty: ${item.quantity}", modifier =  Modifier.padding(8.dp))

        Row(modifier = Modifier.padding(8.dp)) {
            // button to trigger edit mode
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            // button to delete the item
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}