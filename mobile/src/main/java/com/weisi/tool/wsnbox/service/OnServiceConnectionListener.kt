package com.weisi.tool.wsnbox.service

interface OnServiceConnectionListener {
    companion object {
        const val SERVICE_DISCONNECTED = 0
        const val SERVICE_CONNECTION_CREATED = 1
        const val SERVICE_CONNECTION_STARTED = 2
        const val SERVICE_CONNECTION_STOPPED = 3
        const val SERVICE_CONNECTION_DESTROYED = 4
    }
    fun onServiceConnectionCreate(service: DataPrepareService)
    fun onServiceConnectionStart(service: DataPrepareService)
    fun onServiceConnectionStop(service: DataPrepareService)
    fun onServiceConnectionDestroy(service: DataPrepareService)
}