package dev.abstrate.kotlin


inline fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> =
    mapTo(LinkedHashSet(collectionSizeOrDefault(10)), transform)

inline fun <T, R : Any> Iterable<T>.mapNotNullToSet(transform: (T) -> R?): Set<R> =
    mapNotNullTo(LinkedHashSet(), transform)

inline fun <T, R> Iterable<T>.flatMapToSet(transform: (T) -> Iterable<R>): Set<R> =
    flatMapTo(LinkedHashSet(), transform)


inline fun <K, V, R> Map<out K, V>.mapToSet(transform: (Map.Entry<K, V>) -> R): Set<R> {
    return mapTo(LinkedHashSet(size), transform)
}

inline fun <K, V, R : Any> Map<out K, V>.mapNotNullToSet(transform: (Map.Entry<K, V>) -> R?): Set<R> {
    return mapNotNullTo(LinkedHashSet(), transform)
}

inline fun <K, V, R> Map<out K, V>.flatMapToSet(transform: (Map.Entry<K, V>) -> Iterable<R>): Set<R> {
    return flatMapTo(LinkedHashSet(), transform)
}


@PublishedApi
internal fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int = if (this is Collection<*>) this.size else default
