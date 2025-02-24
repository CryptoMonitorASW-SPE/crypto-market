package it.unibo.domain.ports

import it.unibo.domain.EventPayload

/**
 * Interface for dispatching events within the application.
 *
 * This interface provides methods for publishing event payloads
 * to the designated event handling system, and for closing the dispatcher
 * to release any underlying resources.
 */
interface EventDispatcher {
    /**
     * Publishes the given event payload.
     *
     * @param data the [EventPayload] to be published.
     */
    fun publish(data: EventPayload)

    /**
     * Closes the event dispatcher, releasing any resources held.
     *
     * This should be called when the dispatcher is no longer needed
     * to prevent resource leaks.
     */
    fun close()
}
