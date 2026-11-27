package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.pluralStringResource

/**
 * [pluralStringResource] with English plural rules: "1 episode", "161 episodes".
 *
 * Compose Resources picks the plural form by the device language even when the text is English, so on a
 * Ukrainian device "161 episode" showed (Ukrainian "one" also covers 21, 31, 161…). The app's strings are
 * English only, so the English rule is the right one: one for 1, other for everything else.
 *
 * How: English `<plurals>` define only `one` and `other`, and Compose Resources falls back to `other` for a form
 * the resource lacks. [count] goes into the text through [formatArgs]; the quantity used to pick the form is
 * [englishPluralQuantity]: 1 is "one" in every language, 6 is "one" in none (CLDR plural rules).
 *
 * Once the strings are translated, switch back to [pluralStringResource] so each language uses its own rules.
 */
@Composable
fun englishPluralStringResource(resource: PluralStringResource, count: Int, vararg formatArgs: Any): String =
    pluralStringResource(resource, englishPluralQuantity(count), *formatArgs)

/** A quantity that selects English "one" for [count] == 1 and "other" otherwise, whatever the device language. */
internal fun englishPluralQuantity(count: Int): Int = if (count == 1) 1 else NEVER_ONE

private const val NEVER_ONE = 6
