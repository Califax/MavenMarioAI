package maven.code;

import java.awt.Point;
import java.util.Set;

import dk.itu.mario.level.MyLevel;

public abstract class Configuration {

	public abstract Point apply(Point at, MyLevel level);
	
	public final int id;
	
	public Configuration(int id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Configuration) {
			return id == ((Configuration) obj).id;
		}
		return false;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
	
}
