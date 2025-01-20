import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardSizeTest {
    @Test
    void testFromString() {
        assertEquals(BoardSize.NINE, BoardSize.fromString("9x9"));
        assertEquals(BoardSize.THIRTEEN, BoardSize.fromString("13x13"));
        assertEquals(BoardSize.NINETEEN, BoardSize.fromString("19x19"));
    }

    @Test
    void testFailingFromString() {
        assertThrows(IllegalStateException.class, () -> { BoardSize.fromString("Wrong String"); });
    }
}
