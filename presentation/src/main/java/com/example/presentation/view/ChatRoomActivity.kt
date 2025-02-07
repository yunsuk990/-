package com.example.presentation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import com.example.domain.model.DomainArtwork
import com.example.domain.model.DomainMessage
import com.example.domain.model.DomainUser
import com.example.presentation.R
import com.example.presentation.utils.noRippleClickable
import com.example.presentation.view.ui.theme.SowoonTheme
import com.example.presentation.viewModel.ChatRoomViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatRoomActivity : ComponentActivity() {

    private val viewModel: ChatRoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val artworkIntent = intent.getStringExtra("artwork")
        val artwork: DomainArtwork = Gson().fromJson(artworkIntent, DomainArtwork::class.java)

        val destUserIntent = intent.getStringExtra("destUser")
        val destUser: DomainUser = Gson().fromJson(destUserIntent, DomainUser::class.java)

        Log.d("ChatRoomActivity_destUser", destUser.toString())

        setContent {

            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(
                color = Color.White,
                darkIcons = !isSystemInDarkTheme()
            )

            LaunchedEffect(Unit) {
                Log.d("chatRoomRoot_LaunchedEffect","called")
                viewModel.checkChatRoom(artwork.artistUid!!, artwork.key!!)
            }

            val currentUserUid by viewModel.currentUserUid.collectAsState()
            val messageList by viewModel.messageList.collectAsState()
            var inputMessage by remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current

                // A surface container using the 'background' color from the theme

            SowoonTheme {
                Scaffold(
                    topBar = {
                        ChatRoomActivityTopBar(name = artwork.name.toString())
                    },
                    modifier = Modifier.fillMaxSize().background(Color.White)
                ){ padding ->
                    chatRoomRoot(
                        inputMessage = inputMessage,
                        currentUserUid = currentUserUid,
                        messageList = messageList,
                        destUser = destUser,
                        onValueChange = { inputMessage = it },
                        inputBtnOnClicked = {
                            viewModel.sendMessage(
                                message = inputMessage,
                                opponentUid = artwork.artistUid!!,
                                artworkId = artwork.key!!
                            )
                            inputMessage = ""
                        },
                        modifier = Modifier.fillMaxSize().background(Color.White).padding(padding).noRippleClickable {
                            keyboardController?.hide()
                        }
                    )
                }
            } // A surface container using the 'background' color from the theme
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun chatRoomRoot(
    inputMessage: String,
    currentUserUid: String,
    destUser: DomainUser,
    messageList: List<DomainMessage>,
    onValueChange: (String) -> Unit,
    inputBtnOnClicked: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
    ) {
        MessageList(currentUserUid, messageList, destUser, Modifier.weight(1f).padding(start = 5.dp , end = 8.dp))
        userMessageTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .imePadding(),
            inputMessage = inputMessage,
            onValueChange = onValueChange,
            inputBtnOnClicked = { inputBtnOnClicked() }
        )
    }

}

@Composable
fun MessageList(
    currentUserUid: String,
    messageList: List<DomainMessage>,
    destUser: DomainUser,
    modifier: Modifier
) {
    var listState = rememberLazyListState(initialFirstVisibleItemIndex = messageList.size)
    Log.d("MessageList_messageListUpdate", messageList.toString())

    LaunchedEffect(key1 = messageList.size) {
        if(messageList.isNotEmpty()){
            listState.scrollToItem(messageList.size - 1)
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        itemsIndexed(messageList){ index, item ->
            ChatBubble(messageList[index], currentUserUid, destUser)
        }
    }
}

@Composable
fun ChatBubble(message: DomainMessage, currentUserUid: String, destUser: DomainUser) {
    val isCurrentUser = message.senderUid == currentUserUid
    val bubbleColor = if(isCurrentUser) Color.Black else colorResource(R.color.messageGray)
    val textColor = if(isCurrentUser) Color.White else Color.Black

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp)
    ){
        val alignment = if(isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart

        Row(modifier = Modifier.align(alignment)){
            if(!isCurrentUser){
                userProfileImage(destUser.profileImage)
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                if(!isCurrentUser){
                    Text(text = destUser.name, color = Color.Black, fontSize = 16.sp)
                }
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .clip(
                            if(isCurrentUser){
                                RoundedCornerShape(topStart = 15.dp, bottomStart = 15.dp, topEnd = 15.dp)
                            }else{
                                RoundedCornerShape(topEnd = 15.dp, bottomStart = 15.dp, bottomEnd = 15.dp)
                            }
                        )
                        .background(bubbleColor)
                ){
                    Text(text = message.message, color = textColor, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp))
                }
            }
        }
    }
}

@Composable
fun userProfileImage(imageUrl: String){
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(50.dp)
            .background(color = colorResource(id = R.color.lightgray)),
        contentAlignment = Alignment.Center,
    ){
//        Image(
//            painter = painterResource(id = imageUrl.toInt()),
//            contentDescription = null
//        )
        if(imageUrl.isEmpty()){
            Icon(painter = painterResource(R.drawable.profile), contentDescription = null, modifier = Modifier.size(20.dp))
        }else{
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun userMessageTextField(
    modifier: Modifier,
    inputMessage: String,
    onValueChange: (String) -> Unit = {},
    inputBtnOnClicked: () -> Unit,
){
    OutlinedTextField(
        value = inputMessage,
        modifier = modifier,
        onValueChange = { onValueChange(it)},
        placeholder = { Text(text = "메세지를 작성해주세요.")},
        shape = RoundedCornerShape(20.dp),
        trailingIcon = {
            IconButton(onClick = {
                if(inputMessage.isNotEmpty()) {inputBtnOnClicked()}
            }) {
                Icon(painter = painterResource(id = R.drawable.send), contentDescription = null)
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorResource(R.color.messageGray),
            unfocusedContainerColor = colorResource(R.color.messageGray),
            focusedIndicatorColor = Color.LightGray,
            unfocusedIndicatorColor = Color.Transparent,
            //disabledIndicatorColor = Color.Transparent,
            //cursorColor = Color.Black,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomActivityTopBar(name: String){
    var context = LocalContext.current
    Column() {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black,
                actionIconContentColor = Color.Black
            ),
            navigationIcon = {
                IconButton(onClick = {
                    (context as Activity).finish()
                }) { Icon(painter = painterResource(id = R.drawable.back), contentDescription = "뒤로가기") }
            },
        )
        Divider(thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    Scaffold(
        topBar = {
            ChatRoomActivityTopBar("asdf")
        },
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        chatRoomRoot(
            inputMessage = "",
            currentUserUid = "123",
            messageList = listOf(
                DomainMessage(message = "hi", senderUid = "123"),
                DomainMessage(message = "hi", senderUid = "1234"),
                DomainMessage(message = "Im very intersted in this artwork. Could you please provide me with more details about it?", senderUid = "123"),
                DomainMessage(message = "Im very intersted in this artwork. Could you please provide me with more details about it?", senderUid = "123"),
                DomainMessage(message = "Im very intersted in this artwork. Could you please provide me with more details about it?", senderUid = "123"),
                DomainMessage(message = "Im very intersted in this artwork. Could you please provide me with more details about it?", senderUid = "123"),
                DomainMessage(message = "Im very intersted in this artwork. Could you please provide me with more details about it?", senderUid = "123"),
                DomainMessage(message = "Im very intersted in this artwork. Could you please provide me with more details about it?", senderUid = "123"),
                DomainMessage(message = "Im very intersted in this artwork. Could you please provide me with more details about it?", senderUid = "123"),
            ),
            onValueChange = {},
            inputBtnOnClicked = {},
            destUser = DomainUser(name = "최윤석"),
            modifier = Modifier.fillMaxSize().padding(it)
        )
    }
}