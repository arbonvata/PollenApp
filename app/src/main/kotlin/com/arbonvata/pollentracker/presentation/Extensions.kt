package com.arbonvata.pollentracker.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.vector.ImageVector

val allergenIcons: Map<String, ImageVector> =
    mapOf(
        "hassel" to Icons.Default.Spa,
        "al" to Icons.Default.Park,
        "tall" to Icons.Default.Park,
        "sälg och viden" to Icons.Default.Grass,
        "alm" to Icons.Default.Park,
        "björk" to Icons.Default.Spa,
        "bok" to Icons.Default.Park,
        "ek" to Icons.Default.Park,
        "gran" to Icons.Default.Park,
        "gräs" to Icons.Default.Grass,
        "gråbo" to Icons.Default.Spa,
        "malörtsambrosia" to Icons.Default.Spa,
    )

fun getAllergenIcon(allergenName: String): ImageVector = allergenIcons[allergenName.trim().lowercase()] ?: Icons.Default.Park
