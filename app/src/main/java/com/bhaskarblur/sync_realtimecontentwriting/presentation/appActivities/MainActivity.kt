package com.bhaskarblur.sync_realtimecontentwriting.presentation.appActivities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bhaskarblur.sync_realtimecontentwriting.presentation.Screens
import com.bhaskarblur.sync_realtimecontentwriting.presentation.registration.SignUpPage
import com.bhaskarblur.sync_realtimecontentwriting.presentation.document.DocumentViewModel
import com.bhaskarblur.sync_realtimecontentwriting.presentation.documentsHome.DocumentsList
import com.bhaskarblur.sync_realtimecontentwriting.presentation.documentsHome.SearchDocumentsPage
import com.bhaskarblur.sync_realtimecontentwriting.presentation.registration.LoginPage
import com.bhaskarblur.sync_realtimecontentwriting.presentation.registration.SignUpViewModel
import com.bhaskarblur.sync_realtimecontentwriting.presentation.widgets.NoInternetPage
import com.bhaskarblur.sync_realtimecontentwriting.ui.theme.backgroundColor
import com.bhaskarblur.sync_realtimecontentwriting.ui.theme.primaryColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var documentViewModel: DocumentViewModel
    private lateinit var userViewModel: SignUpViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = viewModels<SignUpViewModel>().value
        val loggedData by userViewModel.userState
        val networkAvailable by userViewModel.isInternetAvailable

        setContent {
            val context = LocalContext.current
            val scaffoldState = remember { SnackbarHostState() }
            val currentPage = rememberSaveable {
                mutableStateOf("")
            }
            val navController = rememberNavController()

            navController.addOnDestinationChangedListener { _, _, _ -> userViewModel.checkNetworkAvailable() }

            LaunchedEffect(userViewModel.event.value) {
                if (userViewModel.event.value.isNotEmpty()) {
                    Toast.makeText(context, userViewModel.event.value, Toast.LENGTH_SHORT).show()
                    userViewModel.event.value = ""
                }
            }
            LaunchedEffect(networkAvailable, loggedData) {
                if (userViewModel.checkNetworkAvailable()) {
                    userViewModel.initData()
                    delay(500)
                    Log.d("userData__", loggedData.id.isNullOrEmpty().toString())
                    if (!loggedData.id.isNullOrEmpty()) {
                        currentPage.value = Screens.HomePage.route
                        documentViewModel.setUser()

                    } else {
                        currentPage.value = Screens.RegistrationPage.route
                    }
                } else {
                    currentPage.value = Screens.NoInternet.route
                }
            }

            Scaffold(
                snackbarHost = { SnackbarHost(hostState = scaffoldState) }) {
                it
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(backgroundColor)

                ) {
                    if (currentPage.value.isNotEmpty()) {
                        NavHost(
                            navController = navController,
                            startDestination = currentPage.value,
                        ) {

                            composable(
                                route = Screens.RegistrationPage.route
                            ) {
                                SignUpPage(userViewModel, navController, context)
                            }
                            composable(
                                route = Screens.HomePage.route
                            ) {
                                DocumentsList(
                                    userViewModel, documentViewModel,
                                    context, navController
                                )
                            }

                            composable(
                                route = Screens.LoginPage.route
                            ) {
                                LoginPage(userViewModel, navController, context)
                            }

                            composable(
                                route = Screens.SearchDocumentPage.route
                            ) {
                                SearchDocumentsPage(documentViewModel, navController, context)
                            }

                            composable(
                                route = Screens.NoInternet.route
                            ) {
                                NoInternetPage {
                                    userViewModel.checkNetworkAvailable()
                                }

                            }
                        }
                    } else {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = primaryColor,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
