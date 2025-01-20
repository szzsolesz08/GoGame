import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class GoStateTest {

    @ParameterizedTest
    @ArgumentsSource(NeighborsProvider.class)
    public void testGetNeighors(Point point, Point[] expectedNeighbors) {
        GoState state = new GoState(BoardSize.NINE.getSize());
        Point[] neighbors = state.getNeighbors(point);
        Assertions.assertArrayEquals(expectedNeighbors, neighbors);
    }

    static class  NeighborsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(new Point(0, 0), new Point[]{
                            new Point(1, 0),
                            new Point(0, 1)
                    }),
                    Arguments.of(new Point(4, 4), new Point[]{
                            new Point(3, 4),
                            new Point(5, 4),
                            new Point(4, 3),
                            new Point(4, 5)
                    }),
                    Arguments.of(new Point(0, 4), new Point[]{
                            new Point(1, 4),
                            new Point(0, 3),
                            new Point(0, 5)
                    })
            );
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0,0", "1,1", "2,2"
    })
    void testLegalMove_occupiedSpace(int x, int y) {
        GoState state = new GoState(BoardSize.NINE.getSize());
        state = state.placeStone(new Point(x, y));
        Assertions.assertFalse(state.isLegalMove(new Point(x, y)));
    }

    @Test
    void testLegalMove_selfCapture() {
        GoState state = new GoState(BoardSize.NINE.getSize());
        state = state.placeStone(new Point(0, 1))
                     .placeStone(new Point(1, 0))
                     .placeStone(new Point(1, 1))
                     .placeStone(new Point(0, 0));

        Assertions.assertFalse(state.isLegalMove(new Point(0, 1)));
    }

    @Test
    void testLegalMove_repeating() {
        GoState state = new GoState(BoardSize.NINE.getSize());
        state = state.placeStone(new Point(0, 0))
                .placeStone(new Point(1, 0))
                .placeStone(new Point(0, 1))
                .placeStone(new Point(1, 0));

        Assertions.assertFalse(state.isLegalMove(new Point(0, 0)));
    }

    @Test
    void testCheckCapture() {
        GoState state = new GoState(BoardSize.NINE.getSize());
        state = state.placeStone(new Point(1, 1)) // Black
                .placeStone(new Point(1, 2)) // White
                .placeStone(new Point(2, 2)) // Black
                .placeStone(new Point(1, 3)) // White
                .placeStone(new Point(2, 3)) // Black
                .placeStone(new Point(4, 1)) // White
                .placeStone(new Point(0, 2)) // Black
                .placeStone(new Point(4, 2)) // White
                .placeStone(new Point(0, 3)) // Black
                .placeStone(new Point(4, 3)); // White

        GoState newState = state.placeStone(new Point(1, 4));

        Assertions.assertEquals(BoardSpace.EMPTY, newState.board[1][2]);
        Assertions.assertEquals(2, newState.blackCaptured);
    }

    @Test
    void testGetLiberties() {
        GoState state = new GoState(BoardSize.NINE.getSize());
        state = state.placeStone(new Point(4, 4));

        Set<Point> scanned = new HashSet<>();
        Point[] liberties = state.getLiberties(Stone.BLACK, new Point(4, 4), scanned);

        Point[] expectedLiberties = new Point[]{
                new Point(5, 4),
                new Point(4, 3),
                new Point(3, 4),
                new Point(4, 5)
        };

        Assertions.assertArrayEquals(expectedLiberties, liberties);
    }
}
