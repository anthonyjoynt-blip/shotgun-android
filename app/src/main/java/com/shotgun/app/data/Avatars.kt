package com.shotgun.app.data

/** The avatars a rider can pick from. Emoji, so nothing to bundle and they render on any phone. */
object Avatars {
    val all = listOf(
        "🐶", // dog
        "🐱", // cat
        "🦊", // fox
        "🐻", // bear
        "🐼", // panda
        "🐨", // koala
        "🐯", // tiger
        "🦁", // lion
        "🐮", // cow
        "🐷", // pig
        "🐸", // frog
        "🐵", // monkey
        "🐧", // penguin
        "🐢", // turtle
        "🦄", // unicorn
        "🐙", // octopus
        "🦖", // t-rex
        "🐝", // bee
        "🦋", // butterfly
        "🐬"  // dolphin
    )

    /** For riders saved before avatars existed. */
    const val DEFAULT = "🙂" // slightly smiling face
}
