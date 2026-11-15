package dev.damda.template.api;

import dev.damda.global.error.BusinessException;
import dev.damda.global.error.CommonErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TemplateController {

    @GetMapping("/api/v1/templates/{id}")
    public TemplateDetailResponse detail(@PathVariable Long id) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
