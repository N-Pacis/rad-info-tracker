package rw.auca.radinfotracker.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ERole {
    ADMIN("ADMIN"),
    TECHNICIAN("TECHNICIAN"),
    RADIOLOGIST("RADIOLOGIST"),
    QUALITY_ASSURANCE("QUALITY_ASSURANCE"),
    FINANCE("FINANCE"),
    FRONT_DESK("FRONT_DESK");

    private String value;

    ERole(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ERole fromValue(String text) {
        for (ERole b : ERole.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
