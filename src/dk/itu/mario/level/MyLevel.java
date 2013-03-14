package dk.itu.mario.level;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import maven.code.Configuration;
import maven.code.Configurations;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.engine.sprites.SpriteTemplate;

public class MyLevel extends Level {
	
	// Data for DataRecorder
	public int BLOCKS_EMPTY = 0;
	public int BLOCKS_COINS = 0;
	public int BLOCKS_POWER = 0;
	public int COINS = 0;
	public int ENEMIES = 0;
	
	// Store information about the level
	private Random random;
	private int difficulty;
	private int type;
	
	private GamePlay playerMetrics;
	private int fastestPossibleTime = width / 10;
	private int prevLevelCompTime;

	// Static to persist over multiple levels
	private static Map<Configuration, Integer> deathCount = new HashMap<>();
	
	private List<Configuration> configs;
	private TreeMap<Integer, Configuration> configTreeMap;

	public MyLevel(int width, int height) {
		this(width, height, 4731L, 0, LevelInterface.TYPE_OVERGROUND, new GamePlay());
	}

	public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics) {
		super(width, height);
		
		this.playerMetrics = playerMetrics;
		
		// Hills don't play nicely with other types.
		this.type = LevelInterface.TYPE_OVERGROUND;
		
		this.difficulty = difficulty;
		this.random = new Random(seed);
		this.configs = new ArrayList<>(Configurations.configs());
		this.configTreeMap = new TreeMap<Integer, Configuration>();
		this.prevLevelCompTime = playerMetrics.getCompletionTime();
		
		createLevel();
	}

	/**
	 * Will take in the coordinate mario died at and find the configuration that the
	 * x coordinate is within range of
	 * 
	 * @param x The x coordinate mario died at
	 * @return
	 */
	public Configuration death(int x) {
	    Integer key = configTreeMap.floorKey(x);
	    if (key == null) {
	    	return null;
	    } else {
	    	Configuration c = configTreeMap.get(key);
	    	if (!deathCount.containsKey(c)) {
	    		deathCount.put(c, 0);
	    	}
	    	deathCount.put(c, deathCount.get(c) + 1);
	    	return c;
	    }
	}
	
	public void createLevel() {
		Point at = new Point(0, 2);

		// width we want to leave safe at the end and beginning
		int cushion = 15;

		int sum = configs.size();
		for (int deaths : deathCount.values()) {
			sum += deaths;
		}
		
		int limit = sum / 6;
		for (Configuration c : deathCount.keySet()) {
			if (deathCount.get(c) > limit) {
				sum -= deathCount.get(c) + limit;
				deathCount.put(c, limit);
			}
		}
		
		at = straight(at, cushion);
		while(at.x < width - cushion) {
			int config = random.nextInt(sum);
			Configuration choice = null;
			for (Configuration c : configs) {
				
				config -= 1;
				if (deathCount.containsKey(c)) {
					config -= deathCount.get(c);
				}
				
				if (config <= 0) {
					choice = c;
					break;
				}
			}
			
			if (choice == null) continue;
			
			configTreeMap.put(at.x, choice);
			at = choice.apply(at, this);
		}

		/*
		 * coordinates of the exit
		 */
		at = straight(at, width - at.x - 5);
		xExit = at.x;
		yExit = y(at.y - 1);
		at = straight(at, width - at.x);
		fixWalls();
	}

	/**
	 * Build straight section at given point with specified length
	 */
	public Point straight(Point at, int l) {
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < at.y; j++) {
				setBlock(at.x + i, y(j), GROUND);
			}
		}
		at.x += l;
		return at;
	}
	
	/**
	 * Builds a hill
	 */
	public Point hill(Point at, int w, int h) {
		
		if (at.y + h >= height) {
			h = height - 1 - at.y;
		}
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (j < h - 1) {
					if (i == 0) {
						setBlock(at.x + i, y(at.y + j), HILL_LEFT);
					} else if (i == w - 1) {
						setBlock(at.x + i, y(at.y + j), HILL_RIGHT);
					} else {
						setBlock(at.x + i, y(at.y + j), HILL_FILL);
					}
				} else if (i == 0) {
					if (getBlock(at.x + i, y(at.y + j)) == HILL_FILL) {
						setBlock(at.x + i, y(at.y + j), HILL_TOP_LEFT_IN);
					} else {
						setBlock(at.x + i, y(at.y + j), HILL_TOP_LEFT);
					}
				} else if (i == w - 1) {
					if (getBlock(at.x + i, y(at.y + j)) == HILL_FILL) {
						setBlock(at.x + i, y(at.y + j), HILL_TOP_RIGHT_IN);
					} else {
						setBlock(at.x + i, y(at.y + j), HILL_TOP_RIGHT);
					}
				} else {
					setBlock(at.x + i, y(at.y + j), HILL_TOP);
				}
			}
		}
		
		at.x += w;
		return at;
	}

	/**
	 * Creates a jump of the specified length at the given point
	 * @param at the current point location we are looking at on the map
	 * @l the length of the jump
	 * @h the height to increase or decrease by
	 */
	public Point jump(Point at, int l, int h) {
		at.x += l;
		at.y += h;
		return at;
	}
	
	/**
	 * Buids a pipe at the given point of the height, if flower is true there will be a flower enemy in the pipe
	 * 
	 * @param at
	 * @param h
	 * @param flower
	 */
	public void pipe(Point at, int h, boolean flower) {
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
	public void cannon(Point at, int h) {
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
	public void enemy(Point at, int type, boolean winged) {
		setSpriteTemplate(at.x, y(at.y), new SpriteTemplate(type, winged));
	}

	/**
	 * 
	 * @param at
	 * @param h the height to place the block at
	 * @param type
	 */
	public void block(Point at, int h, byte type) {
		setBlock(at.x, y(at.y+h), type);
	}


	/**
	 * Converts the given height into a y coordinate
	 * 
	 * @param h
	 * @return
	 */
	public int y(int h) {
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

	public MyLevel clone() throws CloneNotSupportedException {
		MyLevel clone = new MyLevel(width, height);

		clone.BLOCKS_COINS = BLOCKS_COINS;
		clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
		clone.BLOCKS_POWER = BLOCKS_POWER;
		clone.COINS = COINS;
		clone.ENEMIES = ENEMIES;
		
		clone.configs = configs;
		clone.configTreeMap = configTreeMap;
		clone.difficulty = difficulty;
		clone.fastestPossibleTime = fastestPossibleTime;
		clone.playerMetrics = playerMetrics;
		clone.prevLevelCompTime = prevLevelCompTime;
		clone.random = random;
		clone.type = type;
		
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

	/*
	 * Configurations
	 */

	/*
	private Map<Configuration, Integer> getConfigurations() {
		Map<Configuration, Integer> configurations = new HashMap<>();

		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "JumpThenSpiky";
			}
			
			@Override
			public Point apply(Point at) {
				at = randomJump(at);
				at = straight(at, 3);
				enemy(at, Enemy.ENEMY_SPIKY, true);
				return at;
			}
		}, medium);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "JumpWithTurtles";
			}
			
			@Override
			public Point apply(Point at) {
				at = jump(at, 3, 0);
				enemy(at, Enemy.ENEMY_GREEN_KOOPA_FLYING, true);
				at = jump(at, 3, 0);
				enemy(at, Enemy.ENEMY_GREEN_KOOPA_FLYING, true);
				at = jump(at, 3, 0);
				at = straight(at, 2);
				return at;
			}
		}, medium);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "multipleCannonJump";
			}

			@Override
			public Point apply(Point at) {
				at = straight(at, 2);
				int h = random.nextInt(2) + 2;
				cannon(at, h);
				at = straight(at, 2);
				h++;
				cannon(at, h);
				at = straight(at, 2);
				h++;
				cannon(at, h);
				at = straight(at, 2);
				at = jump(at, 6, 0);
				return at = straight(at, 2);
			}
		}, medium);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "FloatingBlockway";
			}
			
			@Override
			public Point apply(Point at) {
				at = jump(at, 2, 0);
				int h = random.nextInt(3) + 1;
				for (int i = 0; i < h; i++) {
					block(at, h, BLOCK_EMPTY);
					at = jump(at, 1, 0);
					block(at, h+random.nextInt(2)+1, BLOCK_EMPTY);
				}
				at = jump(at, 2, 0);
				h = random.nextInt(4) + 1;
				for (int i = 0; i < h; i++) {
					block(at, h, BLOCK_EMPTY);
					at = jump(at, 1, 0);
				}
				at = jump(at, 2, 0);
				enemy(at, Enemy.ENEMY_GREEN_KOOPA_FLYING, true);
				at = jump(at, 3, 0);
				at = straight(at, 2);
				return at;
			}
		}, mediumRare);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "FloatingBlockwayWithRedTurtles";
			}
			
			@Override
			public Point apply(Point at) {
				at = jump(at, 2, 0);
				int h = random.nextInt(2) + 1;
				for (int i = 0; i < h; i++) {
					at = jump(at, 1, 0);
					block(at, h, BLOCK_EMPTY);
					at = jump(at, 1, 0);
					block(at, h, BLOCK_EMPTY);
				}
				
				at = jump(at, 2, 0);
				h = random.nextInt(4);
				for (int i = 0; i < h; i++) {
					at = jump(at, 1, 0);
					block(at, h, BLOCK_EMPTY);
					if (i%2 == 0) {
						enemy(new Point(at.x, at.y+h+1), Enemy.ENEMY_RED_KOOPA, false);
					}
				}
				at = straight(at, 3);
				return at;
			}
		}, medium);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "FloatingNarrowBlockway";
			}
			
			@Override
			public Point apply(Point at) {
				int high = 3;
				int low = -1;
				int l = 9;
				at = straight(at, 3);
				block(at, high, BLOCK_COIN);
				cannon(at, 2);
				at = straight(at, 1);
				block(at, high, BLOCK_EMPTY);
				at = straight(at, 1);
				for (int i = 0; i < l; i++) {
					block(at, high, BLOCK_EMPTY);
					if (i%3 == 0) {
						enemy(new Point(at.x, at.y+1), Enemy.ENEMY_GOOMBA, false);
					}
					block(at, low, BLOCK_EMPTY);
					at = jump(at, 1, 0);
				}
				block(at, high, BLOCK_EMPTY);
				at = straight(at, 1);
				block(at, high, BLOCK_EMPTY);
				at = straight(at, 1);
				block(at, high, BLOCK_POWERUP);
				cannon(at, 2);
				at = straight(at, 2);
				return at;
			}
		}, medium);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "FloatingNarrowBlockwayWithSpikey";
			}
			
			@Override
			public Point apply(Point at, MyLevel level) {
				int high = 3;
				int low = -1;
				int l = 6;
				at = straight(at, 3);
				block(at, high, BLOCK_EMPTY);
				cannon(at, 2);
				at = straight(at, 1);
				block(at, high, BLOCK_EMPTY);
				at = straight(at, 1);
				for (int i = 0; i < l; i++) {
					block(at, high, BLOCK_COIN);
					if (i%3 == 0) {
						enemy(new Point(at.x, at.y+1), Enemy.ENEMY_SPIKY, false);
					}
					block(at, low, BLOCK_EMPTY);
					at = jump(at, 1, 0);
				}
				block(at, high, BLOCK_EMPTY);
				at = straight(at, 1);
				block(at, high, BLOCK_EMPTY);
				at = straight(at, 1);
				block(at, high, BLOCK_EMPTY);
				cannon(at, 2);
				at = straight(at, 2);
				return at;
			}
		}, medium);
		

		return configurations;
	}
	*/

}
