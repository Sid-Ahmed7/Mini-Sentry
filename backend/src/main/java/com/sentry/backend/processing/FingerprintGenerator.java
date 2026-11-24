package com.sentry.backend.processing;

import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.dto.request.EventRequest;
import com.sentry.backend.dto.request.FrameRequest;
import com.sentry.backend.processing.message.FilePaths;
import com.sentry.backend.processing.message.Hashing;
import com.sentry.backend.processing.message.MessageNormalizer;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class FingerprintGenerator {

    private final Hashing hashing;

    public String generate(EventRequest event) {
        StringBuilder source = new StringBuilder();
        source.append(Objects.toString(event.type(), "")).append(CommonConstants.LINE_SEPARATOR);

        int count = 0;
        for (FrameRequest frame : event.allFrames()) {
            if (!StackFrameClassifier.isApplicationCode(frame)) {
                continue;
            }
            source.append(Objects.toString(frame.function(), "")).append(CommonConstants.FUNCTION_FILE_SEPARATOR).append(FilePaths.baseName(frame.file())).append(CommonConstants.LINE_SEPARATOR);
            count++;
            if (count == CommonConstants.MAX_APP_FRAMES) {
                break;
            }
        }

        if (count == 0) {
            source.append(MessageNormalizer.normalize(event.message()));
        }
        return hashing.sha256Hex(source.toString());
    }
}
