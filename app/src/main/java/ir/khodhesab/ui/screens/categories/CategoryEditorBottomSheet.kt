package ir.khodhesab.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.Category
import ir.khodhesab.ui.util.CategoryIconMapper

/**
 * BottomSheet editor for creating new custom categories or editing default/custom category keywords and styling.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorBottomSheet(
    editingCategory: Category?,
    isCreating: Boolean,
    onSave: (name: String, iconKey: String, colorHex: String, keywords: List<String>) -> Unit,
    onDelete: (categoryId: Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var nameText by remember { mutableStateOf(editingCategory?.name ?: "") }
    var selectedIconKey by remember { mutableStateOf(editingCategory?.iconKey ?: "ic_cat_food") }
    var selectedColorHex by remember { mutableStateOf(editingCategory?.colorHex ?: "#FF5722") }

    val keywords = remember {
        mutableStateListOf<String>().apply {
            editingCategory?.keywords?.let { addAll(it) }
        }
    }

    var newKeywordInput by remember { mutableStateOf("") }
    val isDefaultCategory = editingCategory?.isDefault == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isCreating) "ایجاد دسته‌بندی جدید" else "ویرایش دسته‌بندی ${editingCategory?.name ?: ""}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameText,
                onValueChange = { if (!isDefaultCategory) nameText = it },
                label = { Text(if (isDefaultCategory) "نام دسته‌بندی (پیش‌فرض سیستم)" else "نام دسته‌بندی") },
                readOnly = isDefaultCategory,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "انتخاب آیکون",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CategoryIconMapper.AVAILABLE_ICONS, key = { it.key }) { item ->
                    val isSelected = selectedIconKey.equals(item.key, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedIconKey = item.key },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "انتخاب رنگ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CategoryIconMapper.SELECTABLE_COLORS) { hex ->
                    val color = CategoryIconMapper.parseHexColor(hex)
                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColorHex = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "کلیدواژه‌های دسته‌بندی خودکار",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = newKeywordInput,
                    onValueChange = { newKeywordInput = it },
                    placeholder = { Text("مثال: اسنپ، بنزین، هایپر...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val kw = newKeywordInput.trim()
                        if (kw.isNotBlank() && !keywords.contains(kw)) {
                            keywords.add(kw)
                            newKeywordInput = ""
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن کلیدواژه")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                keywords.forEach { kw ->
                    InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(kw) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "حذف",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { keywords.remove(kw) }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nameText.isNotBlank()) {
                        onSave(nameText.trim(), selectedIconKey, selectedColorHex, keywords.toList())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره دسته‌بندی")
            }

            if (editingCategory != null && !isDefaultCategory) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onDelete(editingCategory.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف دسته‌بندی", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CategoryEditorBottomSheetPreview() {
    ir.khodhesab.ui.theme.KhodHesabTheme {
        val editingCategory = Category(
            id = 1,
            name = "خوراکی و سوپرمارکت",
            iconKey = "ic_cat_food",
            colorHex = "#FF5722",
            kind = ir.khodhesab.domain.model.TransactionKind.WITHDRAWAL,
            keywords = listOf("اسنپ", "هایپر")
        )
        CategoryEditorBottomSheet(
            editingCategory = editingCategory,
            isCreating = false,
            onSave = { _, _, _, _ -> },
            onDelete = {},
            onDismiss = {}
        )
    }
}

