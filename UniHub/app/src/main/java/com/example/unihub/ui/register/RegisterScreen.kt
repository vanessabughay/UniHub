package com.example.unihub.ui.register

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.unihub.ui.theme.unihubTheme

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {

    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.success = false
            navController.navigate("login")
        }
    }

    Box(modifier = Modifier.fillMaxSize() .background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .wrapContentHeight()
                    .background(
                        color = Color(0xFFD9D9D9).copy(alpha = 0.33f),
                        shape = RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cadastre-se",
                        fontSize = 32.sp,
                        color = Color(0xFF243C5B),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val inputBackgroundColor = Color(0xFFC1D5E4).copy(alpha = 0.66f)
                    val labelColor = MaterialTheme.colorScheme.onBackground
                    val textColor = Color.Black

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Nome",
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                        TextField(
                            value = viewModel.name,
                            onValueChange = { viewModel.name = it },
                            modifier = Modifier
                                .width(285.dp)
                                .height(50.dp),
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "E-mail",
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                        TextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            modifier = Modifier
                                .width(285.dp)
                                .height(50.dp),
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reimplementação do CustomLabeledInput para o campo "Senha"
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Senha",
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                        TextField(
                            value = viewModel.password,
                            onValueChange = { viewModel.password = it },
                            modifier = Modifier
                                .width(285.dp)
                                .height(50.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Password
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.registerUser()
                        },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .width(120.dp)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC1D5E4)
                        )
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = Color.Gray,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Cadastrar",
                                color = Color.Black.copy(alpha = 0.68f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Já possui conta?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Entrar",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    unihubTheme {
        RegisterScreen(navController = navController)
    }
}