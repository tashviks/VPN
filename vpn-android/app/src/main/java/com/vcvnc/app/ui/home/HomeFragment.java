package com.vcvnc.app.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.vcvnc.app.R;
import com.vcvnc.vpn.ProxyConfig;
import com.vcvnc.vpn.utils.VpnServiceHelper;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Handler handler;
    private TextView textView;
    private Button startRunBtn;
    private Button stopRunBtn;
    View root;
    public static final String myPref ="preferenceName";
    ProxyConfig.VpnStatusListener vpnStatusListener = new ProxyConfig.VpnStatusListener() {

        @Override
        public void onVpnStart(Context context) {
            handler.post(new Runnable() {
                             public void run() {
                                 System.out.println("vpn start");
                             }
                         }
            );
        }

        @Override
        public void onVpnEnd(Context context) {
            handler.post(new Runnable() {
                             public void run() {
                                 System.out.println("vpn stop");
                             }
                         }
            );
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        textView = root.findViewById(R.id.text_home);


        if(!getPreferenceValue("ip").equals("0")){
            ProxyConfig.serverIp= getPreferenceValue("ip");
            ProxyConfig.serverPort = Integer.parseInt(getPreferenceValue("port"));
            ProxyConfig.DNS_FIRST = getPreferenceValue("dns1");
            ProxyConfig.DNS_SECOND = getPreferenceValue("dns2");
        }

        handler = new Handler();

        startRunBtn = root.findViewById(R.id.start_run);
        startRunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVPN();
                startRunBtn.setVisibility(View.INVISIBLE);
                stopRunBtn.setVisibility(View.VISIBLE);
                textView.setText("connected: /" + ProxyConfig.serverIp + ":" +ProxyConfig.serverPort);
            }
        });
        stopRunBtn = root.findViewById(R.id.stop_run);
        stopRunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeVpn();
                startRunBtn.setVisibility(View.VISIBLE);
                stopRunBtn.setVisibility(View.INVISIBLE);
                textView.setText("vpn address: /" + ProxyConfig.serverIp + ":" +ProxyConfig.serverPort);
            }
        });

        ProxyConfig.Instance.registerVpnStatusListener(vpnStatusListener);
        updateUI();
        return root;
    }

    public String getPreferenceValue(String key)
    {
        SharedPreferences sp = root.getContext().getSharedPreferences(myPref,0);
        String str = sp.getString(key,"0");
        return str;
    }

    public void writeToPreference(String key, String value)
    {
        SharedPreferences.Editor editor = root.getContext().getSharedPreferences(myPref,0).edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void updateUI() {
        if (VpnServiceHelper.vpnRunningStatus()){
            startRunBtn.setVisibility(View.INVISIBLE);
            stopRunBtn.setVisibility(View.VISIBLE);
            textView.setText("connected: /" + ProxyConfig.serverIp + ":" +ProxyConfig.serverPort);
        }else{
            startRunBtn.setVisibility(View.VISIBLE);
            stopRunBtn.setVisibility(View.INVISIBLE);
            textView.setText("vpn address: /" + ProxyConfig.serverIp + ":" +ProxyConfig.serverPort);
        }
    }

    private void startVPN() {
        if (!VpnServiceHelper.vpnRunningStatus()) {
            VpnServiceHelper.changeVpnRunningStatus(this.getContext(), true);
        }

    }

    private void closeVpn() {
        if (VpnServiceHelper.vpnRunningStatus()) {
            VpnServiceHelper.changeVpnRunningStatus(this.getContext(), false);
        }
    }


}
