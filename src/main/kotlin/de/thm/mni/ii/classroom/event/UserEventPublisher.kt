package de.thm.mni.ii.classroom.event

import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import reactor.core.publisher.FluxSink
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Consumer

@Component
class UserEventPublisher(private val executor: Executor)
    : ApplicationListener<ClassroomEvent>, Consumer<FluxSink<ClassroomEvent>> {

    private val queue: BlockingQueue<ClassroomEvent> = LinkedBlockingQueue() // <3>

    override fun onApplicationEvent(event: ClassroomEvent) {
        queue.offer(event)
    }

    override fun accept(sink: FluxSink<ClassroomEvent>) {
        executor.execute {
            while (true) try {
                val event: ClassroomEvent = queue.take()
                sink.next(event)
            } catch (e: InterruptedException) {
                ReflectionUtils.rethrowRuntimeException(e)
            }
        }
    }
}