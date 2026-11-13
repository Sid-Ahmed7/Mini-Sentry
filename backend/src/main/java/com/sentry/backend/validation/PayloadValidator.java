package com.sentry.backend.validation;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.sentry.backend.common.CurrentLocale;
import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.dto.request.BreadcrumbRequest;
import com.sentry.backend.dto.request.EventRequest;
import com.sentry.backend.dto.request.FrameRequest;
import com.sentry.backend.exceptions.InvalidPayloadException;

import java.util.Map;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PayloadValidator {

    private MessageSource messageSource;

    public void validate(EventRequest event) {

        if (event == null) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.empty", null, CurrentLocale.getCurrentLocale()));
        }

        required(event.type(), "type", CommonConstants.MAX_REQUIRED);
        optional(event.message(), "message", CommonConstants.MAX_OPTIONAL);

        if (event.platform() == null || !CommonConstants.PLATFORM_PATTERN.matcher(event.platform()).matches()) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.platform.invalid", null, CurrentLocale.getCurrentLocale()));
        }
        if (event.release() != null && !CommonConstants.RELEASE_PATTERN.matcher(event.release()).matches()) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.release.invalid", null, CurrentLocale.getCurrentLocale()));
        }
        if (event.environment() != null && !CommonConstants.ENVIRONMENT_PATTERN.matcher(event.environment()).matches()) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.environment.invalid", null, CurrentLocale.getCurrentLocale()));
        }

        optional(event.url(), "url", CommonConstants.MAX_OPTIONAL_URL);

        if (event.allFrames().size() > CommonConstants.MAX_FRAMES) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.frames.tooMany", null, CurrentLocale.getCurrentLocale()));
        }
        for (FrameRequest f : event.allFrames()) {
            if (f == null) {
                throw new InvalidPayloadException(messageSource.getMessage("payload.frame.null", null, CurrentLocale.getCurrentLocale()));
            }
            optional(f.function(), "frame.function", CommonConstants.MAX_OPTIONAL_FRAME_FUNCTION);
            optional(f.file(), "frame.file", CommonConstants.MAX_OPTIONAL_FRAME_FILE);
            range(f.line(), "frame.line");
            range(f.column(), "frame.column");
        }

        if (event.allTags().size() > CommonConstants.MAX_TAGS) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.tags.tooMany", null, CurrentLocale.getCurrentLocale()));
        }
        for (Map.Entry<String, String> entry : event.allTags().entrySet()) {
            required(entry.getKey(), "tag.key", CommonConstants.MAX_TAG_KEY);
            optional(entry.getValue(), "tag.value", CommonConstants.MAX_TAG_VALUE);
        }

        if (event.allBreadcrumb().size() > CommonConstants.MAX_BREADCRUMBS) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.breadcrumbs.tooMany", null, CurrentLocale.getCurrentLocale()));
        }
        for (BreadcrumbRequest breadcrumb : event.allBreadcrumb()) {
            if (breadcrumb == null) {
                throw new InvalidPayloadException(messageSource.getMessage("payload.breadcrumb.null", null, CurrentLocale.getCurrentLocale()));
            }
            optional(breadcrumb.message(), "breadcrumb.message", CommonConstants.MAX_BREADCRUMB_MESSAGE);
        }
    }

    private void required(String value, String name, Integer max) {
        if (value == null || value.isBlank()) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.field.required", new Object[]{name}, CurrentLocale.getCurrentLocale()));
        }
        optional(value, name, max);
    }

    private void optional(String value, String name, Integer max) {
        if (value != null && value.length() > max) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.field.tooLong", new Object[]{name}, CurrentLocale.getCurrentLocale()));
        }
    }

    private void range(Integer value, String name) {
        if (value != null && (value < 0 || value > 10_000_000)) {
            throw new InvalidPayloadException(messageSource.getMessage("payload.field.outOfRange", new Object[]{name}, CurrentLocale.getCurrentLocale()));
        }
    }

}
