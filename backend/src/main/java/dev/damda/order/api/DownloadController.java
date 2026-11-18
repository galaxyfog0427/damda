package dev.damda.order.api;

import dev.damda.global.error.BusinessException;
import dev.damda.global.error.CommonErrorCode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/templates")
public class DownloadController {

    @PostMapping("/{id}/downloads")
    public DownloadResponse download(@PathVariable Long id) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
