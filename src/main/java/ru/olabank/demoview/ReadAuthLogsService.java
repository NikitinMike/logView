package ru.olabank.demoview;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;
import static ru.olabank.demoview.MainController.dtf;

@Slf4j
public class ReadAuthLogsService {
    static final String basePath = "/home/olauser/logs/auth/";

    static public Path checkFile(Date date){
        File fileZip = new File(basePath + "archived/auth." + dtf.format(date) + ".0.log.gz");
        if(fileZip.isFile() && fileZip.canRead())
            return decompressGzip(fileZip.toPath(), new File("auth.log").toPath());
        return null;
    }

    public static List<DataMessage> readAuthLog(Date dt) {
        log.info("GET {} {} Ok", dtf.format(dt), ((ServletRequestAttributes) currentRequestAttributes()).getRequest().getRemoteAddr());
        Path dataFile = checkFile(dt);
        if (dataFile==null) dataFile = new File(basePath + "auth.log").toPath();
        try (Stream<String> lines = Files.lines(dataFile)) {
            return lines.filter(l -> !l.matches(".+(main|Ok|GET|request|TokenController|password|CLIENT|OAuthDao).+"))
                    .filter(l -> l.matches(".+ru.olabank.sprint.auth.+"))
                    .filter(l -> l.matches(".+http-nio-8072-exec-.+"))
                    .map(ReadAuthLogsService::cleanAll).map(DataMessage::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> readFiles() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(basePath + "archived/"))) {
            return stream.filter(file -> !Files.isDirectory(file)).sorted()
                    .map(f -> f.getFileName().toString()
                            .replaceFirst("auth.(.*).0.log.gz", "$1"))
                    .collect(Collectors.toList());
        }
    }

    static String cleanAll(String m) {
        return m.replaceAll("ru.olabank.sprint.auth.config.CustomAuthentication|x-forwarded-for=.+ "
                + "|request SecurityContextHolderAwareRequestWrapper|headers=|found=|x-real-ip="
                + "|org.springframework.security.web.header.HeaderWriterFilter.HeaderWriterRequest@"
                + "|http-nio-8072-exec-|client_id=android_app, scope=read write,|content-length=..,"
                + "|user_agent=|5.0.0-alpha.|username=|fingerprint=|accept-encoding=gzip,|user-agent="
                + "|content-type=application/x-www-form-urlencoded,|authorization=Basic .+=,"
                + "|Get client by login=|x-forwarded-proto=https, host=auth.olabank.ru, x-forwarded-port=443,"
                + "|x-forwarded-server=sv-proxy-1, x-forwarded-host=auth.olabank.ru, ", "");
    }

    static Path decompressGzip(Path source, Path destPath) {
        try (GZIPInputStream gis = new GZIPInputStream(Files.newInputStream(source))) {
            Files.delete(destPath);
            Files.copy(gis, destPath);
            return destPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
