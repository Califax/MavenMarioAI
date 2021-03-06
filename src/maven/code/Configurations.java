package maven.code;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.level.Level;
import dk.itu.mario.level.MyLevel;

public class Configurations {

	/*
	 * Singleton, ewww.
	 */

	private Set<Configuration> configs;

	private Configurations() {
		configs = new HashSet<>();

		// Register the configurations here
//		configs.add(powerUp());
		configs.add(spikeyFloor());
		configs.add(straight());
		configs.add(blockway());
		configs.add(flyingTurtleJump());
		configs.add(jump());
		configs.add(goomba());
		configs.add(hill());
		configs.add(hills());
		configs.add(elevation());
		configs.add(coins());
		configs.add(blocks());
		configs.add(cave());
		configs.add(pipe(true));
		configs.add(pipe(false));
		configs.add(cannon());
		configs.add(jumpSpike());
	}

	private static Configurations instance;
	private static Configurations instance() {
		if (instance == null) {
			instance = new Configurations();
		}
		return instance;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/*
	 * Static
	 */

	private static Random random = new Random();
	private static int id = 0;

	public static Set<Configuration> configs() {
		return instance().configs;
	}

	public static Configuration cave() {
		return new Configuration(id++) {

			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.straight(at, 2);
				int h = random.nextInt(5) + 3;
				if (at.y + h < level.getHeight()) {
					int w = random.nextInt(5) + 8;
					at.y += h;
					level.ceiling(at, w);
					at.y -= h;
					at.x -= w;

					at.y += 1;
					for (int i = 0; i < w / 2; i++) {
						level.coin(at);
						at.x += 2;
					}
					at.x -= (w / 2) * 2;
					at.y -= 1;

					level.straight(at, w);
				}

				return level.straight(at, 3);
			}
		};
	}

	public static Configuration straight() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				return level.straight(at, random.nextInt(5) + 2);
			}
		};
	}

	public static Configuration jump() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				int h = random.nextInt(7) - 3;
				while (at.y + h >= level.getHeight() || at.y + h <= 0) {
					h = random.nextInt(7) - 3;
				}

				at = level.jump(at, 3, h);
				return level.straight(at, 2);
			}
		};
	}

	public static Configuration goomba() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.straight(at, 2);
				level.enemy(at, Enemy.ENEMY_GOOMBA, false);
				return level.straight(at, 2);
			}
		};
	}

	public static Configuration hill() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				int h = random.nextInt(2) + 3;
				int w = random.nextInt(5) + 5;

				at.x++;
				level.hill(at, w, h);
				at.x -= w;
				at.x--;

				return level.straight(at, w + 2);
			}
		};
	}

	public static Configuration hills() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {

				int hill1 = random.nextInt(6) + 2;
				int hill2 = random.nextInt(6) + 2;

				int height1 = random.nextInt(4) + 2;
				int height2 = random.nextInt(6) + 2;
				while(height1 == height2) {
					height2 = random.nextInt(6) + 2;
				}

				int width1 = random.nextInt(4) + 3;
				int width2 = random.nextInt(4) + 3;

				/*
				 * The next 4 conditions handle weird looking hills
				 */
				if (hill1 == hill2) {
					if (height1 < height2) {
						hill1--;
					} else {
						hill2--;
					}
				}

				if (hill1 + width1 == hill2) {
					hill2--;
				}

				if (hill2 + width2 == hill1) {
					hill1--;
				}

				if (hill1 + width1 == hill2 + width2) {
					if (hill1 < hill2) {
						width2++;
					} else {
						width1++;
					}
				}

				/*
				 * Always want to draw the taller hill first, so we swap stuff!
				 */
				if (height1 < height2) {
					int tmp = hill1;
					hill1 = hill2;
					hill2 = tmp;

					tmp = height1;
					height1 = height2;
					height2 = tmp;

					tmp = width1;
					width1 = width2;
					width2 = tmp;
				}

				at.x += hill1;
				at = level.hill(at, width1, height1);
				at.x -= width1;
				at.x += width1 / 2;
				if (random.nextBoolean()) {
					level.enemy(at, Enemy.ENEMY_GREEN_KOOPA, false);
					at.y += height1;
					level.enemy(at, Enemy.ENEMY_RED_KOOPA, false);
					at.y -= height1;
				} else {
					level.enemy(at, Enemy.ENEMY_GREEN_KOOPA, false);
					at.y += height1;
					level.enemy(at, Enemy.ENEMY_RED_KOOPA, false);
					at.y -= height1;
				}
				at.x -= width1/2;
				at.x -= hill1;

				at.x += hill2;
				at = level.hill(at, width2, height2);
				at.x -= width2;
				at.x -= hill2;

				int width = Math.max(hill1 + width1, hill2 + width2) + 1;
				return level.straight(at, width);
			}
		};
	}

	public static Configuration elevation() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				int w = random.nextInt(4) + 2;
				int d = random.nextInt(7) - 3;
				while (at.y + d <= 0 || at.y + d >= level.getHeight()) {
					d = random.nextInt(7) - 3;
				}

				at.y += d;
				at = level.straight(at, w);

				return at;
			}
		};
	}

	public static Configuration coins() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.straight(at, 1);
				int w = random.nextInt(6) + 1;
				int h = random.nextInt(2) + 2;
				if (at.y + h < level.getHeight()) {
					for (int i = 0; i < w; i++) {
						level.block(at, h, Level.BLOCK_COIN);
						at = level.straight(at, 1);
					}
				}
				return level.straight(at, 2);
			}
		};
	}

	public static Configuration blocks() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.straight(at, 1);
				int w = random.nextInt(6) + 1;
				int h = random.nextInt(2) + 2;
				if (at.y + h < level.getHeight()) {
					for (int i = 0; i < w; i++) {
						int r = random.nextInt(10);
						if (r < 7) {
							// 70% of the time coins
							level.block(at, h, Level.BLOCK_COIN);
						} else if (r < 9) {
							// 20% of the time empty
							level.block(at, h, Level.BLOCK_EMPTY);
						} else {
							// 10% of the time powerup
							level.block(at, h, Level.BLOCK_POWERUP);
						}
						at = level.straight(at, 1);
					}
				}
				return level.straight(at, 2);
			}
		};
	}

	public static Configuration pipe(final boolean flower) {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.straight(at, 1);
				int h = random.nextInt(2) + 2;
				level.pipe(at, h, flower);
				return level.straight(at, 3);
			}
		};
	}

	public static Configuration cannon() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.straight(at, 1);
				int h = random.nextInt(2) + 2;
				level.cannon(at, h);
				return level.straight(at, 2);
			}
		};
	}

	public static Configuration blockway() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.straight(at, 1);
				level.cannon(at, 2);
				at = level.straight(at, 2);

				for (int i = 0; i < 5; i++) {
					if (i == 0) {
						level.enemy(new Point(at.x, at.y), Enemy.randomEnemy(), false);
					}
					
					level.block(at, -1, Level.ROCK);
					at = level.jump(at, 1, 0);
				}

				at = level.straight(at, 1);
				level.cannon(at, 2);
				return level.straight(at, 2);
			}
		};
	}

	public static Configuration flyingTurtleJump() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				at = level.jump(at, 2, 0);
				level.enemy(at, Enemy.ENEMY_GREEN_KOOPA_FLYING, true);
				at =  level.jump(at, 3, 0);
				level.enemy(at, Enemy.ENEMY_GREEN_KOOPA_FLYING, true);
				at =  level.jump(at, 2, 0);
				at =  level.straight(at, 2);
				return at;
			}
		};
	}	

	public static Configuration jumpSpike() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				int h = random.nextInt(7) - 3;
				while (at.y + h >= level.getHeight() || at.y + h <= 0) {
					h = random.nextInt(7) - 3;
				}

				at = level.jump(at, 3, h);
				at = level.straight(at, 3);
				level.enemy(at, Enemy.ENEMY_SPIKY, true);
				return at;
			}
		};
	}	

	public static Configuration spikeyFloor() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				level.block(at, 0, Level.ROCK);
				at = level.straight(at, 3);
				for (int i = 0; i < 8; i++) {
					at = level.straight(at, 1);
					if (i == 0) {
						level.enemy(at, Enemy.ENEMY_RED_KOOPA, false);
					} else if (i > 2){
						level.enemy(at, Enemy.ENEMY_SPIKY, false);
					}
				}
				return level.straight(at, 1);
			}
		};
	}
	
	public static Configuration powerUp() {
		return new Configuration(id++) {
			@Override
			public Point apply(Point at, MyLevel level) {
				int h = 3;
				at.y += h;
				at = level.ceiling(at, 2);
				at.y -= h;
				at.x -= 2;
				at = level.straight(at, 4);
				at.y += h;
				at = level.island(at, 2, level.getHeight() - at.y - 3);
				at.y -= h;
				at.x -= 2;
				
				level.block(at, level.getHeight() - at.y - 1, Level.BLOCK_POWERUP);
				
				return level.straight(at, 4);
			}
		};
	}
	
}

