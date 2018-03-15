package com.weisi.tool.wsnbox.processor

import android.content.Context
import com.cjq.lib.weisi.communicator.Communicator
import com.cjq.lib.weisi.communicator.tcp.TcpClient
import com.cjq.lib.weisi.protocol.UdpSensorProtocol
import com.weisi.tool.wsnbox.bean.configuration.Settings

/**
 * Created by CJQ on 2018/3/13.
 */
class TcpSensorDataAccessor : CommonSensorDataAccessor<TcpSensorDataAccessor.CommunicatorDelegate, UdpSensorProtocol>(CommunicatorDelegate(), UdpSensorProtocol()) {

    private var mDataRequestFrame = dataRequestFrame

    override fun onStartDataAccess(context: Context?, settings: Settings?, listener: OnStartResultListener?) {
        mCommunicator.tcpClient.connect(settings!!.remoteServerIp, settings!!.remoteServerPort, TcpClient.OnServerConnectListener {
            if (it === null) {
                notifyStartFailed(listener, ERR_LAUNCH_COMMUNICATOR_FAILED)
            } else {
                onPostLaunchCommunicator(context, settings, listener)
            }
        }, 10000)
    }

    override fun launchCommunicator(context: Context?, settings: Settings?): Boolean {
        return true
    }

    override fun onInitDataRequestTaskParameter(settings: Settings?): Boolean {
        return true
    }

    override fun onTimeSynchronize(settings: Settings?) {
    }

    override fun getDataRequestCycle(settings: Settings?): Long {
        return settings!!.tcpDataRequestCycle
    }

    override fun shutdownCommunicator() {
        mCommunicator.shutdown()
    }

    override fun sendDataRequestFrame() {
        mCommunicator.tcpClient.socket.write(mDataRequestFrame)
    }

    class CommunicatorDelegate : Communicator {

        val tcpClient = TcpClient()

        override fun read(dst: ByteArray?, offset: Int, length: Int): Int {
            return tcpClient.socket.read(dst, offset, length)
        }

        override fun canRead(): Boolean {
            return tcpClient.socket.canRead()
        }

        override fun stopRead() {
            tcpClient.socket.stopRead()
        }

        fun shutdown() {
            tcpClient.shutdown()
        }
    }
}