package com.example.jit.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button discover_peers;
    ListView lv;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    ArrayList<String> names;
    Collection<WifiP2pDevice> peers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discover_peers = (Button)findViewById(R.id.discover);
        lv = (ListView)findViewById(R.id.peers_list);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void Discover_Peers(View v){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,"peers discovered",Toast.LENGTH_SHORT).show();
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peerlist) {
                        peers = peerlist.getDeviceList();
                        UpdateList();
                    }
                });
            }
            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this,"error in discovery, may be your WiFi is off",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void UpdateList(){
        names = new ArrayList<String>();
        for(Iterator i=peers.iterator();i.hasNext();){
            WifiP2pDevice peer = (WifiP2pDevice) i.next();
            names.add(peer.deviceName);
        }

        ArrayAdapter la=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,names);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                String s=(String)parent.getAdapter().getItem(position);
                connect_to_peer(s);
            }
        });

        lv.setAdapter(la);
    }

    public  void connect_to_peer(String name){
        WifiP2pConfig config = new WifiP2pConfig();
        for(Iterator i=peers.iterator();i.hasNext();){
            WifiP2pDevice peer = (WifiP2pDevice) i.next();
            if(peer.deviceName.equals(name)){
                config.deviceAddress = peer.deviceAddress;
                break;
            }
        }
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this,"error in connecting",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
