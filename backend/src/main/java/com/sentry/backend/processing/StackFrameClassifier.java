package com.sentry.backend.processing;

import java.util.Objects;

import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.dto.request.FrameRequest;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public class StackFrameClassifier {

    public static boolean isApplicationCode(FrameRequest frame) {
        if(frame == null) {
            return false;
        }

        if(frame.inApp() != null) {
            return frame.inApp();
        }

        String searchText = (Objects.toString(frame.file(), "") + " " + Objects.toString(frame.function(), "")).toLowerCase();
        
        if(searchText.isBlank()) {
            return false;
        }

        for (String lib : CommonConstants.LIBRARY_MARKERS) {
            if(searchText.contains(lib)) {
                return false;
            }
        }
        return  frame.file() != null && !frame.file().isBlank();
    }
    
}
