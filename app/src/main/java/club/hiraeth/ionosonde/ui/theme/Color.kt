package club.hiraeth.ionosonde.ui.theme

import androidx.compose.ui.graphics.Color

// App brand colors
val DeepNavy = Color(0xFF0D1B2A)
val NavyLight = Color(0xFF1B2838)
val Amber = Color(0xFFFFB347)

// Condition indicator colors
val ConditionGreen = Color(0xFF4CAF50)
val ConditionAmber = Color(0xFFFFA726)
val ConditionRed = Color(0xFFEF5350)

// Material 3 theme colors
val PrimaryLight = Color(0xFF1565C0)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD1E4FF)
val OnPrimaryContainerLight = Color(0xFF001D36)
val SecondaryLight = Color(0xFF535F70)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFDFBFF)
val OnSurfaceLight = Color(0xFF1A1C1E)
val SurfaceVariantLight = Color(0xFFDFE2EB)

val PrimaryDark = Color(0xFF9ECAFF)
val OnPrimaryDark = Color(0xFF003258)
val PrimaryContainerDark = Color(0xFF00497D)
val OnPrimaryContainerDark = Color(0xFFD1E4FF)
val SecondaryDark = Color(0xFFBBC7DB)
val OnSecondaryDark = Color(0xFF253140)
val SurfaceDark = Color(0xFF1A1C1E)
val OnSurfaceDark = Color(0xFFE2E2E6)
val SurfaceVariantDark = Color(0xFF43474E)

fun conditionColor(status: String): Color {
    val s = status.lowercase().trim()
    return when {
        s == "good" || s == "band open" -> ConditionGreen
        s == "fair" || s == "moderate" -> ConditionAmber
        s == "poor" || s == "closed" || s == "band closed" -> ConditionRed
        else -> ConditionAmber
    }
}

fun kIndexColor(k: Int): Color = when {
    k <= 2 -> ConditionGreen
    k <= 4 -> ConditionAmber
    else -> ConditionRed
}

fun kIndexColor(k: Double): Color = kIndexColor(k.toInt())

fun sfiColor(sfi: Int): Color = when {
    sfi >= 100 -> ConditionGreen
    else -> ConditionAmber
}

fun xRayColor(xRayClass: String): Color {
    val c = xRayClass.uppercase().firstOrNull() ?: return ConditionGreen
    return when (c) {
        'A', 'B' -> ConditionGreen
        'C' -> ConditionAmber
        'M', 'X' -> ConditionRed
        else -> ConditionGreen
    }
}
