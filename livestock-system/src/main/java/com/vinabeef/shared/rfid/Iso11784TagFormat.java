package com.vinabeef.shared.rfid;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** ISO 11784/11785 animal tag: 15 digits (3-digit country code, e.g. 704 for Viet Nam, plus 12-digit ID). */
@Component
public class Iso11784TagFormat implements TagFormat {

    private static final Pattern FIFTEEN_DIGITS = Pattern.compile("\\d{15}");

    @Override
    public String name() {
        return "ISO 11784/11785 (15 digits)";
    }

    @Override
    public boolean isValid(String tag) {
        return tag != null && FIFTEEN_DIGITS.matcher(tag).matches();
    }
}
