package com.weisi.tool.wsnbox.processor.accessor

import android.content.Context
import com.weisi.tool.wsnbox.bean.configuration.Settings
import com.wsn.lib.wsb.communicator.Communicator
import com.wsn.lib.wsb.communicator.tcp.Tcp
import com.wsn.lib.wsb.communicator.tcp.TcpClient
import com.wsn.lib.wsb.communicator.tcp.TcpSocket
import com.wsn.lib.wsb.protocol.UdpSensorProtocol

/**
 * Created by CJQ on 2018/3/13.
 */
class TcpSensorDataAccessor : CommonSensorDataAccessor<TcpSensorDataAccessor.CommunicatorDelegate, UdpSensorProtocol>(CommunicatorDelegate(), UdpSensorProtocol()) {

    private var mDataRequestFrame = dataRequestFrame

    override fun onStartDataAccess(context: Context, settings: Settings, listener: OnStartResultListener) {
        mCommunicator.tcpClient.connect(settings.remoteServerIp, settings.remoteServerPort, object : TcpClient.OnServerConnectListener {
            override fun onServerConnect(state: Tcp.ConnectState, socket: TcpSocket?) {
                if (state == Tcp.ConnectState.CONNECTING) {
                    notifyStartFailed(listener, ERR_IS_CONNECTING)
                } else if (state == Tcp.ConnectState.CONNECTED && socket != null) {
                    setHasTimeSynchronized(true)
                    onPostLaunchCommunicator(context, settings, listener)
                } else {
                    notifyStartFailed(listener, ERR_LAUNCH_COMMUNICATOR_FAILED)
                }
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
        mCommunicator.tcpClient.tcpSocket.write(mDataRequestFrame)
    }

    class CommunicatorDelegate : Communicator {

        val tcpClient = TcpClient()

        override fun read(dst: ByteArray, offset: Int, length: Int): Int {
            return tcpClient.tcpSocket.read(dst, offset, length)
        }

        override fun canRead(): Boolean {
            return tcpClient.tcpSocket.canRead()
        }

        override fun stopRead() {
            tcpClient.tcpSocket.stopRead()
        }

        fun shutdown() {
            tcpClient.shutdown()
        }
    }
}