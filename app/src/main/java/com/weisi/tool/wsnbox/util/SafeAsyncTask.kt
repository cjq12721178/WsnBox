package com.weisi.tool.wsnbox.util

import android.os.AsyncTask
import java.lang.ref.WeakReference

abstract class SafeAsyncTask<Params, Progress, Result>(achiever: SafeAsyncTask.ResultAchiever<Result, Progress>) : AsyncTask<Params, Progress, Result>() {

    private val weakAchiever = WeakReference(achiever)

    override fun onPostExecute(result: Result?) {
        val achiever = weakAchiever.get() ?: return
        if (achiever.invalid()) {
            return
        }
        achiever.onResultAchieved(result)
    }

    override fun onProgressUpdate(vararg values: Progress) {
        val achiever = weakAchiever.get() ?: return
        if (achiever.invalid()) {
            return
        }
        achiever.onProgressUpdate(values)
    }

    interface ResultAchiever<Result, Progress> : AchieverChecker {
        fun onResultAchieved(result: Result?)
        fun onProgressUpdate(values: Array<out Progress>) {}
    }

    interface AchieverChecker {
        fun invalid(): Boolean
    }
}