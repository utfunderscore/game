package org.readutf.game.minestom.utils

/**
 * A utility object for formatting time given in seconds into a human-readable format,
 * displaying hours, minutes, and seconds, but only including each unit if necessary.
 * For example, 3665 seconds would be formatted as "1h 1m 5s", while 65 seconds would be formatted as "1m 5s".
 */
object TimeFormatter {
    /**
     * Converts the given time in seconds to a formatted string that includes hours, minutes, and seconds.
     * The resulting string will only include the units that are necessary.
     * For example:
     * - 3665 seconds will return "1h 1m 5s"
     * - 3600 seconds will return "1h"
     * - 65 seconds will return "1m 5s"
     * - 5 seconds will return "5s"
     * - 0 seconds will return an empty string
     *
     * @param totalSeconds The total time in seconds to be formatted.
     * @return A formatted string representing the time in hours, minutes, and seconds (e.g., "1h 2m 3s").
     */
    fun formatTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val remainingMinutes = (totalSeconds % 3600) / 60
        val remainingSeconds = totalSeconds % 60

        val result = StringBuilder()

        if (hours > 0) {
            result.append(hours).append("h ")
        }
        if (remainingMinutes > 0 || hours > 0) { // Only include minutes if there were hours
            result.append(remainingMinutes).append("m ")
        }
        if (remainingSeconds > 0 || hours > 0 || remainingMinutes > 0) { // Always include seconds if needed
            result.append(remainingSeconds).append("s")
        }

        return result.toString().trim() // Remove trailing space
    }
}
