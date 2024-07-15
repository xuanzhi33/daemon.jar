package cn.xuanzhi33.daemonjar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class UpdateService {

    @Value("${daemonJar.token:daemonJar}")
    private String token;


    @Value("${daemonJar.cmd:ping,baidu.com}")
    private List<String> cmdList;

    @Value("${daemonJar.url:https://www.baidu.com}")
    private String downloadUrl;

    @Value("${daemonJar.name:test.html}")
    private String downloadPath;

    private Thread processThread;
    private Boolean isRunning = false;
    private Process jarProcess;


    public boolean checkToken(String userToken) {
        return token.equals(userToken);
    }

    private void runCmd() {
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(true);
        log.info("Execute command: {}", cmdList);
        try {
            jarProcess = pb.start();
            // 获取标准输出，并打印出来
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            jarProcess.waitFor();
            log.info("Execute command finished: {}", cmdList);
            Result.success();
        } catch (Exception e) {
            log.error("Execute command error: ", e);
            log.error("Command: {}", cmdList);
            Result.error(500, "Execute command error");
        }
    }

    public synchronized Result startProcessIfNotRunning() {
        if (isRunning) {
            return Result.error(400, "Process is running");
        }
        isRunning = true;

        processThread = new Thread(() -> {
            runCmd();
            isRunning = false;
        });
        processThread.start();
        return Result.success();
    }

    public synchronized void stopProcessIfRunning() {
        if (!isRunning) {
            return;
        }

        jarProcess.destroy();

        try {
            processThread.join();
        } catch (InterruptedException e) {
            log.error("Stop process error: ", e);
        }
    }

    public synchronized Result startOrRestartProcess() {
        stopProcessIfRunning();
        return startProcessIfNotRunning();
    }

    public synchronized Result downloadFile(String url) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Path filePath = Path.of(downloadPath);
            boolean isDeleted = Files.deleteIfExists(filePath);
            if (isDeleted) {
                log.info("Deleted file: {}", filePath);
            }

            log.info("Start download file: {}", url);
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            client.send(request,
                    HttpResponse.BodyHandlers.ofFile(filePath));
            log.info("Download file finished: {}", filePath);
            return Result.success();
        } catch (IOException | InterruptedException e) {
            log.error("Download file error: ", e);
            return Result.error(500, "Download file error");
        }
    }

    public synchronized Result updateServer() {
        Result result = downloadFile(downloadUrl);
        if (result.getCode() != 200) {
            return result;
        }

        return startOrRestartProcess();
    }


}
