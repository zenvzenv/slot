package zenv.slot.utils;

/**
 * @author zhengwei AKA zenv
 * @since 2022/12/2 9:06
 */
public final class StringUtils {
    /**
     * 根据 class 中的字段生成对应的 Setter/Getter 方法名
     *
     * @param fieldName 字段名称
     * @return 将首字母大写的字段方法名
     */
    public static String getSetGetMethodName(String fieldName) {
        final char[] chars = fieldName.toCharArray();
        chars[0] = toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

    private static char toUpperCase(char c) {
        if (97 <= c && c <= 122) {
            c ^= 32;
        }
        return c;
    }
}
