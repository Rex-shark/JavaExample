import java.util.Optional;

public class VariationSelectorMapper {
    private static final int VARIATION_SELECTOR_START = 0xFE00;
    private static final int VARIATION_SELECTOR_END = 0xFE0F;
    private static final int VARIATION_SELECTOR_SUPPLEMENT_START = 0xE0100;
    private static final int VARIATION_SELECTOR_SUPPLEMENT_END = 0xE01EF;

    public static String toVariationSelector(int byteVal) {
        if (byteVal >= 0 && byteVal < 16) {
            return new String(Character.toChars(VARIATION_SELECTOR_START + byteVal));
        } else if (byteVal >= 16 && byteVal < 256) {
            return new String(Character.toChars(VARIATION_SELECTOR_SUPPLEMENT_START + byteVal - 16));
        } else {
            return null;
        }
    }

    public static Integer fromVariationSelector(int codePoint) {
        if (codePoint >= VARIATION_SELECTOR_START && codePoint <= VARIATION_SELECTOR_END) {
            return codePoint - VARIATION_SELECTOR_START;
        } else if (codePoint >= VARIATION_SELECTOR_SUPPLEMENT_START && codePoint <= VARIATION_SELECTOR_SUPPLEMENT_END) {
            return codePoint - VARIATION_SELECTOR_SUPPLEMENT_START + 16;
        } else {
            return null;
        }
    }
}

