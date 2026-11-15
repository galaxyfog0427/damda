package dev.damda.template.api;

import dev.damda.global.error.BusinessException;
import dev.damda.global.error.CommonErrorCode;
import dev.damda.template.domain.Category;
import dev.damda.template.domain.Format;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    @GetMapping
    public PagedModel<TemplateSummaryResponse> list(
            @RequestParam(required = false) @Nullable Category category,
            @RequestParam(required = false) @Nullable Format format,
            @RequestParam(required = false) @Nullable String q,
            @RequestParam(defaultValue = "0") @Min(0) int page) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @GetMapping("/{id}")
    public TemplateDetailResponse detail(@PathVariable Long id) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
