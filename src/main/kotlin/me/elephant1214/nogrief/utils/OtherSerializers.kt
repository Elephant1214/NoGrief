package me.elephant1214.nogrief.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
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

object ClaimPermsSerializer : KSerializer<EnumSet<ClaimPermission>> {
    override val descriptor = serialDescriptor<Set<ClaimPermission>>()

    override fun serialize(encoder: Encoder, value: EnumSet<ClaimPermission>) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(descriptor, 0, SetSerializer(ClaimPermission.serializer()), value)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): EnumSet<ClaimPermission> {
        val set = decoder.decodeSerializableValue(SetSerializer(ClaimPermission.serializer()))
        return EnumSet.copyOf(set)
    }
}
