package fr.univmrs.tagc.javaMDD;

/**
 * Definition of a variable that can be used for MDDFactory.
 * <p>
 * A variable has a name, an optional associated object and set of possible values.
 * For the sake of simplicity, only the number of possible values is defined,
 * the factory will use the integer range [0..nbval[.
 * <p>
 * A Boolean variable uses 2 values and will be mapped to the [0,1] interval.
 * <br>A ternary variable uses 3 values and will be mapped to the [0,2] interval.
 * <p>
 * If your variable can take another set of values, it is your responsibility to map
 * them to the [0..nbval[ interval.
 * 
 * @author Aurelien Naldi
 */
public class MDDVariable {
	public final String name;
	public final Object key;
	final byte nbval;
	final byte spanbloc;
	
	public MDDVariable (String name) {
		this(name, 2);
	}
	public MDDVariable (String name, int nbval) {
		this.key = name;
		this.name = name;
		this.nbval = (byte)nbval;
		this.spanbloc = 0;
	}
	public MDDVariable (Object key, String name, int nbval) {
		this.key = key;
		this.name = name;
		this.nbval = (byte)nbval;
		this.spanbloc = 0;
	}
}