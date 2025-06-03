package com.example.myposition.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp



@Composable
fun ChooseRadioButton(
    modifier: Modifier = Modifier,
    chooseHandler: (String) -> Unit,
    initSelect: String
) {

    val radioOptions = listOf("Image", "Video")
    var selectedOption = rememberSaveable { mutableStateOf(initSelect) }

    fun onOptionSelected(text: String){
        //Update internal state
        selectedOption.value = text

        //Call parent handler
        chooseHandler(text)
    }

    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    //.fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption.value),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption.value),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}