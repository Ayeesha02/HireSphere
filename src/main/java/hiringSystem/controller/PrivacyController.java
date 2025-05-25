/**
 * PrivacyController.java
 * 
 * This controller handles requests related to the privacy notice of the application.
 */
package hiringSystem.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class PrivacyController {

    /**
     * Endpoint to retrieve the privacy notice.
     * 
     * @return ResponseEntity containing the privacy notice HTML content.
     */
    @GetMapping("/privacy")
    public ResponseEntity<String> getPrivacyNotice() throws IOException {
        ClassPathResource resource = new ClassPathResource("privacyNotice.html");
        byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String content = new String(data, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(content);
    }
}