package org.advisor.member.constants;

public enum Authority {
    USER,
    ADMIN,
    MODERATOR;
    public static boolean isValid(String value) {
        try {
            Authority.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
