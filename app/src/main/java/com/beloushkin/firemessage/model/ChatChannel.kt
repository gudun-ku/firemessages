package com.beloushkin.firemessage.model

import java.lang.reflect.Constructor

data class ChatChannel(val userIds: MutableList<String>) {
    constructor(): this(mutableListOf())
}