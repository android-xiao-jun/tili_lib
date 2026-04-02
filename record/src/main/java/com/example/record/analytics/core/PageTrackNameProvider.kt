package com.example.record.analytics.core

interface PageTrackNameProvider {
    fun pageTrackName(): String
}

interface PageTrackExtraProvider {
    fun pageTrackExtra(): String?
}