package me.heid.heidtools.stream.chat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import me.heid.heidtools.BR;

public class ConnectionService extends Service {
    int startMode = 0;       // indicates how to behave if the service is killed
    boolean allowRebind = true; // indicates whether onRebind should be used
    boolean host;
    ServiceThread thread;
    String HostAddress;
    private ChatClient.TextViewModel model;
    private boolean initialized = false;
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_VALUE = 3;
    static final int MSG_START= 4;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    private Messages messageese = new Messages();
    private static class Messages extends BaseObservable {
        private String toSend;
        private String toReceive;

        @Bindable
        public String getToSend() {
            if(toSend == null) toSend= "";
            return toSend;
        }

        @Bindable
        public String getToReceive() {
            if(toReceive == null) toReceive = "";
            return toReceive;
        }

        public synchronized void SetToSend(String toSend) {
            this.toSend = toSend;
            notifyPropertyChanged(BR.toSend);
        }
        public synchronized void SetToReceive(String toReceive) {
            this.toReceive = toReceive;
            notifyPropertyChanged(BR.toReceive);
        }
    }



    private final class ServiceThread extends Thread{
        boolean host;
        boolean run = true;
        Socket socket = new Socket();
        String hostAddress;

        public ServiceThread(@NonNull String name, boolean host, String hostAddress) {
            super(name);
            this.host = host;
            this.hostAddress = hostAddress;
        }

        @Override
        public void run() {
            while(run) {
                try {

                    if (host) {
                        ServerSocket ss = new ServerSocket(2139);
                        socket = ss.accept();

                    } else {
                       try{

                           socket.bind(null);
                           socket.connect((new InetSocketAddress(hostAddress, 2139)), 200);

                       }catch (SocketTimeoutException e){
                           Log.w("WIFI Service", e.getLocalizedMessage().toString());
                       }
                    }
                    if(socket != null) {
                        if (socket.isConnected()) {
                            HandleConnection();
                        }
                    }

                    sleep(200);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            super.run();

        }

        private void HandleConnection() throws IOException, InterruptedException {
            Messages m = messageese;
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            m.SetToReceive("Connected");

            while (socket.isConnected()){
                if(reader.ready()){
                    if(m.getToReceive().equals("")) m.SetToReceive(reader.readLine());
                }
                if(!m.getToSend().equals("")){
                    writer.print(m.getToSend()+"\n");
                    m.SetToSend("");
                    writer.flush();
                }
                sleep(250);
            }



        }


    }

    @Override
    public void onCreate() {


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        return startMode;
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed

    }

    class IncomingHandler extends Handler {
        private Context applicationContext;
        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case ConnectionService.MSG_SET_VALUE:
                    if((msg.getData().getString("ToSend") != null)) messageese.SetToSend(msg.getData().getString("ToSend"));
                    break;
                case ConnectionService.MSG_START:

                    host = msg.getData().getBoolean("Host",false);
                    HostAddress = msg.getData().getString("HostAddress");
                    ServiceThread t = new ServiceThread("start",host,HostAddress);
                    t.start();

                    if(mMessenger != null){
                        messageese.addOnPropertyChangedCallback(callback);
                        initialized = true;
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    }
    Messenger mMessenger;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mMessenger = new Messenger(new IncomingHandler(this));
        if(!initialized){
            messageese.addOnPropertyChangedCallback(callback);
            initialized = true;
        }


        return mMessenger.getBinder();

    }

   Observable.OnPropertyChangedCallback callback =  new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {

            if(propertyId == BR.toReceive){
                if(messageese.getToReceive().equals("")) return;
                String mess = messageese.getToReceive() + "";
                messageese.SetToReceive("");

                Bundle b = new Bundle();
                b.putString("ToReceive",mess);
                Message message = Message.obtain(null,ConnectionService.MSG_SET_VALUE,null);
                message.setData(b);
                try {
                    for (Messenger m: mClients) {
                        m.send(message);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };


}
