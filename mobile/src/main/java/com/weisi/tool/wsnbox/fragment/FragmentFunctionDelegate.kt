package com.weisi.tool.wsnbox.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import com.weisi.tool.wsnbox.activity.BaseActivity
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.service.OnServiceConnectionListener.Companion.SERVICE_CONNECTION_CREATED
import com.weisi.tool.wsnbox.service.OnServiceConnectionListener.Companion.SERVICE_CONNECTION_DESTROYED
import com.weisi.tool.wsnbox.service.OnServiceConnectionListener.Companion.SERVICE_CONNECTION_STARTED
import com.weisi.tool.wsnbox.service.OnServiceConnectionListener.Companion.SERVICE_CONNECTION_STOPPED
import com.weisi.tool.wsnbox.service.OnServiceConnectionListener.Companion.SERVICE_DISCONNECTED

class FragmentFunctionDelegate<F>(private val fragment: F) where F : Fragment, F : BaseFragmentFunction {

    private var serviceConnectionState = SERVICE_DISCONNECTED

    @Suppress("UNCHECKED_CAST")
    fun getBaseActivity(): BaseActivity? {
        return fragment.activity as BaseActivity?
    }

    fun onActivityCreated(savedInstanceState: Bundle?) {
        performServiceConnectionCreate()
    }

    fun onResume() {
        performServiceConnectionStart()
    }

    fun onPause() {
        performServiceConnectionStop()
    }

    fun onDestroy() {
        performServiceConnectionDestroy()
    }

    fun performServiceConnectionCreate() {
        performServiceConnection(SERVICE_DISCONNECTED, SERVICE_CONNECTION_CREATED) { service ->
            fragment.onServiceConnectionCreate(service)
            performChildrenServiceConnection {
                it.performServiceConnectionCreate()
            }
        }
    }

    private fun performServiceConnection(currentState: Int, nextState: Int, event: (service: DataPrepareService) -> Unit): Boolean
            = if (serviceConnectionState == currentState) {
                  getBaseActivity()?.dataPrepareService?.let {
                      serviceConnectionState = nextState
                      event(it)
                  }
                  true
              } else {
                  false
              }

    private fun performServiceConnection(state1: Int, state2: Int, nextState: Int, event: (service: DataPrepareService) -> Unit) {
        if (!performServiceConnection(state1, nextState, event)) {
            performServiceConnection(state2, nextState, event)
        }
    }

    private fun performChildrenServiceConnection(childEvent: (fragment: BaseFragmentFunction) -> Unit) {
        //val childFragments = fragment.childFragmentManager.fragments ?: return
        fragment.childFragmentManager.fragments.forEach {
            if (it is BaseFragmentFunction) {
                childEvent(it)
            }
        }
    }

    fun performServiceConnectionStart() {
        performServiceConnectionCreate()
        performServiceConnection(SERVICE_CONNECTION_CREATED, SERVICE_CONNECTION_STOPPED, SERVICE_CONNECTION_STARTED) { service ->
            fragment.onServiceConnectionStart(service)
            performChildrenServiceConnection {
                it.performServiceConnectionStart()
            }
        }
    }

    private fun performServiceConnectionStop() {
        performServiceConnection(SERVICE_CONNECTION_STARTED, SERVICE_CONNECTION_STOPPED) {
            fragment.onServiceConnectionStop(it)
        }
    }

    private fun performServiceConnectionDestroy() {
        performServiceConnection(SERVICE_CONNECTION_STOPPED, SERVICE_CONNECTION_DESTROYED) {
            fragment.onServiceConnectionDestroy(it)
        }
    }

    fun isFragmentInvalid(): Boolean {
        return fragment.getBaseActivity()?.invalid() != false
    }
}