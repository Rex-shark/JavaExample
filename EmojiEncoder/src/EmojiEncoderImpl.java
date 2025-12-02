import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EmojiEncoderImpl implements EmojiEncoder {
    @Override
    public String encode(String emoji, String text) {
        if (emoji == null) throw new IllegalArgumentException("emoji is null");
        if (text == null) throw new IllegalArgumentException("text is null");

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        sb.append(emoji);

        for (byte b : bytes) {
            int unsigned = b & 0xFF;
            String vs = VariationSelectorMapper.toVariationSelector(unsigned);
            if (vs == null) throw new IllegalArgumentException("byte out of range: " + unsigned);
            sb.append(vs);
        }

        return sb.toString();
    }

    @Override
    public String decode(String encoded) {
        if (encoded == null) throw new IllegalArgumentException("encoded is null");

        List<Integer> collected = new ArrayList<>();

        int i = 0;
        int length = encoded.length();
        while (i < length) {
            int codePoint = encoded.codePointAt(i);
            Integer b = VariationSelectorMapper.fromVariationSelector(codePoint);
            if (b == null && collected.size() > 0) {
                break; // stop when we have collected some bytes and encounter non-selector
            } else if (b == null) {
                // skip until selectors start
            } else {
                collected.add(b);
            }
            i += Character.charCount(codePoint);
        }

        byte[] arr = new byte[collected.size()];
        for (int j = 0; j < collected.size(); j++) {
            arr[j] = (byte) (collected.get(j) & 0xFF);
        }

        return new String(arr, StandardCharsets.UTF_8);
    }
}

