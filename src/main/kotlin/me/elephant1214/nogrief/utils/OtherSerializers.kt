package me.elephant1214.nogrief.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.elephant1214.nogrief.NoGrief
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.World
import java.time.Instant
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return try {
            UUID.fromString(decoder.decodeString())
        } catch (e: IllegalArgumentException) {
            throw SerializationException(e)
        }
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

object WorldSerializer : KSerializer<World> {
    override val descriptor = PrimitiveSerialDescriptor("world", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: World) {
        UUIDSerializer.serialize(encoder, value.uid)
    }

    override fun deserialize(decoder: Decoder): World {
        val uuid: UUID = UUIDSerializer.deserialize(decoder)
        val world = Bukkit.getWorld(uuid) ?: throw WorldNotFoundException("Could not find a world with UUID $uuid")
        return world
    }
}

object ClaimPermEnumSetSerializer : KSerializer<EnumSet<ClaimPermission>> {
    override val descriptor = buildClassSerialDescriptor("EnumSet") {
        element<String>("permissions")
    }

    override fun serialize(encoder: Encoder, value: EnumSet<ClaimPermission>) {
        val permissions = value.toList()
        encoder.encodeSerializableValue(ListSerializer(ClaimPermission.serializer()), permissions)
    }

    override fun deserialize(decoder: Decoder): EnumSet<ClaimPermission> {
        val permissions = decoder.decodeSerializableValue(ListSerializer(ClaimPermission.serializer()))
        return EnumSet.copyOf(permissions)
    }
}

object ComponentSerializer : KSerializer<Component> {
    override val descriptor = PrimitiveSerialDescriptor("component", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Component) {
        encoder.encodeString(NoGrief.MINI_MESSAGE.serialize(value))
    }

    override fun deserialize(decoder: Decoder): Component {
        val component = decoder.decodeSerializableValue(String.serializer())
        return NoGrief.MINI_MESSAGE.deserialize(component)
    }
}
