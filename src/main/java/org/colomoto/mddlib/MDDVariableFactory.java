package org.colomoto.mddlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MDDVariableFactory extends ArrayList<Object> {

	private final Map<Object, Byte> values = new HashMap<Object, Byte>();
	

	/**
	 * Add a multivalued variable to the list.
	 * The default <code>add()</code> method will add a Boolean variable.
	 * 
	 * @param key 		object used as key for the variable
	 * @param nbvalues	number of possible values, must be at least 2
	 * 
	 * @return true if it was added successfully.
	 */
	public boolean add(Object key, byte nbvalues) {
		if (nbvalues < 2) {
			return false;
		}
		
		boolean ret = super.add(key);
		
		if (ret && nbvalues > 2) {
			values.put(key, nbvalues);
		}
		
		return ret;
	}

	public byte getNbValue(Object key) {
		byte ret = 2;
		
		Byte v = values.get(key);
		if (v != null) {
			ret = v.byteValue();
		}
		return ret;
	}

}
