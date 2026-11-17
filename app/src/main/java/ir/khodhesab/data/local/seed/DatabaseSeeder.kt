package ir.khodhesab.data.local.seed

import ir.khodhesab.data.local.entity.BankEntity
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.model.TransactionType

/**
 * Pre-populates the database with initial Iranian banks and default transaction categories.
 */
object DatabaseSeeder {

    /**
     * Initial list of major Iranian banks with shortcodes, hex colors, and icon keys.
     */
    val DEFAULT_BANKS = listOf(
        BankEntity(code = "MELLI", persianName = "بانک ملی ایران", senderNumbers = listOf("9611", "+989611", "BANK MELLI"), colorHex = "#1E88E5", iconKey = "ic_bank_melli"),
        BankEntity(code = "MELLAT", persianName = "بانک ملت", senderNumbers = listOf("200033", "20004000", "BANK MELLAT"), colorHex = "#E53935", iconKey = "ic_bank_mellat"),
        BankEntity(code = "SADERAT", persianName = "بانک صادرات ایران", senderNumbers = listOf("200000", "BANK SADERAT"), colorHex = "#1565C0", iconKey = "ic_bank_saderat"),
        BankEntity(code = "TEJARAT", persianName = "بانک تجارت", senderNumbers = listOf("200070", "BANK TEJARAT"), colorHex = "#00ACC1", iconKey = "ic_bank_tejarat"),
        BankEntity(code = "SEPAH", persianName = "بانک سپه", senderNumbers = listOf("200090", "BANK SEPAH"), colorHex = "#F57C00", iconKey = "ic_bank_sepah"),
        BankEntity(code = "PASARGAD", persianName = "بانک پاسارگاد", senderNumbers = listOf("10009000", "PASARGAD"), colorHex = "#FBC02D", iconKey = "ic_bank_pasargad"),
        BankEntity(code = "SAMAN", persianName = "بانک سامان", senderNumbers = listOf("20008000", "SAMAN"), colorHex = "#8E24AA", iconKey = "ic_bank_saman"),
        BankEntity(code = "PARSIAN", persianName = "بانک پارسیان", senderNumbers = listOf("300088", "PARSIAN"), colorHex = "#43A047", iconKey = "ic_bank_parsian"),
        BankEntity(code = "KESHAVARZI", persianName = "بانک کشاورزی", senderNumbers = listOf("200091", "KESHAVARZI"), colorHex = "#2E7D32", iconKey = "ic_bank_keshavarzi"),
        BankEntity(code = "EGHTESAD_NOVIN", persianName = "بانک اقتصاد نوین", senderNumbers = listOf("20002000", "EN BANK"), colorHex = "#D81B60", iconKey = "ic_bank_en"),
        BankEntity(code = "SHAHR", persianName = "بانک شهر", senderNumbers = listOf("20003000", "CITY BANK"), colorHex = "#E64A19", iconKey = "ic_bank_shahr"),
        BankEntity(code = "DAY", persianName = "بانک دی", senderNumbers = listOf("200045", "DAY BANK"), colorHex = "#5E35B1", iconKey = "ic_bank_day"),
        BankEntity(code = "SINA", persianName = "بانک سینا", senderNumbers = listOf("200040", "SINA BANK"), colorHex = "#3949AB", iconKey = "ic_bank_sina"),
        BankEntity(code = "REFAH", persianName = "بانک رفاه کارگران", senderNumbers = listOf("300044", "REFAH"), colorHex = "#00897B", iconKey = "ic_bank_refah"),
        BankEntity(code = "MASKAN", persianName = "بانک مسکن", senderNumbers = listOf("300010", "MASKAN"), colorHex = "#FB8C00", iconKey = "ic_bank_maskan"),
        BankEntity(code = "BLU", persianName = "بلوبانک", senderNumbers = listOf("10008000", "BLU BANK", "blubank"), colorHex = "#0052CC", iconKey = "ic_bank_blu"),
        BankEntity(code = "RESALAT", persianName = "بانک قرض‌الحسنه رسالت", senderNumbers = listOf("20004747", "RESALAT"), colorHex = "#00796B", iconKey = "ic_bank_resalat"),
        BankEntity(code = "MEHR_IRAN", persianName = "بانک قرض‌الحسنه مهر ایران", senderNumbers = listOf("30008585", "MEHR BANK"), colorHex = "#388E3C", iconKey = "ic_bank_mehr")
    )

    /**
     * Initial list of default expense and income categories.
     */
    val DEFAULT_CATEGORIES = listOf(
        // Withdrawal / Expense categories
        CategoryEntity(name = "خوراک و رستوران", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_food", colorHex = "#FF5722", isDefault = true, keywords = listOf("رستوران", "سوپرمارکت", "هایپر", "فروشگاه", "نانوایی", "کافه", "فست فود")),
        CategoryEntity(name = "حمل‌ونقل و سوخت", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_transport", colorHex = "#FF9800", isDefault = true, keywords = listOf("اسنپ", "تپسی", "بنزین", "پمپ بنزین", "مترو", "تاکسی", "پارکینگ")),
        CategoryEntity(name = "خرید و پوشاک", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_shopping", colorHex = "#E91E63", isDefault = true, keywords = listOf("پوشاک", "لباس", "کفش", "کیف", "فروشگاه", "پاساژ")),
        CategoryEntity(name = "قبوض و شارژ", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_bills", colorHex = "#9C27B0", isDefault = true, keywords = listOf("قبض", "برق", "گاز", "آب", "تلفن", "همراه اول", "ایرانسل", "رایتل", "شارژ", "اینترنت")),
        CategoryEntity(name = "مسکن و اجاره", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_housing", colorHex = "#795548", isDefault = true, keywords = listOf("اجاره", "رهن", "شارژ ساختمان", "املاک")),
        CategoryEntity(name = "درمان و سلامت", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_health", colorHex = "#F44336", isDefault = true, keywords = listOf("داروخانه", "بیمارستان", "مطب", "دکتر", "کلینیک", "آزمایشگاه")),
        CategoryEntity(name = "تفریح و سفر", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_leisure", colorHex = "#00BCD4", isDefault = true, keywords = listOf("سینما", "هتل", "بلیط", "پرواز", "تور", "استخر", "ورزش")),
        CategoryEntity(name = "آموزش و کتاب", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_education", colorHex = "#3F51B5", isDefault = true, keywords = listOf("کتاب", "دانشگاه", "مدرسه", "آموزشگاه", "دوره")),
        CategoryEntity(name = "کارمزد و خدمات بانکی", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_bank_fee", colorHex = "#607D8B", isDefault = true, keywords = listOf("کارمزد", "پایا", "ساتنا", "شتاب")),
        CategoryEntity(name = "سایر هزینه‌ها", type = TransactionType.WITHDRAWAL, iconKey = "ic_cat_other_exp", colorHex = "#9E9E9E", isDefault = true, keywords = emptyList()),

        // Deposit / Income categories
        CategoryEntity(name = "حقوق و دستمزد", type = TransactionType.DEPOSIT, iconKey = "ic_cat_salary", colorHex = "#4CAF50", isDefault = true, keywords = listOf("حقوق", "دستمزد", "پاداش", "عیدی", "مزایا")),
        CategoryEntity(name = "کسب‌وکار و فروش", type = TransactionType.DEPOSIT, iconKey = "ic_cat_business", colorHex = "#2196F3", isDefault = true, keywords = listOf("فروش", "درآمد", "واریز مشتری")),
        CategoryEntity(name = "سود بانکی و سرمایه‌گذاری", type = TransactionType.DEPOSIT, iconKey = "ic_cat_investment", colorHex = "#009688", isDefault = true, keywords = listOf("سود", "سرمایه", "بورس", "صندوق")),
        CategoryEntity(name = "یارانه و هدیه", type = TransactionType.DEPOSIT, iconKey = "ic_cat_subsidy", colorHex = "#8BC34A", isDefault = true, keywords = listOf("یارانه", "معیشتی", "هدیه", "کادو")),
        CategoryEntity(name = "بازگشت وجه", type = TransactionType.DEPOSIT, iconKey = "ic_cat_refund", colorHex = "#CDDC39", isDefault = true, keywords = listOf("بازگشت", "استرداد", "اصلاحیه")),
        CategoryEntity(name = "سایر درآمدها", type = TransactionType.DEPOSIT, iconKey = "ic_cat_other_inc", colorHex = "#80CBC4", isDefault = true, keywords = emptyList())
    )
}
