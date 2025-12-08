@file:Suppress("UnstableApiUsage")

package me.kub94ek.crimson.command

import com.mojang.brigadier.suggestion.SuggestionProvider
import io.papermc.paper.command.brigadier.CommandSourceStack
import me.kub94ek.kubLib.KubLib



fun customItemsSuggestion(namespace: String = "crimson"): SuggestionProvider<CommandSourceStack> {
    return SuggestionProvider { _, builder ->
        val items: List<String> = KubLib.getInstance().itemRegistry.getItemIds(namespace)
        items.stream()
            .filter {
                it.lowercase().startsWith(builder.remainingLowerCase)
                    || it.substring(it.indexOf(':')+1, it.length).startsWith(builder.remainingLowerCase)
            }
            .forEach(builder::suggest)
        builder.buildFuture()
    }
}

/*fun customFragmentTypesSuggestion(): SuggestionProvider<CommandSourceStack> {
    return SuggestionProvider { _, builder ->
        val types: List<String> = FragmentTypes.fragmentTypes.keys.toList()
        types.stream()
            .filter {
                it.lowercase().startsWith(builder.remainingLowerCase)
            }
            .forEach(builder::suggest)
        builder.buildFuture()
    }
}*/