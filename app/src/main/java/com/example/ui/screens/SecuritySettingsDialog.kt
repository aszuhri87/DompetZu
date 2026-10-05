package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

enum class SecurityDialogStep {
    OVERVIEW,
    SETUP_PIN_ENTER,
    SETUP_PIN_CONFIRM,
    CHANGE_PIN_OLD,
    CHANGE_PIN_NEW,
    CHANGE_PIN_CONFIRM,
    DISABLE_PIN_VERIFY
}

@Composable
fun SecuritySettingsDialog(
    isLockEnabled: Boolean,
    onDismiss: () -> Unit,
    onSetPin: (newPin: String) -> Unit,
    onVerifyPin: (pin: String) -> Boolean,
    onDisableLock: (currentPin: String) -> Boolean,
    onChangePin: (oldPin: String, newPin: String) -> Boolean,
    onLockNow: () -> Unit
) {
    var step by remember { mutableStateOf(SecurityDialogStep.OVERVIEW) }
    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var oldPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Keamanan Sandi PIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    SecurityDialogStep.OVERVIEW -> {
                        // Status Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLockEnabled) IncomeGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isLockEnabled) IncomeGreen else Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isLockEnabled) "App Lock Aktif" else "App Lock Nonaktif",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isLockEnabled) IncomeGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isLockEnabled) "Aplikasi terlindungi sandi PIN" else "Belum dilindungi PIN sandi",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (successMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✅ $successMessage",
                                fontSize = 12.sp,
                                color = IncomeGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isLockEnabled) {
                            Button(
                                onClick = onLockNow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lock_now_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kunci Aplikasi Sekarang")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    step = SecurityDialogStep.CHANGE_PIN_OLD
                                    oldPinInput = ""
                                    pinInput = ""
                                    confirmPinInput = ""
                                    errorMessage = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("change_pin_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ubah PIN Sandi")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    step = SecurityDialogStep.DISABLE_PIN_VERIFY
                                    pinInput = ""
                                    errorMessage = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("disable_pin_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Nonaktifkan Kunci Sandi")
                            }
                        } else {
                            Button(
                                onClick = {
                                    step = SecurityDialogStep.SETUP_PIN_ENTER
                                    pinInput = ""
                                    confirmPinInput = ""
                                    errorMessage = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("enable_pin_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aktifkan Kunci PIN Sandi")
                            }
                        }
                    }

                    SecurityDialogStep.SETUP_PIN_ENTER -> {
                        Text(
                            text = "Langkah 1/2: Masukkan 4-Digit PIN Baru",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                            label = { Text("PIN Baru (4 Digit)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_pin_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = errorMessage, color = ExpenseRed, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (pinInput.length == 4) {
                                    step = SecurityDialogStep.SETUP_PIN_CONFIRM
                                    errorMessage = ""
                                } else {
                                    errorMessage = "PIN harus terdiri dari 4 digit angka."
                                }
                            },
                            enabled = pinInput.length == 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("continue_confirm_pin_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lanjutkan Konfirmasi")
                        }
                    }

                    SecurityDialogStep.SETUP_PIN_CONFIRM -> {
                        Text(
                            text = "Langkah 2/2: Konfirmasi PIN Baru Anda",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPinInput = it },
                            label = { Text("Ulangi PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_pin_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = errorMessage, color = ExpenseRed, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (confirmPinInput == pinInput) {
                                    onSetPin(pinInput)
                                    successMessage = "Kunci PIN berhasil diaktifkan!"
                                    step = SecurityDialogStep.OVERVIEW
                                } else {
                                    errorMessage = "PIN konfirmasi tidak cocok. Coba lagi."
                                }
                            },
                            enabled = confirmPinInput.length == 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_new_pin_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Simpan dan Aktifkan PIN")
                        }
                    }

                    SecurityDialogStep.CHANGE_PIN_OLD -> {
                        Text(
                            text = "Masukkan PIN Lama Anda:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = oldPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) oldPinInput = it },
                            label = { Text("PIN Lama") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("old_pin_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = errorMessage, color = ExpenseRed, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (onVerifyPin(oldPinInput)) {
                                    step = SecurityDialogStep.CHANGE_PIN_NEW
                                    errorMessage = ""
                                } else {
                                    errorMessage = "PIN lama tidak sesuai."
                                }
                            },
                            enabled = oldPinInput.length == 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Verifikasi")
                        }
                    }

                    SecurityDialogStep.CHANGE_PIN_NEW -> {
                        Text(
                            text = "Masukkan PIN Baru (4 Digit):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                            label = { Text("PIN Baru") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (pinInput.length == 4) {
                                    step = SecurityDialogStep.CHANGE_PIN_CONFIRM
                                }
                            },
                            enabled = pinInput.length == 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lanjutkan")
                        }
                    }

                    SecurityDialogStep.CHANGE_PIN_CONFIRM -> {
                        Text(
                            text = "Konfirmasi PIN Baru Anda:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPinInput = it },
                            label = { Text("Ulangi PIN Baru") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = errorMessage, color = ExpenseRed, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (confirmPinInput == pinInput) {
                                    onChangePin(oldPinInput, pinInput)
                                    successMessage = "PIN berhasil diubah!"
                                    step = SecurityDialogStep.OVERVIEW
                                } else {
                                    errorMessage = "PIN konfirmasi tidak sama."
                                }
                            },
                            enabled = confirmPinInput.length == 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Simpan Perubahan PIN")
                        }
                    }

                    SecurityDialogStep.DISABLE_PIN_VERIFY -> {
                        Text(
                            text = "Masukkan PIN saat ini untuk menonaktifkan penguncian:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                            label = { Text("PIN Saat Ini") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_disable_pin_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = errorMessage, color = ExpenseRed, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val ok = onDisableLock(pinInput)
                                if (ok) {
                                    successMessage = "Kunci sandi telah dinonaktifkan."
                                    step = SecurityDialogStep.OVERVIEW
                                } else {
                                    errorMessage = "PIN tidak sesuai."
                                }
                            },
                            enabled = pinInput.length == 4,
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_disable_pin_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Konfirmasi Nonaktifkan")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step != SecurityDialogStep.OVERVIEW) {
                TextButton(onClick = { step = SecurityDialogStep.OVERVIEW }) {
                    Text("Kembali")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Selesai")
                }
            }
        }
    )
}
