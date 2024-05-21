package me.elephant1214.nogrief.utils

import kotlinx.serialization.SerializationException

class WorldNotFoundException(override val message: String) : SerializationException()