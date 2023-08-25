package ru.olabank.demoview;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataMessage {
    final static GeoService service;

    static {
        try {
            service = new GeoService();
        } catch (IOException | GeoIp2Exception e) {
            throw new RuntimeException(e);
        }
    }

    String id;
    String title;
    String text;
    String ip = "";
    String login = "";
    String phone = "";
    String date = "";
    Boolean print = true;

    public DataMessage(String msg) {
        String[] m = msg.replaceAll("[{},]", "").split("\\s+", 6);
        date = m[0];
        print = false;
        id = m[1].substring(0, 8);
        title = m[2]; // String.format("%s %s", m[2], m[3]);
        text = m[5];
        if (text.contains("Wrong")) {
            String[] ip = text.split("from");
            if (ip.length > 1) this.ip = ip[1];
        } else {
            String[] data = m[5].split(":");
            String[] user = data[0].split("\\s+");
            if (user.length > 2)
                if (data.length > 1) {
                    ip = data[1].trim();
                    phone = user[2];
                    login = user[0];
                    text = user[1];
                    if (ip.matches("\\w+")) phone = ip;
                } else {
                    login = user[0];
                    if (user[2].matches("\\w+")) phone = user[2];
                    else ip = user[2];
                }
            else {
                String[] name = m[5].split("\\s+");
                login = name[0].trim();
            }
        }
        text = text.replace(login, "").replace(ip, "");
        if (phone != null) text = text.replace(phone, "");
        if (!ip.trim().isEmpty()) try {
            text += " " + service.check(ip.trim());
        } catch (IOException | GeoIp2Exception e) {
//                throw new RuntimeException(e);
        }
    }
}