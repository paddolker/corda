package net.corda.core.flows

import net.corda.core.utilities.loggerFor
import java.nio.file.Path
import java.util.*

interface FlowStackSnapshotFactory {
    private object Holder {
        val INSTANCE: FlowStackSnapshotFactory

        init {
            val serviceFactory = ServiceLoader.load(FlowStackSnapshotFactory::class.java).singleOrNull()
            INSTANCE = serviceFactory ?: FlowStackSnapshotDefaultFactory()
        }
    }

    companion object {
        val instance: FlowStackSnapshotFactory by lazy { Holder.INSTANCE }
    }

    /**
     * Returns flow stack data snapshot extracted from Quasar stack.
     * It is designed to be used in the debug mode of the flow execution.
     * Note. This logic is only available during tests and is not meant to be used during the production deployment.
     * Therefore the default implementation does nothing.
     */
    fun getFlowStackSnapshot(flowClass: Class<*>): FlowStackSnapshot?

    /** Stores flow stack snapshot as a json file. The stored shapshot is only partial and consists
     * only data (i.e. stack traces and local variables values) relevant to the flow. It does not
     * persist corda internal data (e.g. FlowStateMachine). Instead it uses [StackFrameDataToken] to indicate
     * the class of the element on the stack.
     * The flow stack snapshot is stored in a file located in
     * {baseDir}/flowStackSnapshots/YYYY-MM-DD/{flowId}/
     * where baseDir is the node running directory and flowId is the flow unique identifier generated by the platform.
     * Note. This logic is only available during tests and is not meant to be used during the production deployment.
     * Therefore the default implementation does nothing.
     */
    fun persistAsJsonFile(flowClass: Class<*>, baseDir: Path, flowId: String): Unit
}

private class FlowStackSnapshotDefaultFactory : FlowStackSnapshotFactory {
    val log = loggerFor<FlowStackSnapshotDefaultFactory>()

    override fun getFlowStackSnapshot(flowClass: Class<*>): FlowStackSnapshot? {
        log.warn("Flow stack snapshot are not supposed to be used in a production deployment")
        return null
    }

    override fun persistAsJsonFile(flowClass: Class<*>, baseDir: Path, flowId: String) {
        log.warn("Flow stack snapshot are not supposed to be used in a production deployment")
    }
}

/**
 * Main data object representing snapshot of the flow stack, extracted from the Quasar stack.
 */
data class FlowStackSnapshot constructor(
        val timestamp: Long = System.currentTimeMillis(),
        val flowClass: Class<*>? = null,
        val stackFrames: List<Frame> = listOf()
) {
    data class Frame(
            val stackTraceElement: StackTraceElement? = null, // This should be the call that *pushed* the frame of [objects]
            val stackObjects: List<Any?> = listOf()
    )
}

/**
 * Token class, used to indicate stack presence of the corda internal data. Since this data is of no use for
 * a CordApp developer, it is skipped from serialisation and its presence is only marked by this token.
 */
data class StackFrameDataToken(val className: String)