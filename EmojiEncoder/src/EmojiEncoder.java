public interface EmojiEncoder {
    /**
     * Encode the given text into a string that starts with the provided emoji (or character)
     * followed by variation selectors representing the UTF-8 bytes of the text.
     *
     * @param emoji leading emoji or character to carry the hidden message
     * @param text the plaintext to encode
     * @return encoded string
     */
    String encode(String emoji, String text);

    /**
     * Decode the provided encoded string (which may contain an emoji and variation selectors)
     * back into the original plaintext. If no variation selectors found, returns an empty string.
     *
     * @param encoded full encoded string
     * @return decoded plaintext
     */
    String decode(String encoded);
}

