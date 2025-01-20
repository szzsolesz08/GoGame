import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class GoState implements Predicate<Point> {
    public final BoardSpace[][] board;
    public int blackCaptured;
    public int whiteCaptured;
    public Stone turn;
    private final Set<GoState> previousStates;

    public GoState(int size) {
        BoardSize boardSize = BoardSize.fromString(size + "x" + size);
        this.board = java.util.stream.IntStream.range(0, boardSize.getSize())
                .mapToObj(i -> java.util.stream.IntStream.range(0, boardSize.getSize())
                        .mapToObj(j -> BoardSpace.EMPTY)
                        .toArray(BoardSpace[]::new))
                .toArray(BoardSpace[][]::new);;
        this.blackCaptured = 0;
        this.whiteCaptured = 0;
        this.turn = Stone.BLACK;
        this.previousStates = new HashSet<>();

    }

    public GoState(GoState previous) {
        this.board = Arrays.stream(previous.board)
                .map(boardSpaces -> Arrays.copyOf(boardSpaces, boardSpaces.length))
                .toArray(BoardSpace[][]::new);
        this.blackCaptured = previous.blackCaptured;
        this.whiteCaptured = previous.whiteCaptured;
        this.turn = previous.turn;
        this.previousStates = new HashSet<>(previous.previousStates);
    }

    public Point[] getNeighbors(Point point) {
        Point[] neighbors = {
                new Point(point.x - 1, point.y),
                new Point(point.x + 1, point.y),
                new Point(point.x, point.y - 1),
                new Point(point.x, point.y + 1),
        };

        return Arrays.stream(neighbors)
                .filter(this)
                .toArray(Point[]::new);
    }

    public Point[] getLiberties(Stone s, Point p, Set<Point> scanned) {
        Set<Point> liberties = new HashSet<>();
        Set<Point> visited = new HashSet<>();
        Deque<Point> toScan = new ArrayDeque<>();
        toScan.add(p);

        while (!toScan.isEmpty()) {
            Point currentPoint = toScan.poll();

            if (visited.contains(currentPoint)) { continue; }
            visited.add(currentPoint);

            BoardSpace space = board[currentPoint.x][currentPoint.y];
            if (space == BoardSpace.EMPTY) {
                liberties.add(currentPoint);
            }
            else if (space.stone == s) {
                scanned.add(currentPoint);
                toScan.addAll(Arrays.stream(getNeighbors(currentPoint))
                        .filter(neighbor -> !visited.contains(neighbor))
                        .toList());
            }
        }

        return liberties.toArray(new Point[0]);
    }

    public void checkCaptured(Point p) {
        Stone opponent = turn.opposite();
        Set<Point> scanned = new HashSet<>();

        Point[] liberties = getLiberties(opponent, p, scanned);
        if (liberties.length == 0) {
            scanned.forEach(capturedStone -> board[capturedStone.x][capturedStone.y] = BoardSpace.EMPTY);

            if (opponent == Stone.BLACK) {
                whiteCaptured += scanned.size();
            }
            else {
                blackCaptured += scanned.size();
            }
        }



        /*java.util.Arrays.stream(getNeighbors(p))
                .filter(neighbor -> board[neighbor.x][neighbor.y].stone == opponent)
                .map(neighbor -> getLiberties(opponent, neighbor, scanned))
                .filter(result -> result.length == 0)
                .forEach(result -> {
                    scanned.forEach(point -> board[point.x][point.y] = BoardSpace.EMPTY);

                    if (opponent == Stone.BLACK) {
                        whiteCaptured += scanned.size();
                    } else {
                        blackCaptured += scanned.size();
                    }
                });*/
    }

    public GoState placeStone(Point p) {
        board[p.x][p.y] = BoardSpace.fromStone(turn);

        Arrays.stream(getNeighbors(p)).forEach(this::checkCaptured);

        turn = turn.opposite();

        return this;
    }

    public boolean isLegalMove(Point p) {
        if (!test(p) || board[p.x][p.y] != BoardSpace.EMPTY) { return false; }



        boolean selfCapture = Arrays.stream(getNeighbors(p))
                .map(neighbor -> getLiberties(turn, neighbor, new HashSet<>()))
                .allMatch(liberties -> liberties.length == 0);

        if (selfCapture) {
            boolean opponentCapture = Arrays.stream(getNeighbors(p))
                    .filter(neighbor -> board[neighbor.x][neighbor.y].stone == turn.opposite())
                    .map(neighbor -> getLiberties(turn.opposite(), neighbor, new HashSet<>()))
                    .anyMatch(liberties -> liberties.length == 1);

            if (!opponentCapture) {return false; }
        }

        GoState currentState = new GoState(this).placeStone(p);
        currentState.turn = turn.opposite();
        if (previousStates.contains(currentState)) { return false; }
        return true;
    }

    public boolean makeMove(Point p) {
        if (p == null) {
            GoState state = new GoState(this);
            previousStates.add(state);
            turn = turn.opposite();
            return false;
        }
        if (!isLegalMove(p)) { return false; }

        GoState state = this.placeStone(p);
        previousStates.add(state);

        return previousStates.contains(state);
    }

    public static GoState loadGame(File file) {
        try (ObjectInput input = new ObjectInputStream(new FileInputStream(file))) {
            return (GoState) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load file", e);
        }
    }

    public void saveGame(File file) {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(this);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save file", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) {return false;}
        GoState goState = (GoState) obj;
        return Arrays.deepEquals(board, goState.board) && turn == goState.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.deepHashCode(board), turn);
    }

    @Override
    public boolean test(Point point) {
        return point.x >= 0 && point.x < board.length && point.y >= 0 && point.y < board[0].length;
    }

    @Override
    public String toString() {
        return "Black Captured: " + blackCaptured + "\n" +
                "White Captured: " + whiteCaptured;
    }
}
