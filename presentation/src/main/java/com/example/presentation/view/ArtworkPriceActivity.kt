package com.example.presentation.view

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DomainArtwork
import com.example.domain.model.DomainPrice
import com.example.domain.model.PriceWithUser
import com.example.presentation.R
import com.example.presentation.ui.theme.lightWhite
import com.example.presentation.utils.chart
import com.example.presentation.view.ui.theme.SowoonTheme
import com.example.presentation.viewModel.ArtworkViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ArtworkPriceActivity : ComponentActivity() {
    private val viewModel: ArtworkViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val argument = intent.getStringExtra("artwork")
        val artwork: DomainArtwork = Gson().fromJson(argument, DomainArtwork::class.java)
        viewModel.getArtworkPrice(artwork.category!!, artwork.key!!)

        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(
                color = Color.White,
                darkIcons = !isSystemInDarkTheme()
            )
            SowoonTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArtworkPriceScreen(viewModel, artwork)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkPriceTopBar(){
    var context = LocalContext.current
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "가격 현황",
                style = MaterialTheme.typography.titleMedium
            )
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
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

@Composable
fun priceInputDialog(priceTextField: String, onDismissRequest: () -> Unit, onInput: (String) -> Unit, onAddData: () -> Unit){
    AlertDialog(
        modifier = Modifier.wrapContentSize(),
        onDismissRequest = { onDismissRequest() },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                TextButton(
                    onClick = onAddData,
                    border = BorderStroke(0.5.dp, Color.LightGray),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "제시하기")
                }

                TextButton(
                    onClick = {
                        onDismissRequest()
                    },
                    border = BorderStroke(0.5.dp, Color.LightGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(text = "취소하기")
                }
            }
        },
        shape = RoundedCornerShape(15.dp),
        title = { Text(text = "가격 제시", fontSize = 20.sp, fontWeight = FontWeight.Bold ,modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)},
        text = {
                BasicTextField(
                    value = priceTextField,
                    onValueChange = onInput,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = lightWhite,
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .padding(8.dp)
                        ) {
                            innerTextField()
                        }
                    },
                )

        },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ArtworkPriceScreen(viewModel: ArtworkViewModel, artwork: DomainArtwork) {

    val priceSaveResult by viewModel.priceSaveResult.observeAsState()
    val priceListResult by viewModel.priceListResult.observeAsState(emptyList())

    var loading by remember { mutableStateOf(true) }

    // x축 (날짜)와 y축 (가격) 데이터를 관리
    val xData by viewModel.xData.observeAsState()
    val yData by viewModel.yData.observeAsState()

    Log.d("ArtworkPriceScreen_xData", xData.toString())
    Log.d("ArtworkPriceScreen_yData", yData.toString())

    var priceTextField by remember { mutableStateOf("") }
    var priceInputBtn by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column {
        ArtworkPriceTopBar()
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        ){
            Column {
                if (!xData.isNullOrEmpty() && !yData.isNullOrEmpty()) {
                    loading = false
                    chart(xData = xData!!, yData = yData!!, dataLabel = "가격현황", modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .padding(all = 8.dp)
                    )
                    priceUserList(priceListResult,
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp))
                }else{
                    loading = true
                }
            }

            if(loading){
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            TextButton(
                onClick = {
                    priceInputBtn = true
                },
                border = BorderStroke(width = 0.5.dp, color = colorResource(id = R.color.lightgray)),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                contentPadding = PaddingValues(top = 15.dp, bottom = 15.dp)
            ) {
                Text(text = "가격 제시하기", fontSize = 18.sp, color = Color.Black)
            }
            if(priceInputBtn){
                priceInputDialog(priceTextField, {priceInputBtn = false}, { priceTextField = it}, onAddData = {
                    val x = priceTextField.toFloatOrNull()
                    if(x != null){
                        val currentDate = LocalDate.now()
                        val formatter = DateTimeFormatter.ofPattern("MM-dd") // 원하는 날짜 형식
                        val formattedDate = currentDate.format(formatter)
                        viewModel.addChartData(formattedDate, priceTextField.toFloat())
                        viewModel.setPriceForArtwork(
                            category = artwork.category!!,
                            artworkId = artwork.key!!,
                            price = priceTextField.toFloat()
                        )
                        priceTextField = ""
                        priceInputBtn = false
                    }else{
                        Toast.makeText(context, "가격을 기입해주세요.", Toast.LENGTH_SHORT).show()
                    }
                } )
            }
            priceSaveResult?.let { result ->
                Log.d("priceSaveResult", result.toString())
                when (result.isSuccess) {
                    true -> {
                        Toast.makeText(context,"가격이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    false -> {
                        Toast.makeText(context,"가격 저장에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun priceUserList(priceListResult: List<PriceWithUser>?, modifier: Modifier) {
    val date = priceListResult?.groupBy { it.date } ?: emptyMap()
    LazyColumn(
        modifier = modifier
    ){
        date.forEach{(date, priceList) ->
            stickyHeader {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.date_text))
                    .padding(vertical = 8.dp)
                ){
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.Black),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                    )
                }
            }
            items(priceList?.size!!){index ->
                priceUser(priceList[index])
            }
        }
    }
}
@Composable
fun priceUser(price: PriceWithUser) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RectangleShape,
        border = BorderStroke(width = 0.5.dp, color = colorResource(id = R.color.lightgray)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = price.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = price.date,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            Text(
                text = "${price.price.toInt()}만원",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}