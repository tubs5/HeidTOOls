package me.heid.heidtools.stream.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import me.heid.heidtools.R;

public class ChatClient extends AppCompatActivity {

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    Messenger mService = null;
    WifiDirectHelper wifiDirect;
    private TextView tv;
    private Intent intentStartService;
    private TextViewModel model;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, ConnectionService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.
                msg = Message.obtain(null, ConnectionService.MSG_SET_VALUE, this.hashCode(), 0);
                mService.send(msg);

                model.toSend.observeForever(s -> {
                    if (model.toSend.getValue().equals("")) return;
                    String mess = model.toSend.getValue() + "";
                    model.toSend.setValue("");

                    Bundle b = new Bundle();
                    b.putString("ToSend", mess);
                    Message message = Message.obtain(null, ConnectionService.MSG_SET_VALUE, null);
                    message.setData(b);
                    try {
                        mService.send(message);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                });


            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }


        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        //ActivityWifiBinding binding =ActivityWifiBinding

        intentStartService = new Intent(this, ConnectionService.class);
        Button scan = ((Button) findViewById(R.id.buttonScan));
        tv = ((TextView) findViewById(R.id.wifiTExt));
        EditText et = ((EditText) findViewById(R.id.wifiEditTExt));
        Button send = ((Button) findViewById(R.id.wifiButtonSend));


        WifiDirectHelper.OnConnectionAsHost onHost = HostAdress -> {
            tv.setText("Connecting as HOST");
            tv.invalidate();
            Message m = Message.obtain(null, ConnectionService.MSG_START, null);
            Bundle b = new Bundle();
            b.putString("HostAddress", HostAdress);
            b.putBoolean("Host", true);
            m.setData(b);
            try {
                mService.send(m);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };
        WifiDirectHelper.OnConnectionAsUser onUser = HostAdress -> {
            tv.setText("Connected as USER");
            tv.invalidate();
            Message m = Message.obtain(null, ConnectionService.MSG_START, null);
            Bundle b = new Bundle();
            b.putBoolean("Host", false);
            b.putString("HostAddress", HostAdress);
            m.setData(b);
            try {
                mService.send(m);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };
        wifiDirect = new WifiDirectHelper(this.getApplicationContext(), onHost, onUser);


        model = new ViewModelProvider(this).get(TextViewModel.class);
        // Create the observer which updates the UI.
        final Observer<String> nameObserver = newName -> {
            // Update the UI, in this case, a TextView.
            tv.setText(newName);
        };
        model.getChat().observe(this, nameObserver);


       // server.setOnClickListener(v -> wifiDirect.connect());


        scan.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("WIFI", "PERMISSION MISSING");
                Toast.makeText(v.getContext(), "MISSING PERMISSION", Toast.LENGTH_LONG).show();
                return;
            }
            wifiDirect.Scan(this);

        });

        send.setOnClickListener(v -> {
            model.toSend.setValue(et.getText().toString());
            et.setText("");
            et.invalidate();
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(intentStartService, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        wifiDirect.RegisterReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        wifiDirect.UnRegisterReceiver();
    }

    public static class TextViewModel extends ViewModel {
        private MutableLiveData<String> chat = new MutableLiveData<>();
        private MutableLiveData<String> toSend = new MutableLiveData<>();

        public MutableLiveData<String> getChat() {
            if (chat == null) {
                chat = new MutableLiveData<>();
            }
            return chat;
        }

        public MutableLiveData<String> getToSend() {
            if (toSend == null) {
                toSend = new MutableLiveData<>();
            }
            return toSend;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ConnectionService.MSG_SET_VALUE) {
                String data = msg.getData().getString("ToReceive");
                model.chat.setValue(model.chat.getValue() + "\n" + data);
            } else {
                super.handleMessage(msg);
            }

        }
    }
}