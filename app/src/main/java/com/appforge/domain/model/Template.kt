package com.appforge.domain.model

enum class Template(val id: Int, val displayName: String, val description: String) {
    ELEGANT_GALLERY(1, "المعرض الأنيق", "بطاقات صورية كبيرة مع Carousel، مثالي لقواعد الصور"),
    TABLE_ORGANIZER(2, "المنظّم الجدولي", "جداول أنيقة وتبويبات، مثالي للبيانات الرقمية"),
    STORY_TELLER(3, "القصة المروية", "Timeline وقصص، مثالي للنصوص الطويلة"),
    INTERACTIVE_GRID(4, "الشبكة التفاعلية", "Grid ملون وبطاقات قابلة للتوسيع، مناسب لأي بيانات");

    companion object {
        fun fromId(id: Int) = entries.first { it.id == id }
        fun default() = INTERACTIVE_GRID
    }
}
