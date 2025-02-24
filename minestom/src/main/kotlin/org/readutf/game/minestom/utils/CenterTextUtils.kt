package org.readutf.game.minestom.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun Component.center(): Component = CenterTextUtils.sendCenteredMessage(this)

object CenterTextUtils {
    private const val CENTER_PX = 154

    val serializer = LegacyComponentSerializer.legacySection()

    fun sendCenteredMessage(component: Component): Component {
        val formattedMessage = serializer.serialize(component)

        if (formattedMessage.isEmpty()) {
            return "".toComponent()
        }

        var messagePxSize = 0
        var previousCode = false
        var isBold = false

        for (c in formattedMessage.toCharArray()) {
            if (c == '§') {
                previousCode = true
                continue
            } else if (previousCode) {
                previousCode = false
                isBold = c == 'l' || c == 'L'
                continue
            } else {
                messagePxSize += if (isBold) DefaultFontInfo.getBoldLength(c) else DefaultFontInfo.getLength(c)
                messagePxSize++
            }
        }

        val halvedMessageSize = messagePxSize / 2
        val toCompensate = CENTER_PX - halvedMessageSize
        val spaceLength = 4
        var compensated = 0
        val sb = StringBuilder()
        while (compensated < toCompensate) {
            sb.append(" ")
            compensated += spaceLength
        }
        return (sb.toString() + formattedMessage).toComponent()
    }

    object DefaultFontInfo {

        private val charLengths = IntArray(256) { 4 } // ASCII range, default length 4

        init {
            // Set specific character lengths
            charLengths['A'.code] = 5
            charLengths['a'.code] = 5
            charLengths['B'.code] = 5
            charLengths['b'.code] = 5
            charLengths['C'.code] = 5
            charLengths['c'.code] = 5
            charLengths['D'.code] = 5
            charLengths['d'.code] = 5
            charLengths['E'.code] = 5
            charLengths['e'.code] = 5
            charLengths['F'.code] = 4
            charLengths['f'.code] = 4
            charLengths['G'.code] = 5
            charLengths['g'.code] = 5
            charLengths['H'.code] = 5
            charLengths['h'.code] = 5
            charLengths['I'.code] = 3
            charLengths['i'.code] = 1
            charLengths['J'.code] = 5
            charLengths['j'.code] = 5
            charLengths['K'.code] = 5
            charLengths['k'.code] = 4
            charLengths['L'.code] = 5
            charLengths['l'.code] = 1
            charLengths['M'.code] = 5
            charLengths['m'.code] = 5
            charLengths['N'.code] = 5
            charLengths['n'.code] = 5
            charLengths['O'.code] = 5
            charLengths['o'.code] = 5
            charLengths['P'.code] = 5
            charLengths['p'.code] = 5
            charLengths['Q'.code] = 5
            charLengths['q'.code] = 5
            charLengths['R'.code] = 5
            charLengths['r'.code] = 5
            charLengths['S'.code] = 5
            charLengths['s'.code] = 5
            charLengths['T'.code] = 5
            charLengths['t'.code] = 4
            charLengths['U'.code] = 5
            charLengths['u'.code] = 5
            charLengths['V'.code] = 5
            charLengths['v'.code] = 5
            charLengths['W'.code] = 5
            charLengths['w'.code] = 5
            charLengths['X'.code] = 5
            charLengths['x'.code] = 5
            charLengths['Y'.code] = 5
            charLengths['y'.code] = 5
            charLengths['Z'.code] = 5
            charLengths['z'.code] = 5
            charLengths['1'.code] = 5
            charLengths['2'.code] = 5
            charLengths['3'.code] = 5
            charLengths['4'.code] = 5
            charLengths['5'.code] = 5
            charLengths['6'.code] = 5
            charLengths['7'.code] = 5
            charLengths['8'.code] = 5
            charLengths['9'.code] = 5
            charLengths['0'.code] = 5
            charLengths['!'.code] = 1
            charLengths['@'.code] = 6
            charLengths['#'.code] = 5
            charLengths['$'.code] = 5
            charLengths['%'.code] = 5
            charLengths['^'.code] = 5
            charLengths['&'.code] = 5
            charLengths['*'.code] = 5
            charLengths['('.code] = 4
            charLengths[')'.code] = 4
            charLengths['-'.code] = 5
            charLengths['_'.code] = 5
            charLengths['+'.code] = 5
            charLengths['='.code] = 5
            charLengths['{'.code] = 4
            charLengths['}'.code] = 4
            charLengths['['.code] = 3
            charLengths[']'.code] = 3
            charLengths[':'.code] = 1
            charLengths[';'.code] = 1
            charLengths['"'.code] = 3
            charLengths['\''.code] = 1
            charLengths['<'.code] = 4
            charLengths['>'.code] = 4
            charLengths['?'.code] = 5
            charLengths['/'.code] = 5
            charLengths['\\'.code] = 5
            charLengths['|'.code] = 1
            charLengths['~'.code] = 5
            charLengths['`'.code] = 2
            charLengths['.'.code] = 1
            charLengths[','.code] = 1
            charLengths[' '.code] = 3
        }

        fun getLength(c: Char): Int = charLengths[c.code and 0xFF]

        fun getBoldLength(c: Char): Int {
            val length = getLength(c)
            return if (c == ' ') length else length + 1
        }
    }
}
