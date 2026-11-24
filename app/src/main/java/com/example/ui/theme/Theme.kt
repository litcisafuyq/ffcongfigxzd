package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = EmberPrimary,
    onPrimary = EmberOnPrimary,
    primaryContainer = EmberPrimaryContainer,
    onPrimaryContainer = EmberOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = MintTertiary,
    onTertiary = MintOnTertiary,
    tertiaryContainer = MintTertiaryContainer,
    onTertiaryContainer = MintOnTertiaryContainer,
    error = AlertError,
    onError = AlertOnError,
    errorContainer = AlertErrorContainer,
    onErrorContainer = AlertOnErrorContainer,
    background = TacticalBackground,
    onBackground = TacticalOnSurface,
    surface = TacticalSurface,
    onSurface = TacticalOnSurface,
    surfaceVariant = TacticalSurfaceVariant,
    onSurfaceVariant = TacticalOnSurfaceVariant,
    surfaceContainerHigh = TacticalSurfaceContainerHigh,
    outline = TacticalOutline,
    outlineVariant = TacticalOutlineVariant,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightEmberPrimary,
    onPrimary = EmberOnPrimary,
    primaryContainer = EmberOnPrimaryContainer,
    onPrimaryContainer = EmberPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    tertiary = MintTertiary,
    onTertiary = MintOnTertiary,
    error = AlertError,
    background = LightTacticalBackground,
    onBackground = LightTacticalOnSurface,
    surface = LightTacticalSurface,
    onSurface = LightTacticalOnSurface,
    surfaceVariant = LightTacticalSurfaceVariant,
    onSurfaceVariant = LightTacticalOnSurfaceVariant,
    outline = LightTacticalOutline,
  )

val TacticalShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
  )

@Composable
fun FfvnConfigTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = TacticalShapes,
    content = content,
  )
}
