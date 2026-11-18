package dev.damda.order.api;

import dev.damda.global.error.BusinessException;
import dev.damda.global.error.CommonErrorCode;
import jakarta.validation.constraints.Min;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/library")
public class LibraryController {

    @GetMapping
    public PagedModel<LibraryItemResponse> listLibrary(@RequestParam(defaultValue = "0") @Min(0) int page) {

        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @PostMapping("/{templateId}/downloads")
    public DownloadResponse redownload(@PathVariable Long templateId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
