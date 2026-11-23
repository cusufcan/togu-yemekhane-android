package com.mercan.app.platform

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface BackgroundRefresh {
    /** Günde iki kez, internet varken menüyü tazeleyen iş. */
    fun ensureScheduled()

    /** İndirme başarısız oldu: internet gelince bir kez daha dene. */
    fun retryWhenOnline()

    fun cancelRetry()
}

class JobBackgroundRefresh @Inject constructor(
    @ApplicationContext private val context: Context,
) : BackgroundRefresh {
    private val jobs get() = context.getSystemService(JobScheduler::class.java)

    override fun ensureScheduled() {
        if (jobs.allPendingJobs.any { it.id == PERIODIC }) return
        jobs.schedule(
            job(PERIODIC)
                .setPeriodic(TimeUnit.HOURS.toMillis(12))
                .setPersisted(true)
                .build(),
        )
    }

    override fun retryWhenOnline() {
        jobs.schedule(job(RETRY).setMinimumLatency(TimeUnit.SECONDS.toMillis(30)).build())
    }

    override fun cancelRetry() = jobs.cancel(RETRY)

    private fun job(id: Int) = JobInfo.Builder(id, ComponentName(context, RefreshJobService::class.java))
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

    private companion object {
        const val PERIODIC = 1
        const val RETRY = 2
    }
}
