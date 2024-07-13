package cn.xuanzhi33.daemonjar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
@Slf4j
public class UpdateService {

    @Value("${daemonJar.token:daemonJar}")
    private String token;


    @Value("${daemonJar.cmd:ping,baidu.com}")
    private List<String> execCmdList;

    @Value("${daemonJar.url:https://www.baidu.com}")
    private String downloadUrl;

    @Value("${daemonJar.name:test.html}")
    private String downloadPath;

    private Thread processThread;
    private Boolean isRunning = false;
    private Process process;


    public boolean checkToken(String userToken) {
        return token.equals(userToken);
    }

    private void runCmd(List<String> cmdList) {
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(true);
        log.info("Execute command: {}", cmdList);
        try {
            process = pb.start();
            // 获取标准输出，并打印出来
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }

            process.waitFor();
            log.info("Execute command finished: {}", cmdList);
        } catch (Exception e) {
            log.error("Execute command error: ", e);
            log.error("Command: {}", cmdList);
        }
    }

    public synchronized Result startProcessIfNotRunning() {
        if (isRunning) {
            return Result.error(400, "Process is running");
        }
        isRunning = true;

        processThread = new Thread(() -> {
            runCmd(execCmdList);
            isRunning = false;
        });
        processThread.start();
        return Result.success();
    }

    public synchronized void stopProcessIfRunning() {
        if (!isRunning) {
            return;
        }

        process.destroy();
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
        WebClient webClient = WebClient.create();
        Flux<DataBuffer> dataBufferFlux = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        Path path = Paths.get(downloadPath);

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) { // 如果文件存在，清空文件内容
            DataBufferUtils.write(dataBufferFlux, channel)
                    .doOnComplete(() -> log.info("Download file finished"))
                    .blockLast();
            return Result.success();
        } catch (Exception e) {
            log.error("Download file error: ", e);
            return Result.error(500, "Download file error: " + e.getMessage());
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
