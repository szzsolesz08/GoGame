public enum BoardSize {
    NINE(9),
    THIRTEEN(13),
    NINETEEN(19);

    private final int size;

    BoardSize(int size) {
        this.size = size;
    }

    public static BoardSize fromString(String s) {
        return java.util.Arrays.stream(values())
                .filter(b -> b.toString().equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Wrong board size: " + s));
    }

    public static String[] getStringValues() {
        return java.util.Arrays.stream(values())
                .map(BoardSize::toString)
                .toArray(String[]::new);
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return size + "x" + size;
    };
}
