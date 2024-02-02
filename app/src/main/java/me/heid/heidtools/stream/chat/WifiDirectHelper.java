package me.heid.heidtools.stream.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

public class WifiDirectHelper {
    private static boolean IsWifiP2pEnabled = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private final Context applicationContext;
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private MyBroadcastReceiver receiver;
    private final List<WifiP2pDevice> peers = new ArrayList<>();
    private final OnConnectionAsUser user;
    private final OnConnectionAsHost host;

    DevicePickerDialog newFragment = new DevicePickerDialog(new DevicePickerDialog.Selected() {
        @Override
        public void Selected(WifiP2pDevice device) {
            connect(device);
        }
    });
    private final WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            ArrayList<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());
            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);
                newFragment.addItemToAdapter(refreshedPeers);
                // If an AdapterView is backed by this data, notify it
                // of the change. For instance, if you have a ListView of
                // available peers, trigger an update.
                // ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
            }

            if (peers.size() == 0) {
                Log.d("WIFI", "No devices found");
                return;
            } else {
                for (WifiP2pDevice dev : peers) {
                    Log.d("WIFI", dev.deviceName + "\t" + dev.primaryDeviceType + "\t" + dev.secondaryDeviceType + "\t" + dev.status);
                }

            }
        }
    };
    private final WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {

            // String from WifiP2pInfo struct
            String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
            // After the group negotiation, we can determine the group owner
            // (server).
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a group owner thread and accepting
                // incoming connections.
                host.OnConnectionAsHost(groupOwnerAddress);

            } else if (info.groupFormed) {
                // The other device acts as the peer (client). In this case,
                // you'll want to create a peer thread that connects
                // to the group owner.
                user.OnConnectionAsUser(groupOwnerAddress);
            }
        }
    };

    public WifiDirectHelper(Context c, OnConnectionAsHost host, OnConnectionAsUser user) {
        this.user = user;
        this.host = host;
        applicationContext = c;
        manager = (WifiP2pManager) c.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(c, c.getMainLooper(), null);
        setupIntentFilter();
    }

    private static void setIsWifiP2pEnabled(boolean b) {
        IsWifiP2pEnabled = b;
    }

    private void setupIntentFilter() {
        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @SuppressLint("MissingPermission")
    public void connect(WifiP2pDevice device) {

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(applicationContext, "Connecting", Toast.LENGTH_SHORT).show();
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(applicationContext, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Scan(FragmentActivity f) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
               // Toast.makeText(applicationContext, "SCANNING", Toast.LENGTH_LONG).show();
                newFragment.show(f.getSupportFragmentManager(), "Scan");




                Log.e("WIFI", "SCANNING");
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(applicationContext, "SCANNING FAILED", Toast.LENGTH_LONG).show();
                Log.e("WIFI", "SCANNING FAILED REASON:" + reasonCode);
            }
        });
    }

    public void RegisterReceiver() {
        receiver = new MyBroadcastReceiver(manager, channel, applicationContext);
        applicationContext.registerReceiver(receiver, intentFilter);
    }

    public void UnRegisterReceiver() {
        applicationContext.unregisterReceiver(receiver);
    }

    public interface OnConnectionAsHost {
        @SuppressLint("NotConstructor")
        void OnConnectionAsHost(String HostAddress);
    }

    public interface OnConnectionAsUser {
        @SuppressLint("NotConstructor")
        void OnConnectionAsUser(String HostAddress);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        private final WifiP2pManager manager;
        private final WifiP2pManager.Channel channel;
        private Activity a;

        public MyBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Context c) {
            this.manager = manager;
            this.channel = channel;
            this.a = a;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wi-Fi Direct mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                WifiDirectHelper.setIsWifiP2pEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                // The peer list has changed! We should probably do something about
                // that.
                if (manager != null) {
                    manager.requestPeers(channel, peerListListener);
                }

                Log.d("WIFIDIRECT", "The peer list has changed!");
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                if (manager == null) {
                    return;
                }

                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    manager.requestConnectionInfo(channel, connectionListener);

                    Log.d("WIFIDIRECT", "Connection state changed!");
                } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
               /* DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));*/
                    Log.d("WIFIDIRECT", "FOUND ONE");


                }
            }
        }
    }

}
