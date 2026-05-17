package com.example.zero.healthcare.util;

import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CursorUtils {

    private CursorUtils() {}

    public static String encode(Long id) {
        if (id == null) return null;
        return Base64.getEncoder().encodeToString(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
    }

    public static Long decode(String cursor) {
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            return Long.parseLong(decoded);
        } catch (Exception e) {
            throw new CoreException(ErrorCode.INVALID_CURSOR);
        }
    }
}
