package org.readutf.game.engine.game.settings.location

data class PositionTypeData(
    val name: String = "",
    val startsWith: String = "",
    val endsWith: String = "",
) {
    companion object {
        fun convert(positionType: PositionType): PositionTypeData =
            PositionTypeData(
                name = positionType.name,
                startsWith = positionType.startsWith,
                endsWith = positionType.endsWith,
            )
    }
}
