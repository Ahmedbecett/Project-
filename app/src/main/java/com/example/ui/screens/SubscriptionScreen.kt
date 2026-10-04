package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.SubscriptionPlan
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun SubscriptionScreen(
    isPremium: Boolean,
    onSubmitPayment: (String, String, SubscriptionPlan, PaymentMethod, String, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val baridiMobAccount = "002440629137"
    val binanceWallet = "0x0ccf01Ce03c1A485a130Ae63607e922Ff6772191"

    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.MONTHLY_3) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BARIDIMOB) }

    var userNameInput by remember { mutableStateOf("") }
    var userContactInput by remember { mutableStateOf("") }
    var transactionRefInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    var paymentSubmittedSuccess by remember { mutableStateOf(false) }

    if (paymentSubmittedSuccess) {
        AlertDialog(
            onDismissRequest = { paymentSubmittedSuccess = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎉", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تم إرسال طلب الاشتراك بنجاح!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("شكراً لك! تم استلام بيانات المعاملة وسيتم مراجعتها وتفعيل باقة ${selectedPlan.titleAr} في أقرب وقت.")
                    Text("يمكنك أيضاً إرسال إشعار مباشر عبر البريد إلى ahmedbecetti41@gmail.com لتسريع التفعيل.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        paymentSubmittedSuccess = false
                        onBack()
                    }
                ) {
                    Text("حسناً")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("subscription_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ترقية الباقة واشتراكات الدفع",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Surface(
                shape = CircleShape,
                color = GoldYellow.copy(alpha = 0.15f),
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "👑", fontSize = 34.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isPremium) "أنت مشترك في الباقة المميزة (VIP)" else "فتح جميع المستويات والذكاء الاصطناعي",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "المستوى A1 مجاني للجميع. المستويات (A2, B1, B2, C1, C2) ومحادثات الذكاء الاصطناعي والشهادات تتطلب اشتراكاً بتكلفة متوسطة ومناسبة.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Step 1: Select Plan
            Text(
                text = "1. اختر باقة الاشتراك المناسبة لك:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubscriptionPlan.values().forEach { plan ->
                    val isSelected = plan == selectedPlan
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlan = plan }
                            .testTag("plan_card_${plan.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPlan = plan }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plan.titleAr,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    plan.badge?.let { b ->
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldYellow
                                        ) {
                                            Text(
                                                text = b,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "صالحة لمدة ${plan.durationDays} يوماً لجميع اللغات والمستويات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${plan.priceDzd} دج",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${plan.priceUsdt} USDT",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 2: Choose Payment Method
            Text(
                text = "2. اختر وسيلة الدفع (بريدي موب أو بينانس):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaymentMethod.values().forEach { method ->
                    val isSelected = method == selectedMethod
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMethod = method }
                            .testTag("method_${method.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = method.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (method == PaymentMethod.BARIDIMOB) "بريدي موب سيسيبي" else "بينانس USDT",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Details Box with Copy
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedMethod == PaymentMethod.BARIDIMOB) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📮", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بيانات التحويل عبر تطبيق بريدي موب (BaridiMob):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "اسم المستفيد: Ahmed Becetti", style = MaterialTheme.typography.bodySmall)
                        Text(text = "المبلغ المطلوب: ${selectedPlan.priceDzd} دج", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "رقم الحساب / RIP البريدي:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = baridiMobAccount,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("BaridiMob", baridiMobAccount))
                                        Toast.makeText(context, "تم نسخ رقم بريدي موب: $baridiMobAccount", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بيانات الدفع عبر محفظة بينانس (Binance Pay / USDT):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "الشبكة: BNB Smart Chain (BEP20)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(text = "المبلغ المطلوب: ${selectedPlan.priceUsdt} USDT", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "عنوان المحفظة (BEP20):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = binanceWallet,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Binance Wallet", binanceWallet))
                                        Toast.makeText(context, "تم نسخ محفظة بينانس", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 3: Enter Transaction Confirmation Details
            Text(
                text = "3. أدخل تفاصيل المعاملة بعد التحويل لتفعيل حسابك:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = userNameInput,
                onValueChange = { userNameInput = it },
                label = { Text("الاسم الكامل") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userContactInput,
                onValueChange = { userContactInput = it },
                label = { Text("رقم الهاتف أو البريد الإلكتروني للتواصل") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = transactionRefInput,
                onValueChange = { transactionRefInput = it },
                label = { Text(if (selectedMethod == PaymentMethod.BARIDIMOB) "رقم العملية / وصل التحويل في بريدي موب" else "معرف المعاملة TxID في بينانس") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("ملاحظة إضافية (اختياري)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (userNameInput.isBlank() || transactionRefInput.isBlank()) {
                        Toast.makeText(context, "يرجى كتابة اسمك ورقم المعاملة", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmitPayment(
                            userNameInput,
                            userContactInput,
                            selectedPlan,
                            selectedMethod,
                            transactionRefInput,
                            notesInput
                        )
                        paymentSubmittedSuccess = true
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_subscription_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إرسال طلب التفعيل إلى المطور",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email direct link to Ahmed Becetti
            OutlinedButton(
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ahmedbecetti41@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "تأكيد دفع اشتراك LinguaQuest: ${selectedPlan.titleAr}")
                        putExtra(Intent.EXTRA_TEXT, "الاسم: $userNameInput\nرقم الهاتف/الإيميل: $userContactInput\nالباقة: ${selectedPlan.titleAr}\nطريقة الدفع: ${selectedMethod.title}\nرقم المعاملة: $transactionRefInput")
                    }
                    try {
                        context.startActivity(Intent.createChooser(emailIntent, "إرسال وصل الدفع"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "تم إرسال الطلب داخلياً إلى المسؤول", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إرسال نسخة من الوصل بالبريد (ahmedbecetti41@gmail.com)")
            }
        }
    }
}
