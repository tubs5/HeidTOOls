package me.heid.heidtools.stream.chat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class DevicePickerDialog extends DialogFragment {

    public interface Selected{
        void Selected(WifiP2pDevice device);
    }
    private final Selected d;

    public DevicePickerDialog(Selected d) {
        this.d = d;
    }

    private ArrayAdapter adapter;
    private ArrayList<WifiP2pDevice> devices;
    private final ArrayList<String> deviceNames = new ArrayList<>();

    public void addItemToAdapter(ArrayList<WifiP2pDevice> items){
        deviceNames.add("a");
        deviceNames.add("b");
        deviceNames.add("c");


        if(adapter == null) adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,deviceNames);
        devices = items;
        deviceNames.clear();
        if(items == null) return;
        for (WifiP2pDevice device: items) {
            deviceNames.add(device.deviceName);
        }

        adapter.notifyDataSetChanged();
        getDialog().invalidateOptionsMenu();
        Log.w("test",adapter.getCount()+"");


    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        deviceNames.add("d");
        deviceNames.add("e");
        deviceNames.add("f");

        if(adapter == null) adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,deviceNames);
        Log.w("test",adapter.getCount()+"");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("WifiDirect Target").setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (WifiP2pDevice de:devices) {
                    String dev = (String) adapter.getItem(which);
                    if(de.deviceName.equals(dev)){
                        d.Selected(de);
                        return;
                    }
                }
            }
        });

        return builder.create();
    }



}
