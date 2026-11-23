package com.mercan.app.platform

import android.app.job.JobParameters
import android.app.job.JobService
import com.mercan.app.di.AppScope
import com.mercan.app.domain.MenuRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RefreshJobService : JobService() {
    @Inject lateinit var menus: MenuRepository

    @Inject @AppScope lateinit var scope: CoroutineScope

    private val running = mutableMapOf<Int, Job>()

    override fun onStartJob(params: JobParameters): Boolean {
        running[params.jobId] = scope.launch {
            val ok = menus.refreshCurrent()
            jobFinished(params, !ok)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        running.remove(params.jobId)?.cancel()
        return true
    }
}
