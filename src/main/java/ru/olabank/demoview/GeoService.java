package ru.olabank.demoview;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Slf4j
@Service
public class GeoService {

    //    File database = new File("GeoLiteCity2018.dat");
    final static File database = new File("GeoLite2-City.mmdb");
    final static DatabaseReader dbReader;

    static {
        try {
            dbReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GeoService() throws IOException, GeoIp2Exception {
        log.info("GeoService running on "
                +check(InetAddress.getByName("mob.olabank.ru").getHostAddress()));
    }

    String null2Space(String s){return s==null?"":s;}

    public String check(String ip) throws IOException, GeoIp2Exception {
        CityResponse response = dbReader.city(InetAddress.getByName(ip));
        return null2Space(response.getCountry().getName())
                +" "+null2Space(response.getMostSpecificSubdivision().getName())
                +" "+null2Space(response.getCity().getName());
    }
}
