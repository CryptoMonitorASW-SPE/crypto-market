package it.unibo.domain.ports

import it.unibo.domain.EventPayload

/**
 * Interface for dispatching events.
 */
interface EventDispatcher {
    /**
     * Publishes the given event payload.
     *
     * @param data The event payload to publish.
     */
    fun publish(data: EventPayload)

    /**
     * Closes the event dispatcher, releasing any resources.
     */
    fun close()
}
