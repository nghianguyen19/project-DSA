	package DSAProject.Minesweeper.Game;

	import java.awt.Graphics;
	import java.util.*;

	import DSAProject.Minesweeper.Game.States.CellStates;
	import DSAProject.Minesweeper.Game.States.GameStates;
	import DSAProject.Minesweeper.GFX.Assets;

	public class Board {
		private int N;
		private int NMines;
		private int NCovered;

		private GameStates gameState;

		private boolean[][] isMine;
		private int[][] mineCnt;
		private CellStates[][] states;

		private final int[] di = new int[]{-1, -1, -1, 0, 1, 1, 1, 0};
		private final int[] dj = new int[]{-1, 0, 1, 1, 1, 0, -1, -1};

		private final CellStates[] uncoveredStates = new CellStates[]{
				CellStates.UNC0, CellStates.UNC1, CellStates.UNC2, CellStates.UNC3,
				CellStates.UNC4, CellStates.UNC5, CellStates.UNC6, CellStates.UNC7, CellStates.UNC8
		};

		// Stack to track move history for undo
		private Stack<Move> moveHistory;

		public Board(int N, int NMines) {
			if (N < 10 || N > 1000 || NMines < 1 || NMines > N * N) {
				N = 30;
				NMines = 100;
			}

			this.N = N;
			this.NCovered = N * N;
			this.NMines = NMines;

			isMine = new boolean[N][N];
			mineCnt = new int[N + 2][N + 2];
			states = new CellStates[N][N];

			moveHistory = new Stack<>(); // Initialize move history stack

			putMines();

			for (int i = 0; i < N; i++) {
				Arrays.fill(states[i], CellStates.COVERED);
			}

			gameState = GameStates.ONGOING;
		}

		private void putMines() {
			Random rand = new Random();
			int mines = NMines;
			while (mines-- > 0) {
				int pos = rand.nextInt(NCovered);
				int x = pos % N;
				int y = pos / N;
				if (isMine[y][x])
					mines++;
				else {
					isMine[y][x] = true;
					for (int d = 0; d < di.length; d++) {
						mineCnt[y + di[d] + 1][x + dj[d] + 1]++;
					}
				}
			}
		}

		private void uncoverAll(Graphics g, boolean won) {
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					if (states[i][j] == CellStates.COVERED && isMine[i][j]) {
						states[i][j] = won ? CellStates.FLAGGED : CellStates.MINE;
						Assets.draw(i, j, states[i][j], g);
					} else if (states[i][j] == CellStates.FLAGGED && !isMine[i][j]) {
						states[i][j] = CellStates.WRONG_FLAG;
						Assets.draw(i, j, states[i][j], g);
					}
				}
			}
		}

		private void bfs(int row, int col, Graphics g) {
			Queue<Integer> q = new ArrayDeque<>();
			Set<Integer> visited = new HashSet<>();

			NCovered++;
			q.add(row * N + col);
			visited.add(row * N + col);

			while (!q.isEmpty()) {
				int r = q.peek() / N;
				int c = q.poll() % N;

				if (states[r][c] != CellStates.COVERED)
					continue;

				states[r][c] = uncoveredStates[mineCnt[r + 1][c + 1]];
				Assets.draw(r, c, states[r][c], g);
				NCovered--;

				if (states[r][c] != CellStates.UNC0)
					continue;

				for (int i = 0; i < di.length; i++) {
					int _r = r + di[i];
					int _c = c + dj[i];
					int key = _r * N + _c;
					if (_r < 0 || _r >= N || _c < 0 || _c >= N || visited.contains(key))
						continue;
					q.add(key);
					visited.add(key);
				}
			}

			if (NCovered == NMines)
				gameState = GameStates.WON;
		}

		public boolean uncoverCell(int row, int col, Graphics g) {
			if (states[row][col] != CellStates.COVERED)
				return false;
			if (isMine[row][col]) {
				gameState = GameStates.LOST;
				uncoverAll(g, false);
				states[row][col] = CellStates.FIRED_MINE;
				Assets.draw(row, col, CellStates.FIRED_MINE, g);
			} else {
				NCovered--;
				states[row][col] = uncoveredStates[mineCnt[row + 1][col + 1]];
				Assets.draw(row, col, states[row][col], g);

				moveHistory.push(new Move(row, col, CellStates.COVERED));

				if (NCovered == NMines) {
					gameState = GameStates.WON;
					uncoverAll(g, true);
				} else
					bfs(row, col, g);
			}
			g.dispose();
			return true;
		}

		public void toggleFlag(int row, int col, Graphics g) {
			if (states[row][col] == CellStates.COVERED) {
				states[row][col] = CellStates.FLAGGED;
				moveHistory.push(new Move(row, col, CellStates.COVERED));
			} else if (states[row][col] == CellStates.FLAGGED) {
				states[row][col] = CellStates.COVERED;
				moveHistory.push(new Move(row, col, CellStates.FLAGGED));
			}

			Assets.draw(row, col, states[row][col], g);
			g.dispose();
		}

		public boolean undo(Graphics g) {
			if (moveHistory.isEmpty())
				return false;

			Move lastMove = moveHistory.pop();
			states[lastMove.row][lastMove.col] = lastMove.previousState;
			Assets.draw(lastMove.row, lastMove.col, lastMove.previousState, g);

			if (gameState != GameStates.ONGOING) {
				hideAllBombs(g);
				gameState = GameStates.ONGOING;
			}

			NCovered++;

			return true;
		}
		private void hideAllBombs(Graphics g) {
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					if (states[i][j] == CellStates.MINE || states[i][j] == CellStates.FIRED_MINE) {
						states[i][j] = CellStates.COVERED;
						Assets.draw(i, j, CellStates.COVERED, g);
					}
				}
			}
		}

		public GameStates getGameState() {
			return gameState;
		}

		public void coverCell(int row, int col, Graphics g) {
		}

		// Inner class to store move details
		private static class Move {
			private final int row, col;
			private final CellStates previousState;

			public Move(int row, int col, CellStates previousState) {
				this.row = row;
				this.col = col;
				this.previousState = previousState;
			}

			public int getRow() {
				return row;
			}

			public int getCol() {
				return col;
			}

			public CellStates getPreviousState() {
				return previousState;
			}
		}
	}
