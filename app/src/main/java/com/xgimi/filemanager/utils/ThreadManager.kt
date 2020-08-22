package com.xgimi.filemanager.utils

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 13:56
 *    desc   :
 */
object ThreadManager {

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val CORE_POOL_SIZE = CPU_COUNT + 1
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    private val KEEP_ALIVE = 1L
    private val THREAD_NAME = "FileManager"

    private val factory: ThreadFactory = object : ThreadFactory {

        private val mCount = AtomicInteger(1)

        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, THREAD_NAME + " #" + mCount.getAndIncrement())
        }
    }

    private val sPoolWorkQueue: BlockingQueue<Runnable> = LinkedBlockingQueue(64)

    private val mThreadPool: Executor = ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAXIMUM_POOL_SIZE,
        KEEP_ALIVE,
        TimeUnit.SECONDS,
        sPoolWorkQueue,
        factory
    )

    fun execute(runnable: Runnable) {
        mThreadPool.execute(runnable)
    }
}
