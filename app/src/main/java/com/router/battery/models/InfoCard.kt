package com.router.battery.models

import androidx.annotation.DrawableRes

data class InfoCard(
    val title: String,
    val description: String,
    @DrawableRes val imageResourceId: Int
    )

