package cn.xuanzhi33.daemonjar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private UpdateService updateService;

	@Test
	void contextLoads() {

	}

	@Test
	void testUpdate() {
		Result result = updateService.updateServer();
		// 判断是否更新成功
		assertEquals(200, result.getCode(), "Update failed");
		// 判断存在index.html文件
		Path path = Path.of("test.html");
        assertTrue(path.toFile().exists(), "test.html not found");

		// 读取文件内容
        try {
            String fileContent = Files.readString(path, StandardCharsets.UTF_8);
			assertTrue(fileContent.contains("<html>"), "index.html content error");
        } catch (IOException e) {
            // 直接抛出异常
			fail("Read file failed");
        }
    }

}
