package com.vcvnc.app.ui.notifications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.vcvnc.app.R;
import com.vcvnc.vpn.ProxyConfig;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private EditText ip;
    private EditText port;
    private EditText dns1;
    private EditText dns2;
    private Button saveBtn;
    private View root;
    public static final String myPref ="preferenceName";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        root = inflater.inflate(R.layout.fragment_notifications, container, false);
        //final TextView textView = root.findViewById(R.id.text_notifications);

        ip = root.findViewById(R.id.options_ip_value);
        port = root.findViewById(R.id.options_port_value);
        dns1 = root.findViewById(R.id.options_dns1_value);
        dns2 = root.findViewById(R.id.options_dns2_value);
        saveBtn = root.findViewById(R.id.options_save_btn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProxyConfig.serverIp= getIP();
                ProxyConfig.serverPort = Integer.parseInt(getPort());
                ProxyConfig.DNS_FIRST = getDns1();
                ProxyConfig.DNS_SECOND = getDns2();


                writeToPreference("ip", ProxyConfig.serverIp);
                writeToPreference("port", ProxyConfig.serverPort + "");
                writeToPreference("dns1", ProxyConfig.DNS_FIRST);
                writeToPreference("dns2", ProxyConfig.DNS_SECOND);

                showAlertDialog(getString(R.string.apply_success));
            }
        });

        if(!getPreferenceValue("ip").equals("0")){
            ProxyConfig.serverIp= getPreferenceValue("ip");
            ProxyConfig.serverPort = Integer.parseInt(getPreferenceValue("port"));
            ProxyConfig.DNS_FIRST = getPreferenceValue("dns1");
            ProxyConfig.DNS_SECOND = getPreferenceValue("dns2");
        }

        setIP(ProxyConfig.serverIp);
        setPort(ProxyConfig.serverPort);
        setDns1(ProxyConfig.DNS_FIRST);
        setDns2(ProxyConfig.DNS_SECOND);
        return root;
    }

    private void showAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext()).setIcon(R.mipmap.ic_launcher).setTitle(getString(R.string.result))
                .setMessage(msg).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(root.getContext(), "成功！", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
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

    public String getIP() {
        return ip.getText().toString();
    }

    public void setIP(String value) {
        this.ip.setText(value);
    }

    public String getPort() {
        return port.getText().toString();
    }

    public void setPort(int value) {
        this.port.setText(value + "");
    }

    public String getDns1() {
        return this.dns1.getText().toString();
    }

    public void setDns1(String value) {
        this.dns1.setText(value);
    }

    public String getDns2() {
        return this.dns2.getText().toString();
    }

    public void setDns2(String value) {
        this.dns2.setText(value);
    }
}
