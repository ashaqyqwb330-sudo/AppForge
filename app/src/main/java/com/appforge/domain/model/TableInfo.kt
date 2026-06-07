package com.appforge.domain.model

data class TableInfo(
    val name: String,
    val columns: List<ColumnInfo>,
    val rowCount: Int = 0
)

data class ColumnInfo(
    val name: String,
    val type: String, // TEXT, INTEGER, REAL, BLOB
    val isPrimaryKey: Boolean = false
)

data class RowData(
    val values: Map<String, String?>, // column name to value
    val rawData: Map<String, ByteArray?> = emptyMap() // for BLOBs
)
