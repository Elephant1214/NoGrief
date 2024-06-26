package me.elephant1214.nogrief.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.elephant1214.nogrief.claims.Claim
import me.elephant1214.nogrief.claims.ClaimChunk
import me.elephant1214.nogrief.claims.ClaimColor
import me.elephant1214.nogrief.claims.permissions.ClaimPermission
import net.kyori.adventure.text.Component
import org.bukkit.World
import java.time.Instant
import java.util.*

object ClaimSerializer : KSerializer<Claim> {
    private const val ID_INDEX = 0
    private const val NAME_INDEX = 1
    private const val COLOR_INDEX = 2
    private const val OWNER_INDEX = 3
    private const val WORLD_INDEX = 4
    private const val CHUNKS_INDEX = 5
    private const val DEFAULT_PERMS_INDEX = 6
    private const val PERMS_INDEX = 7
    private const val MODIFIED_INDEX = 8

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("claim") {
        element<@Serializable(UUIDSerializer::class) UUID>("id")
        element<Component>("name")
        element<ClaimColor>("color")
        element<@Serializable(UUIDSerializer::class) UUID>("owner")
        element<World>("world")
        element<Set<Long>>("chunks")
        element<@Serializable(ClaimPermEnumSetSerializer::class) EnumSet<ClaimPermission>>("defaultPermissions")
        element<Map<@Serializable(UUIDSerializer::class) UUID, @Serializable(ClaimPermEnumSetSerializer::class) EnumSet<ClaimPermission>>>(
            "permissions"
        )
        element<@Serializable(InstantSerializer::class) Instant>("modified")
    }

    override fun serialize(encoder: Encoder, value: Claim) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeUUID(ID_INDEX, value.claimId)
        composite.encodeSerializableElement(descriptor, NAME_INDEX, ComponentSerializer, value.name)
        composite.encodeSerializableElement(descriptor, COLOR_INDEX, ClaimColor.serializer(), value.color)
        composite.encodeUUID(OWNER_INDEX, value.owner)
        composite.encodeSerializableElement(descriptor, WORLD_INDEX, WorldSerializer, value.world)
        composite.encodeSerializableElement(
            descriptor, CHUNKS_INDEX, SetSerializer(Long.serializer()), value.getChunksForSerial()
        )
        composite.encodeSerializableElement(
            descriptor,
            DEFAULT_PERMS_INDEX,
            ClaimPermEnumSetSerializer,
            value.defaultPermissions
        )
        composite.encodeSerializableElement(
            descriptor,
            PERMS_INDEX,
            MapSerializer(UUIDSerializer, SetSerializer(ClaimPermission.serializer())),
            value.getPermsForSerial()
        )
        composite.encodeSerializableElement(descriptor, MODIFIED_INDEX, InstantSerializer, value.modified)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Claim {
        lateinit var claimId: UUID
        lateinit var name: Component
        lateinit var color: ClaimColor
        lateinit var owner: UUID
        lateinit var world: World
        lateinit var chunks: Set<Long>
        lateinit var defaultPermissions: EnumSet<ClaimPermission>
        lateinit var permissions: Map<UUID, EnumSet<ClaimPermission>>
        lateinit var modified: Instant

        val composite = decoder.beginStructure(descriptor)
        loop@ while (true) {
            when (val index = composite.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                ID_INDEX -> claimId = composite.decodeUUID(index)
                NAME_INDEX -> name = composite.decodeSerializableElement(descriptor, NAME_INDEX, ComponentSerializer)
                COLOR_INDEX -> color =
                    composite.decodeSerializableElement(descriptor, COLOR_INDEX, ClaimColor.serializer())

                OWNER_INDEX -> owner = composite.decodeUUID(index)
                WORLD_INDEX -> world = composite.decodeSerializableElement(descriptor, index, WorldSerializer)
                CHUNKS_INDEX -> chunks =
                    composite.decodeSerializableElement(descriptor, index, SetSerializer(Long.serializer()))

                DEFAULT_PERMS_INDEX -> defaultPermissions =
                    composite.decodeSerializableElement(descriptor, index, ClaimPermEnumSetSerializer)

                PERMS_INDEX -> permissions = composite.decodeSerializableElement(
                    descriptor, index, MapSerializer(UUIDSerializer, ClaimPermEnumSetSerializer)
                )

                MODIFIED_INDEX -> modified = composite.decodeSerializableElement(descriptor, index, InstantSerializer)
                else -> throw SerializationException("Unknown index $index")
            }
        }
        composite.endStructure(descriptor)

        return Claim(
            claimId = claimId,
            name = name,
            color = color,
            ownerIn = owner,
            world = world,
            _chunks = ClaimChunk.fromLongSet(world, chunks),
            defaultPermissions = defaultPermissions,
            _permissions = permissions.toMutableMap(),
            modifiedIn = modified
        )
    }

    private fun CompositeEncoder.encodeUUID(index: Int, value: UUID) =
        encodeSerializableElement(descriptor, index, UUIDSerializer, value)

    private fun CompositeDecoder.decodeUUID(index: Int): UUID =
        decodeSerializableElement(descriptor, index, UUIDSerializer)
}
