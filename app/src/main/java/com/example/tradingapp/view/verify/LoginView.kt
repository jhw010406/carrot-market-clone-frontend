package com.example.tradingapp.view.verify

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tradingapp.model.data.navigation.MainNavigationGraph
import com.example.tradingapp.viewmodel.verify.LoginViewModel
import com.example.tradingapp.viewmodel.verify.UserInformationViewModel
import com.example.tradingapp.utils.ui.theme.getTextFieldColors
import com.example.tradingapp.view.other.LoadingBar
import com.example.tradingapp.view.other.LoadingView
import com.example.tradingapp.view.other.RootSnackbar

@Composable
fun LoginView(
    tag : String,
    loginViewModel : LoginViewModel,
    myInfo : UserInformationViewModel,
    navController : NavHostController
){
    var inputID by remember { mutableStateOf("") }
    var inputPW by remember { mutableStateOf("") }
    var keepLogin by remember { mutableStateOf(false) }
    var isBlankInputID by remember { mutableStateOf(false) }
    var isBlankInputPW by remember { mutableStateOf(false) }
    var autoLoginSucceed by remember { mutableStateOf(true) }
    val currentContext = LocalContext.current
    val currentStackEntry by navController.currentBackStackEntryAsState()
    val currentViewRoute = currentStackEntry?.destination?.route

    BackHandler {
        (currentContext as Activity).finish()
    }

    LaunchedEffect(Unit) {

        if (loginViewModel.doAutoLogin(tag)){
            loginViewModel.login(
                tag,
                loginViewModel.myCertificate!!.id,
                loginViewModel.myCertificate!!.password,
                true
            ) { getUserInformation, isSuccessful ->

                if (isSuccessful){
                    myInfo.userInfo.value = getUserInformation
                    myInfo.userInfo.value!!.id = loginViewModel.myCertificate!!.id
                    navController.navigate(MainNavigationGraph.HOME.name){
                        popUpTo(MainNavigationGraph.LOGIN.name){ inclusive = true }
                    }
                }
                else {
                    loginViewModel.deleteLocalData()
                    autoLoginSucceed = false
                    RootSnackbar.show("자동 로그인 실패")
                }
            }
        }
        else {
            loginViewModel.deleteLocalData()
            autoLoginSucceed = false
        }
    }

    if (autoLoginSucceed) {
        LoadingView()
    }
    else {
        Surface (modifier = Modifier.fillMaxSize()){
            Column (
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                // 아이디
                Column (horizontalAlignment = Alignment.Start){
                    OutlinedTextField(
                        value = inputID,
                        placeholder = { Text(text = "ID 입력") },
                        colors = getTextFieldColors(),
                        singleLine = true,
                        onValueChange = { input ->
                            if (restrictInputID(input)){ inputID = input }
                        }
                    )
                    Spacer(modifier = Modifier.size(4.dp))

                    Column (
                        modifier = Modifier
                            .width(280.dp)
                            .height(24.dp)
                    ) {
                        if (isBlankInputID){ Text(text = "아이디를 입력해주세요.", color = Color.Red) }
                    }
                }
                Spacer(modifier = Modifier.size(36.dp))

                // 비밀번호
                Column (horizontalAlignment = Alignment.Start){
                    OutlinedTextField(
                        value = inputPW,
                        placeholder = { Text(text = "비밀번호 입력") },
                        colors = getTextFieldColors(),
                        singleLine = true,
                        onValueChange = { input ->
                            if (restrictInputPW(input)){ inputPW = input }
                        }
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Column (modifier = Modifier
                        .width(280.dp)
                        .height(24.dp)) {
                        if (isBlankInputPW){
                            Text(text = "비밀번호를 입력해주세요.", color = Color.Red)
                        }
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))

                // keep login state
                Row (
                    modifier = Modifier.clickable { keepLogin = !keepLogin },
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(
                        checked = keepLogin,
                        onCheckedChange = { keepLogin = !keepLogin },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = MaterialTheme.colorScheme.outline,
                            checkedColor = Color(0xFFFF6E1D),
                            checkmarkColor = Color.White
                        )
                    )
                    Text(text = "로그인 유지")
                }
                Spacer(modifier = Modifier.size(40.dp))

                // 로그인 버튼
                Surface (
                    modifier = Modifier
                        .width(160.dp)
                        .wrapContentHeight()
                        .clickable {
                            if (inputID.isNotBlank() && inputPW.isNotBlank()) {
                                loginViewModel.login(
                                    tag,
                                    inputID,
                                    inputPW,
                                    keepLogin
                                ) { getUserInformation, isSuccessful ->

                                    if (isSuccessful) {
                                        myInfo.userInfo.value = getUserInformation
                                        myInfo.userInfo.value!!.id =
                                            loginViewModel.myCertificate!!.id
                                        navController.navigate(MainNavigationGraph.HOME.name) {
                                            if (currentViewRoute != null) {
                                                popUpTo(currentViewRoute) { inclusive = true }
                                            }
                                        }
                                    }
                                    // wrong id or password or connect server failed
                                    else {
                                        isBlankInputID = false
                                        isBlankInputPW = false
                                    }
                                }
                            } else {
                                isBlankInputID = inputID.isBlank()
                                isBlankInputPW = inputPW.isBlank()
                            }
                        },
                    shape = RoundedCornerShape(12),
                    color = Color(0xFFFF6E1D)
                ){
                    Text(
                        text = "로그인",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))

                // 회원가입 버튼
                Text(
                    text = "계정이 없으신가요? 회원가입 하기",
                    modifier = Modifier.clickable {
                        navController.navigate(MainNavigationGraph.REGISTER.name)
                    }
                )
            }
        }
    }
}