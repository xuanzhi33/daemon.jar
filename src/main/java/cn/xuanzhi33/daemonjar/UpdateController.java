package cn.xuanzhi33.daemonjar;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UpdateController {
    private final UpdateService updateService;

    public UpdateController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @GetMapping("/update")

    public Result update(@RequestParam String token, HttpServletRequest request) {
        if (!updateService.checkToken(token)) {
            String ipAddr = request.getHeader("X-Forwarded-For");
            log.info("Token error, attempt from: {}", ipAddr);
            return Result.tokenError();
        }

        return updateService.updateServer();
    }
}
