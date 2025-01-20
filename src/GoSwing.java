import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import static javax.swing.JFileChooser.APPROVE_OPTION;

/*

TODOs for students: create the following classes in package gogame:

enums BoardSize, Stone, BoardSpace
classes Point, GoState
JUnit GoStateTest

*/
public class GoSwing {
	public static record GoInputs(
		JComboBox<String> cb,
		JButton button,
		JButton loadButton,
		JButton passButton
	) {
		public static GoInputs make() {
			var cb = new JComboBox<>(BoardSize.getStringValues());
			cb.setMaximumSize(cb.getPreferredSize());

			var button = new JButton("Start Game");
			var loadButton = new JButton("Load Game");
			var passButton = new JButton("Pass");
			passButton.setVisible(false);

			return new GoInputs(cb, button, loadButton, passButton);
		}
	}

	public static record GoPanel(
		JPanel panel,
		JPanel hpanel,
		JTextArea gameLabel,
		GoPane pane,

		GoInputs inputs
	) {
		public static GoPanel make() {
			var inputs = GoInputs.make();

			var panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			var hpanel = new JPanel();
			hpanel.add(inputs.cb());
			hpanel.add(inputs.button());
			hpanel.add(inputs.loadButton());
			hpanel.add(inputs.passButton());
			panel.add(hpanel);

			var gameLabel = new JTextArea("\n");
			panel.add(gameLabel);

			var pane = new GoPane(txt -> {
				gameLabel.setText(txt);
			}, new GoState(9));
			panel.add(pane);

			return new GoPanel(panel, hpanel, gameLabel, pane, inputs);
		}
	}

	public static record GoFrame(JFrame frame, GoPanel panel) {
		public static GoFrame make() {
			var frame = new JFrame("Go");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(600, 640);

			var panel = GoPanel.make();
			frame.getContentPane().add(panel.panel());

			return new GoFrame(frame, panel);
		}

		private void setupStyles() {
			panel().hpanel().setMaximumSize(panel().hpanel().getPreferredSize());
			panel().gameLabel().setOpaque(false);
			panel().gameLabel().setMaximumSize(new Dimension(600, panel().gameLabel().getPreferredSize().height));
		}

		private void setupActions() {
			setupToggleGame();
			setupLoad();
			setupSize();
			setupPass();
		}

		private void setupToggleGame() {
			Runnable toggleGame = () -> {
				panel().pane().toggle();

				var isActive = panel().pane().isActive;
				panel().inputs().button().setText(isActive ? "End Game" : "Start Game");
				panel().inputs().loadButton().setText(isActive ? "Save Game" : "Load Game");

				panel().inputs().cb().setEnabled(!isActive);
				panel().inputs().passButton().setVisible(isActive);
			};

			panel().pane().endGame = toggleGame;

			panel().inputs().button().addActionListener(event -> {
				toggleGame.run();
			});
		}

		private void setupLoad() {
			panel().inputs().loadButton().addActionListener(event -> {
				var chooser = new JFileChooser();
				var filter = new FileNameExtensionFilter("Go Saved Game Files", "go");
				chooser.setFileFilter(filter);
				if (panel().pane().isActive) {
					int returnVal = chooser.showSaveDialog(frame());
					if (returnVal == APPROVE_OPTION) {
						String filename = chooser.getSelectedFile().toString();
						panel().pane().saveGame(filename.endsWith(".go") ? chooser.getSelectedFile() : new File(filename + ".go"));
					}
				} else {
					int returnVal = chooser.showOpenDialog(frame());
					if (returnVal == APPROVE_OPTION) {
						panel().pane().loadGame(chooser.getSelectedFile());
					}
				}
			});
		}

		private void setupSize() {
			panel().inputs().cb().addActionListener(event -> {
				var selectedTxt = (String) panel().inputs().cb().getSelectedItem();
				var size = BoardSize.fromString(selectedTxt).getSize();
				panel().pane().setGoState(size);
			});
		}

		private void setupPass() {
			panel().inputs().passButton().addActionListener(event -> {
				panel().pane().pass();
			});
		}
	}

	public static void main(String[] args) {
		var goFrame = GoFrame.make();

		goFrame.setupStyles();
		goFrame.setupActions();
		goFrame.frame().setVisible(true);
	}
}
