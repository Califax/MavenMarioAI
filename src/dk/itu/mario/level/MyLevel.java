package dk.itu.mario.level;

import java.awt.Point;
import java.util.Random;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.engine.sprites.SpriteTemplate;

public class MyLevel extends Level {
	// Store information about the level
	private Random random;
	private int difficulty;
	private int type;

	public MyLevel(int width, int height) {
		super(width, height);
	}

	public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics) {
		this(width, height);
		init(seed, difficulty, type);
		create();
	}

	public void init(long seed, int difficulty, int type) {
		this.type = type;
		this.difficulty = difficulty;
		this.random = new Random(seed);
	}

	public void create() {
		// create the start location
		Point at = new Point(0, 2);
		while(at.x < 100) {
			if (at.x % 10 == 8) {
				at = jump(at, 1);
				at.y = random.nextInt(4) + 1;
			} else {
				at = straight(at, 1);
			}
			
			if (at.x > 20 && at.x % 21 == 4) {
				pipe(at, 1, random.nextBoolean());
			}
			
			if (at.x % 31 == 0) {
				enemy(at, Enemy.ENEMY_GOOMBA, random.nextBoolean());
			}
		}

		// create all of the medium sections
		//		while (length < width - 64) {
		//			length += buildStraight(length, width - length, false);
		//        	length += buildZone(length, width - length);
		//			length += buildStraight(length, width - length, false);
		//			length += buildHillStraight(length, width - length);
		//			length += buildJump(length, width - length);
		//			length += buildTubes(length, width - length);
		//			length += buildCannons(length, width - length);
		//		}

		// set the end piece
//		int floor = height - 1 - random.nextInt(4);
//		xExit = x + 8;
//		yExit = floor;
//
//		// fills the end piece
//		for (int i = x; i < width; i++) {
//			for (int y = 0; y < height; y++) {
//				if (y >= floor) {
//					setBlock(i, y, GROUND);
//				}
//			}
//		}
//
//		if (type == LevelInterface.TYPE_CASTLE
//				|| type == LevelInterface.TYPE_UNDERGROUND) {
//			int ceiling = 0;
//			int run = 0;
//			for (int i = 0; i < width; i++) {
//				if (run-- <= 0 && i > 4) {
//					ceiling = random.nextInt(4);
//					run = random.nextInt(4) + 4;
//				}
//				for (int y = 0; y < height; y++) {
//					if ((i > 4 && y <= ceiling) || i < 1) {
//						setBlock(i, y, GROUND);
//					}
//				}
//			}
//		}
		
		/*
		 * coordinates of the exit
		 */
		xExit = at.x;
		yExit = y(at.y - 1);
		straight(at, width - at.x);
		fixWalls();
	}

	/**
	 * Build straight section at given point with specified length
	 */
	private Point straight(Point at, int l) {
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < at.y; j++) {
				setBlock(at.x + i, y(j), GROUND);
			}
		}
		at.x += l;
		return at;
	}
	
	/**
	 * Creates a jump of the specified length at the given point
	 */
	private Point jump(Point at, int l) {
		at.x += l;
		at.y = 0;
		return at;
	}

	/**
	 * Buids a pipe at the given point of the height, if flower is true there will be a flower enemy in the pipe
	 * 
	 * @param at
	 * @param h
	 * @param flower
	 */
	private void pipe(Point at, int h, boolean flower) {
		for (int i = 0; i < h; i++) {
			if (i == h - 1) {
				setBlock(at.x, y(at.y + i), TUBE_TOP_LEFT);
				setBlock(at.x + 1, y(at.y + i), TUBE_TOP_RIGHT);
			} else {
				setBlock(at.x, y(at.y + i), TUBE_SIDE_LEFT);
				setBlock(at.x + 1, y(at.y + i), TUBE_SIDE_RIGHT);
			}
		}
		
		if (flower) {
			setSpriteTemplate(at.x, y(at.y + h - 1), new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
		}
	}
	
	/**
	 * Builds a cannon of the specified height at the point
	 */
	private void cannon(Point at, int h) {
		for (int i = 0; i < h; i++) {
			if (i == h - 1) {
				setBlock(at.x, y(at.y + i), CANNON_TOP);
			} else if (i == h - 2) {
				setBlock(at.x, y(at.y + i), CANNON_MID);
			} else {
				setBlock(at.x, y(at.y + i), CANNON_BOT);
			}
		}
	}

	/**
	 * Creates an enemy of the given type at the given point
	 * 
	 * @param at
	 * @param type
	 * @param winged
	 */
	private void enemy(Point at, int type, boolean winged) {
		setSpriteTemplate(at.x, y(at.y), new SpriteTemplate(type, winged));
	}

	/**
	 * Converts the given height into a y coordinate
	 * 
	 * @param h
	 * @return
	 */
	private int y(int h) {
		return height - 1 - h;
	}
	
	/*
	 * Their code...
	 */
	
	private void fixWalls() {
		boolean[][] blockMap = new boolean[width + 1][height + 1];

		for (int x = 0; x < width + 1; x++) {
			for (int y = 0; y < height + 1; y++) {
				int blocks = 0;
				for (int xx = x - 1; xx < x + 1; xx++) {
					for (int yy = y - 1; yy < y + 1; yy++) {
						if (getBlockCapped(xx, yy) == GROUND) {
							blocks++;
						}
					}
				}
				blockMap[x][y] = blocks == 4;
			}
		}
		blockify(this, blockMap, width + 1, height + 1);
	}

	private void blockify(Level level, boolean[][] blocks, int width, int height) {
		int to = 0;
		if (type == LevelInterface.TYPE_CASTLE) {
			to = 4 * 2;
		} else if (type == LevelInterface.TYPE_UNDERGROUND) {
			to = 4 * 3;
		}

		boolean[][] b = new boolean[2][2];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int xx = x; xx <= x + 1; xx++) {
					for (int yy = y; yy <= y + 1; yy++) {
						int _xx = xx;
						int _yy = yy;
						if (_xx < 0)
							_xx = 0;
						if (_yy < 0)
							_yy = 0;
						if (_xx > width - 1)
							_xx = width - 1;
						if (_yy > height - 1)
							_yy = height - 1;
						b[xx - x][yy - y] = blocks[_xx][_yy];
					}
				}

				if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
					if (b[0][0] == b[0][1]) {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
						} else {
							// KEEP OLD BLOCK!
						}
					} else {
						if (b[0][0]) {
							// down grass top?
							level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
						} else {
							// up grass top
							level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
						}
					}
				} else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
					if (b[0][0]) {
						// right grass top
						level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
					} else {
						// left grass top
						level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
					}
				} else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
					level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
				} else if (b[0][0] == b[1][0]) {
					if (b[0][0]) {
						if (b[0][1]) {
							level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
						}
					} else {
						if (b[0][1]) {
							// right up grass top
							level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
						} else {
							// left up grass top
							level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
						}
					}
				} else if (b[0][1] == b[1][1]) {
					if (b[0][1]) {
						if (b[0][0]) {
							// left pocket grass
							level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
						} else {
							// right pocket grass
							level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
						}
					} else {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
						}
					}
				} else {
					level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
				}
			}
		}
	}

	public Level clone() throws CloneNotSupportedException {

		Level clone = new Level(width, height);

		clone.xExit = xExit;
		clone.yExit = yExit;
		byte[][] map = getMap();
		SpriteTemplate[][] st = getSpriteTemplate();

		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				clone.setBlock(i, j, map[i][j]);
				clone.setSpriteTemplate(i, j, st[i][j]);
			}
		}

		return clone;

	}
}
