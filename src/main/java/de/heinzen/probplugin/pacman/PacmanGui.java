package de.heinzen.probplugin.pacman;

import de.prob.cli.ProBInstanceProvider;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Christoph Heinzen on 15.08.17.
 */
public class PacmanGui {

    private static final Color BACKGROUND_BLUE = Color.valueOf("#0033ff");
    private static final Color PACMAN_YELLOW = Color.valueOf("#ffff33");
    private static final Color[] GHOST_COLORS = {
            Color.valueOf("#dd0000"),
            Color.valueOf("#66ffff"),
            Color.valueOf("#ff9999"),
            Color.valueOf("#ff9900")};

    private final PacmanAnimator animator;

    private Text scoreValueText;
    private Node[] livePacmans = new Node[3];
    private Group[] ghosts = new Group[4];
    private Node pacman;
    private HashMap<Position, Node> scoreDots;
    private HashMap<Position, Node> ghostDots;

    public PacmanGui(PacmanAnimator animator) {
        this.animator = animator;
    }

    /**
     * Methods to create and update graphical elements
     */
    private Node createPacman(double x, double y) {
        SVGPath pacman = new SVGPath();
        pacman.setContent("m " + x + "," + y +" -13,-7.5 a 15,15 0 1 1 0,15 z");
        pacman.setStroke(PACMAN_YELLOW);
        pacman.setFill(PACMAN_YELLOW);
        return pacman;
    }

    private Group createGhost(Color color, Position pos) {
        Group ghost = new Group();
        ghost.setLayoutX(toImagePos(pos.getX()));
        ghost.setLayoutY(toImagePos(pos.getY()));

        SVGPath body = new SVGPath();
        body.setContent("m 0,0 -13,0 0,-2 a 13,13 0 0 1 26,0 l 0,17 -4,-4 -4,4 -1,0 -4,-4 -4,4 -1,0 -4,-4 -4,4 0,-15 z");
        body.setStroke(color);
        body.setFill(color);
        ghost.getChildren().add(body);

        Circle leftEye = new Circle(-5, -4.4, 3, Color.WHITE);
        Circle rightEye = new Circle(5, -4.4, 3, Color.WHITE);
        ghost.getChildren().addAll(leftEye, rightEye);

        Circle leftPupil = new Circle(-5,-5.2,1.5, BACKGROUND_BLUE);
        Circle rightPupil = new Circle(5,-5.2,1.5, BACKGROUND_BLUE);
        ghost.getChildren().addAll(leftPupil, rightPupil);

        return ghost;
    }

    private Node createRectangle(double x, double y, double height, double width, Color color) {
        Rectangle rect = new Rectangle(x,y,width,height);
        rect.setFill(color);
        return rect;
    }

    private Node createCircle(Position pos, double radius) {
        return new Circle(toImagePos(pos.getX()), toImagePos(pos.getY()), radius, Color.WHITE);
    }

    public void createGui(Tab tab) {

        //create GUI
        AnchorPane pane = new AnchorPane();
        pane.setMinWidth(580);
        pane.setMinHeight(740);
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        Group root = new Group();
        List<Position> black = animator.getPositions("begehbar \\/ geisterhof \\/ geisterhof_zugang");
        List<Position> scoreDots = animator.getPositions("punktefelder_aktuell");
        List<Position> ghostDots = animator.getPositions("geister_aktuell");

        Node backgroundBlue = createRectangle(0,0,640,580,BACKGROUND_BLUE);
        root.getChildren().add(backgroundBlue);

        SVGPath whiteLine = new SVGPath();
        whiteLine.setContent("m 0,640 580,0");
        whiteLine.setStroke(Color.WHITE);
        whiteLine.setStrokeWidth(2);
        root.getChildren().add(whiteLine);

        Text scoreText = new Text(5,680,"Score:");
        scoreText.setFont(Font.font(40));
        scoreText.setFill(PACMAN_YELLOW);
        root.getChildren().add(scoreText);

        Text liveText = new Text(5,730,"Live:");
        liveText.setFont(Font.font(40));
        liveText.setFill(PACMAN_YELLOW);
        root.getChildren().add(liveText);

        scoreValueText = new Text("0");
        scoreValueText.setFont(Font.font(40));
        scoreValueText.setFill(PACMAN_YELLOW);
        scoreValueText.setY(680);
        scoreValueText.setX(318 - scoreValueText.getLayoutBounds().getWidth());
        root.getChildren().add(scoreValueText);

        livePacmans[0] = createPacman(303, 715);
        livePacmans[1] = createPacman(263, 715);
        livePacmans[2] = createPacman(223, 715);
        root.getChildren().addAll(livePacmans);

        for (Position pos : black) {
            Node rect = createRectangle(pos.getX() * 10 + 20, pos.getY() * 10 + 20,40,40,Color.BLACK);
            root.getChildren().add(rect);
        }

        SVGPath barrier = new SVGPath();
        barrier.setContent("m 260,269 l 60,0 0,2 -60,0 z");
        barrier.setFill(Color.WHITE);
        root.getChildren().add(barrier);

        this.scoreDots = new HashMap<>();
        for (Position pos : scoreDots) {
            Node c = createCircle(pos, 4);
            this.scoreDots.put(pos, c);
            root.getChildren().add(c);
        }

        this.ghostDots = new HashMap<>();

        for (Position pos : ghostDots) {
            Node c = createCircle(pos, 10);
            this.ghostDots.put(pos, c);
            root.getChildren().add(c);
        }

        Position pacStart = animator.getPosition("startposition");
        pacman = createPacman(toImagePos(pacStart.getX()), toImagePos(pacStart.getY()));
        root.getChildren().add(pacman);

        for (int i = 0; i < 4; i++) {
            ghosts[i] = createGhost(GHOST_COLORS[i], animator.getPosition("startpos_geist_" + (i+1)));
        }
        root.getChildren().addAll(ghosts);

        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        pane.getChildren().add(root);

        tab.setClosable(false);
        tab.setContent(pane);

        update();
    }

    public void updateScoreValue() {
        if (scoreValueText != null) {
            scoreValueText.setText(animator.getNumber("score") + "");
            scoreValueText.setX(318 - scoreValueText.getLayoutBounds().getWidth());
        }
    }

    public void updatePacman() {
        if (pacman != null) {
            Position pacmanPos = animator.getPosition("position");
            Position pacmanPosOld = animator.getPosition("vorherige_position");
            Position start = animator.getPosition("startposition");
            if (!pacmanPos.equals(pacmanPosOld)) {
                int deltaX = pacmanPosOld.getX() - pacmanPos.getX();
                int deltaY = pacmanPosOld.getY() - pacmanPos.getY();
                if (deltaX == -2 || deltaX > 2) {
                    pacman.setRotate(180);
                } else if (deltaY == 2) {
                    pacman.setRotate(90);
                } else if (deltaY == -2) {
                    pacman.setRotate(-90);
                } else {
                    pacman.setRotate(0);
                }
                int startDeltaX = start.getX() - pacmanPos.getX();
                int startDeltaY = start.getY() - pacmanPos.getY();
                pacman.setLayoutX(0 - startDeltaX * 10);
                pacman.setLayoutY(0 - startDeltaY * 10);
            } else {
                pacman.setLayoutX(0);
                pacman.setLayoutY(0);
                pacman.setRotate(0);
            }
        }
    }

    public void updateGhost(int i) {
        if (ghosts[i] != null) {
            Position ghostPos = animator.getPosition("pos_geist_" + (i+1));
            boolean hunted = animator.check("geist_"  + (i+1) + " : gejagte_geister");
            Color bodyColor = hunted ? BACKGROUND_BLUE : GHOST_COLORS[i];

            ghosts[i].setLayoutX(ghostPos.getX() * 10 + 40);
            ghosts[i].setLayoutY(ghostPos.getY() * 10 + 40);
            for(Node n : ghosts[i].getChildren()) {
                if (n instanceof SVGPath) {
                    SVGPath body = (SVGPath) n;
                    body.setFill(bodyColor);
                    body.setStroke(bodyColor);
                }
            }
        }
    }

    public void updateLives() {
        int lives = animator.getNumber("leben");
        for (Node pac : livePacmans) {
            pac.setOpacity(0);
        }
        for (int i = 0; i < lives; i++) {
            if (livePacmans[i] != null) {
                livePacmans[i].setOpacity(1);
            }
        }
    }

    public void updateScoreDots() {
        List<Position> scores = animator.getPositions("punktefelder_aktuell");
        for(Position p : scoreDots.keySet()) {
            scoreDots.get(p).setOpacity(scores.contains(p) ? 1.0 : 0.0);
        }
    }

    public void updateGhostDots() {
        List<Position> ghosts = animator.getPositions("geister_aktuell");
        for(Position p : ghostDots.keySet()) {
            ghostDots.get(p).setOpacity(ghosts.contains(p) ? 1.0 : 0.0);
        }
    }

    private int toImagePos(int pos) {
        return pos * 10 + 40;
    }

    public void update() {
        updateScoreValue();
        updateGhostDots();
        updateScoreDots();
        updateLives();
        updatePacman();
        for (int i = 0; i < 3; i++) {
            updateGhost(i);
        }
    }
}
