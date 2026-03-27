package com.example.record.analytics.util

import java.util.UUID

object IdGenerator {
    fun newRecordId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}