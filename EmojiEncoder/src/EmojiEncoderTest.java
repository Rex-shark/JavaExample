public class EmojiEncoderTest {
    public static void main(String[] args) {
        EmojiEncoder encoder = new EmojiEncoderImpl();

        String[] tests = new String[] {
            "Hello, World!",
            "Testing 123",
            "Special chars: !@#$%^&*()",
            "Unicode: 你好，世界",
            "",
            " "
        };

        String[] emojis = EmojiConstants.EMOJI_LIST;

        boolean allPassed = true;
        for (String emoji : emojis) {
            for (String t : tests) {
                String encoded = encoder.encode(emoji, t);
                String decoded = encoder.decode(encoded);
                boolean ok = t.equals(decoded);
                if (!ok) {
                    allPassed = false;
                    System.out.println("FAIL for emoji='" + emoji + "' text='" + t + "' -> decoded='" + decoded + "'");
                }
            }
        }

        if (allPassed) System.out.println("All tests passed");
    }
}

