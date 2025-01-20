import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class GoPane extends JComponent implements MouseListener, MouseMotionListener, ComponentListener {
	private GoState state;
	private int pixelsPerSpace;
	private int pixelSize;
	private Point hover;
	private Consumer<String> updateLabel;
	public Runnable endGame;
	public boolean isActive;

	public GoPane(Consumer<String> updateLabel, GoState state) {
		this.updateLabel = updateLabel;
		setGoState(state);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addComponentListener(this);
	}

	public void setGoState(int size) {
		setGoState(new GoState(size));
	}

	public void setGoState(GoState state) {
		this.state = state;
		componentResized(null);
	}

	public void toggle() {
		isActive = !isActive;
		if (isActive)
			updateLabel.accept(state.toString());
	}

	public void pass() {
		if (state.makeMove(null)) {
			endGame.run();
		}
		updateLabel.accept(state.toString());
		repaint();
	}

	public void saveGame(File file) {
		state.saveGame(file);
	}

	public void loadGame(File file) {
		setGoState(GoState.loadGame(file));
	}

	private void drawStone(Graphics g, Point p, Color on, Color bg) {
		var pps = pixelsPerSpace;

		g.setColor(bg);
		g.fillOval(p.x * pps, p.y * pps, pps, pps);
		g.setColor(on);
		g.fillOval(p.x * pps + pps*33/100, p.y * pps + pps*33/100, pps / 3, pps / 3);
	}

	private void drawLib(Graphics g, int x, int y, Color on) {
		g.setColor(on);
		var pps = pixelsPerSpace;
		g.drawOval(x * pps + pps*33/100, y * pps + pps*33/100, pps / 3, pps / 3);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.GRAY);

		drawGridLines(g);
		drawStones(g);
	}

	private void drawStones(Graphics g) {
		Set<Point> libs = getHoveredLibs();

		for (int x = 0; x < state.board.length; x++) {
			for (int y = 0; y < state.board.length; y++) {
				var boardSpace = state.board[x][y];
				var bg = Color.LIGHT_GRAY;

				if (boardSpace == BoardSpace.EMPTY) {
					if (isActive && hover != null && hover.x == x && hover.y == y) {
						var on = state.turn == Stone.BLACK ? Color.BLACK : Color.WHITE;
						drawStone(g, hover, on, bg);
					} else if (libs.contains(new Point(x, y))) {
						var on = state.turn == Stone.BLACK ? Color.BLACK : Color.RED;
						drawLib(g, x, y, on);
					}
					continue;
				}

				var on = boardSpace == BoardSpace.BLACK ? Color.BLACK : Color.WHITE;
				drawStone(g, new Point(x, y), on, on);
			}
		}
	}

	private Set<Point> getHoveredLibs() {
		if (!isActive || hover == null)   return new HashSet<>();

		Set<Point> alreadyScanned = new HashSet<>();
		Point[] libsArr = state.getLiberties(state.turn, hover, alreadyScanned);

		Set<Point> libs = new HashSet<>();
		libs.addAll(Arrays.asList(libsArr));
		return libs;
	}

	private void drawGridLines(Graphics g) {
		for (int x = 0; x < state.board.length; x++) {
			g.fillRect(x * pixelsPerSpace + pixelsPerSpace / 2, 0, 2, pixelSize);
			g.fillRect(0, x * pixelsPerSpace + pixelsPerSpace / 2, pixelSize, 2);
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (!isActive)
			return;
		state.makeMove(new Point(e.getX() / pixelsPerSpace, e.getY() / pixelsPerSpace));
		System.out.println(e.getX() / pixelsPerSpace + ":" + e.getY() / pixelsPerSpace);
		updateLabel.accept(state.toString());
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		hover = null;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		/*if (!isActive)
			return;
		hover = new Point(e.getX() / pixelsPerSpace, e.getY() / pixelsPerSpace);
		if (!state.isLegalMove(hover))
			hover = null;
		repaint();*/
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		pixelsPerSpace = Math.min(getWidth(), getHeight()) / state.board.length;
		pixelSize = pixelsPerSpace * state.board.length;
		repaint();
	}

	public void componentShown(ComponentEvent e) {
	}
}
