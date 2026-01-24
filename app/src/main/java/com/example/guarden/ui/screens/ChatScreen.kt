package com.example.guarden.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.guarden.ui.theme.GreenPrimary
import com.example.guarden.ui.theme.GreenSoft
import com.example.guarden.viewmodel.ChatMessage
import com.example.guarden.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Guarden AI Assistant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
        }

        HorizontalDivider(color = GreenSoft.copy(alpha = 0.3f))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.LocalFlorist,
                contentDescription = null,
                tint = GreenPrimary.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.Center)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                reverseLayout = false,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
            }
        }

        Column(
            modifier = Modifier.background(Color(0xFFF8F8F8))
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = GreenPrimary,
                    trackColor = Color.Transparent
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about your plants...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = GreenPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    }),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (isLoading) Color.Gray else GreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isFromUser) GreenPrimary else Color(0xFFE8F5E9)
    val textColor = if (message.isFromUser) Color.White else Color.Black
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start

    val bubbleShape = if (message.isFromUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(text = message.text, color = textColor)
        }
    }
}