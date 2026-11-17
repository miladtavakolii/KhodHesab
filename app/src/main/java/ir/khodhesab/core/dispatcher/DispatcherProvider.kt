package ir.khodhesab.core.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Interface providing coroutine dispatchers for clean architecture and unit testing.
 *
 * Injection of this interface allows unit tests to swap dispatchers with [kotlinx.coroutines.test.TestDispatcher].
 */
interface DispatcherProvider {
    /**
     * Dispatcher for main thread UI operations.
     */
    val main: CoroutineDispatcher

    /**
     * Dispatcher for IO bound operations such as database reads/writes and file IO.
     */
    val io: CoroutineDispatcher

    /**
     * Dispatcher for CPU-intensive background tasks.
     */
    val default: CoroutineDispatcher

    /**
     * Unconfined dispatcher for immediate execution.
     */
    val unconfined: CoroutineDispatcher
}

/**
 * Default implementation of [DispatcherProvider] using standard [Dispatchers].
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
