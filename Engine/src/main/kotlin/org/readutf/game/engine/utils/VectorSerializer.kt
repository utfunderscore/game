package org.readutf.game.engine.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.node.DoubleNode
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec

class VectorSerializer : JsonSerializer<Point>() {
    override fun serialize(
        point: Point?,
        jsonGenerator: JsonGenerator?,
        serializerProvider: SerializerProvider?,
    ) {
        if (point == null || jsonGenerator == null) return

        jsonGenerator.writeStartObject()
        jsonGenerator.writeNumberField("x", point.x())
        jsonGenerator.writeNumberField("y", point.y())
        jsonGenerator.writeNumberField("z", point.z())
        jsonGenerator.writeEndObject()
    }
}

class VectorDeserializer : JsonDeserializer<Point>() {
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): Point {
        val node: TreeNode = jsonParser?.codec?.readTree(jsonParser) ?: return Vec.ZERO
        val x = (node.get("x") as DoubleNode).asDouble()
        val y = (node.get("y") as DoubleNode).asDouble()
        val z = (node.get("z") as DoubleNode).asDouble()
        return Vec(x, y, z)
    }
}
