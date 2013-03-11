package dk.itu.mario.level;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.engine.sprites.SpriteTemplate;

public class MyLevel extends Level {
	public static int BLOCKS_EMPTY = 0;
	public static int BLOCKS_POWER = 0;
	public static int BLOCKS_COINS = 0;
	public static int COINS = 0;
	public static int ENEMIES = 0;

	// Store information about the level
	private Random random;
	private int difficulty;
	private int type;
	private GamePlay playerMetrics;
	private int fastestPossibleTime = width / 10;
	private int prevLevelCompTime;

	// Configurations
	private static Map<Configuration, Integer> configMap; // Only static vars for easy access in engine
	private List<Configuration> configs;
	public static TreeMap<Integer, Configuration> configTreeMap;
	private static Map<Configuration, Integer> configFreqMap;

	
	/**
	 * Will take in the coordinate mario died at and find the configuration that the
	 * x coordinate is within range of
	 * @param key The x coordinate mario died at
	 * @return
	 */
	public static Configuration configurationDeath(Integer key) {
		Integer k = key > configTreeMap.firstKey() ? configTreeMap.floorKey(key) : configTreeMap.firstKey();
	    if (k != null ) {
	    	Configuration config = configTreeMap.get(k);
	    	Integer freq = configFreqMap.get(k);
	    	if (freq != null) {
	    		configFreqMap.put(config, ++freq);
	    	}
	    	else {
	    		configFreqMap.put(config, 1);
	    	}
	    	int oldDiff = configMap.get(config);
	    	configMap.put(config, ++oldDiff); // Increase difficulty of config player died to
	        return config;
	    }
	    else {
	    	return null;
	    } 
	}
	
	public MyLevel(int width, int height) {
		super(width, height);
	}

	public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics) {
		this(width, height);
		this.playerMetrics = playerMetrics;
		init(seed, difficulty, type);
		prevLevelCompTime = playerMetrics.getCompletionTime();
		create();
		//System.out.println(playerMetrics);
		
	}

	public void init(long seed, int difficulty, int type) {
		this.type = type;
		this.difficulty = difficulty;
		this.random = new Random(seed);
		this.configMap = getConfigurations();
		this.configs = new ArrayList<>(this.configMap.keySet());
		this.configTreeMap = new TreeMap<Integer, Configuration>();
		if (configFreqMap == null) {
			configFreqMap = new HashMap<Configuration, Integer>();
		}
	}
	
	public void printFreqMap() {
		for (Entry<Configuration, Integer> e: configFreqMap.entrySet()) {
			System.out.print(e.getKey() + " : " + e.getValue() + " ");
		}
		System.out.println();
	}
	public void create() {
		// create the start location
		Point at = new Point(0, 2);

		// width we want to leave safe at the end and beginning
		int cushion = 15;

		at = straight(at, cushion);
		while(at.x < width - cushion) {
			int config = random.nextInt(configs.size());
			configTreeMap.put(at.x, configs.get(config));
			at = configs.get(config).apply(at);
		}

		/*
		 * coordinates of the exit
		 */
		xExit = at.x;
		yExit = y(at.y - 1);
		at = straight(at, width - at.x);
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
	 * @param at the current point location we are looking at on the map
	 * @l the length of the jump
	 * @h the height to increase or decrease by
	 */
	private Point jump(Point at, int l, int h) {
		at.x += l;
		at.y += h;
		return at;
	}
	
	private Point randomJump(Point at) {
		int h = random.nextInt(7) - 3;
		int l = random.nextInt(3) + 1;
		while (at.y + h >= height || at.y + h <= 0) {
			h = random.nextInt(7) - 3;
		}
		at = jump(at, l, h);
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
	 * 
	 * @param at
	 * @param h the height to place the block at
	 * @param type
	 */
	private void block(Point at, int h, byte type) {
		setBlock(at.x, y(at.y+h), type);
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

	public MyLevel clone() throws CloneNotSupportedException {
		//	//System.out.println("Called clone");
		//System.out.println(playerMetrics);
		MyLevel clone = new MyLevel(width, height);

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

	private Map<Configuration, Integer> getConfigurations() {
		Map<Configuration, Integer> configurations = new HashMap<>();

		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "straight";
			}

			@Override
			public Point apply(Point at) {
				int w = random.nextInt(5) + 2;
				return straight(at, w);
			}
		}, 1);

		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "gap";
			}

			@Override
			public Point apply(Point at) {
				at = jump(at, 3, 0);
				return straight(at, 2);
			}
		}, 1);

		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "jump";
			}

			@Override
			public Point apply(Point at) {
				int h = random.nextInt(7) - 3;
				while (at.y + h >= height || at.y + h <= 0) {
					h = random.nextInt(7) - 3;
				}

				at = jump(at, 3, h);
				return straight(at, 2);
			}
		}, 1);

		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "goomba";
			}

			@Override
			public Point apply(Point at) {
				at = straight(at, 2);
				enemy(at, Enemy.ENEMY_GOOMBA, false);
				return straight(at, 2);
			}
		}, 1);

		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "coins";
			}

			@Override
			public Point apply(Point at) {
				at = straight(at, 1);
				int w = random.nextInt(6) + 1;
				int h = random.nextInt(2) + 2;
				if (at.y + h < height) {
					for (int i = 0; i < w; i++) {
						block(at, h, BLOCK_COIN);
						at = straight(at, 1);
					}
				}
				return straight(at, 2);
			}
		}, 1);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "random pipe";
			}

			@Override
			public Point apply(Point at) {
				at = straight(at, 1);
				int h = random.nextInt(2) + 2;
				pipe(at, h, random.nextBoolean());
				return straight(at, 3);
			}
		}, 1);
		
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "cannon";
			}

			@Override
			public Point apply(Point at) {
				at = straight(at, 1);
				int h = random.nextInt(2) + 2;
				cannon(at, h);
				return straight(at, 2);
			}
		}, 1);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "random blocks";
			}

			@Override
			public Point apply(Point at) {
				at = straight(at, 1);
				int w = random.nextInt(6) + 1;
				int h = random.nextInt(2) + 2;
				if (at.y + h < height) {
					for (int i = 0; i < w; i++) {
						int r = random.nextInt(10);
						if (r < 7) {
							block(at, h, BLOCK_COIN);
						} else if (r < 9) {
							block(at, h, BLOCK_EMPTY);
						} else {
							block(at, h, BLOCK_POWERUP);
						}
						at = straight(at, 1);
					}
				}
				return straight(at, 2);
			}
		}, 1);
		
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
		}, 1);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "JumpWithTurtles";
			}
			
			@Override
			public Point apply(Point at) {
				at = jump(at, 4, 0);
				enemy(at, Enemy.ENEMY_GREEN_KOOPA_FLYING, true);
				at = jump(at, 4, 0);
				enemy(at, Enemy.ENEMY_GREEN_KOOPA_FLYING, true);
				at = jump(at, 3, 0);
				at = straight(at, 2);
				return at;
			}
		}, 1);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "multipleCannonJump";
			}

			@Override
			public Point apply(Point at) {
				at = straight(at, 1);
				int h = random.nextInt(2) + 2;
				cannon(at, h);
				at = straight(at, 2);
				h++;
				cannon(at, h);
				at = straight(at, 2);
				h++;
				cannon(at, h);
				at = straight(at, 2);
				at = jump(at, 8, 0);
				return at = straight(at, 2);
			}
		}, 1);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "FloatingBlockway";
			}
			
			@Override
			public Point apply(Point at) {
				at = jump(at, 3, 0);
				int h = random.nextInt(2) + 1;
				for (int i = 0; i < h; i++) {
					block(at, h, BLOCK_EMPTY);
					at = jump(at, 1, 0);
				}
				at = jump(at, 2, 0);
				h = random.nextInt(3) + 1;
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
		}, 1);
		
		configurations.put(new Configuration() {
			@Override
			public String id() {
				return "FloatingBlockwayWithRedTurtles";
			}
			
			@Override
			public Point apply(Point at) {
				at = jump(at, 3, 0);
				int h = random.nextInt(2) + 1;
				for (int i = 0; i < h; i++) {
					at = jump(at, 1, 0);
					block(at, h, BLOCK_EMPTY);
					at = jump(at, 1, 0);
					block(at, h, BLOCK_EMPTY);
				
					if (i%3 == 0) {
						enemy(new Point(at.x, at.y+h+1), Enemy.ENEMY_RED_KOOPA, false);
					}
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
				at = straight(at, 2);
				return at;
			}
		}, 1);
		

		return configurations;
	}

	private abstract class Configuration {

		abstract String id();
		abstract Point apply(Point at);

		@Override
		public int hashCode() {
			return id().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return id().equals(obj);
		}

		@Override
		public String toString() {
			return id();
		}
	}
}
