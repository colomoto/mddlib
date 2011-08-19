package fr.univmrs.tagc.javaMDD;

/**
 * A MDD factory is responsible for the creation, storage, retrieval, combination and cleanup
 * of a collection of multi-valued decision diagrams (MDDs).
 * <p>
 * <br>The factory has:
 * <ul>
 *  <li> a set of leaves, defined from start and that can not be changed,</li>
 *  <li> a list of variables. New ones can be added afterwards, and the
 *    names can be changed but changing max values resets the factory</li>
 * </ul>
 * <p>
 * A number of leaves are defined when creating the factory. They are numbered
 * <code>[0...nbleaves[</code>. Negative leaves or holes in the values are not allowed, but you
 * are free to map indices to the values of your choice (including inside custom operators).
 * <br>
 * The same leaf can thus refer to different values, depending on the context.
 * <p>
 * Nodes can be created using the get_bnode and get_mnode methods 
 * (for Boolean and multi-valued nodes respectively), which create
 * them if needed and return the bloc ID.
 * <p>
 * Nodes can also be created by combining existing nodes, using the merge method.
 * This method uses MDDOperations to decide how to do the merging.
 * MDDOperation for the common operations are provided (see MDDOperation.ACTION_*),
 * but it was designed to help creating custom ones.
 * A group-merging method allows to efficiently merge many nodes using the same operation.
 * <p>
 * The content is stored in a large integer array, divided into blocs.
 * Each bloc denotes a MDD node, providing its level and list of children.
 * To avoid duplication, a hashmap-like structure allows to find existing
 * nodes quickly.
 * <p>
 * Data blocs contain a reference counter, which allows to reuse the space
 * when nodes are no longer used. For this, call the free method with a node ID
 * when you stop using it. Make sure to forget the ID as any further use would
 * result in unexpected results.
 * 
 * @author Aurelien Naldi
 */
public class MDDFactory {

	private static final int DEFAULT_CAPACITY  = 100;
	private static final int DEFAULT_HASHITEMS = 20;

	private static final int FILL_LIMIT = 80;
	
	private static final int INC_COUNT = 1;
	private static final int INC_VALUES = 2;
	
	private static final int[] NOTFLIP = {1,0};
	

	/* Temporary switches to enable/disable free (seems to be still buggy) */
	private static final boolean CANFREE=true;
	private static final boolean CANFREEHASH=CANFREE;
	
	private MultiValuedVariable[] variables;
	private int blocsize;

	private int[] hashcodes;
	private int[] hashitems;
	private int[] blocs;
	
	// starting point for free blocs/item chained lists
	private int freeBloc = -1;
	private int freeItem = -1;

	// first free position at the end of data/hashitems arrays
	private int lastitem = 0;
	private int lastbloc = 0;

	private int nbnodes = 0;
	private final int nbleaves;

	/**
	 * Create a new MDDFactory using the default capacity.
	 * 
	 * @param variables		the list of variables that can be used.
	 * @param nbleaves		the number of values that can be reached.
	 * 
	 * @see #MDDFactory(int, MultiValuedVariable[], int)
	 */
	public MDDFactory(MultiValuedVariable[] variables, int nbleaves) {
		this(DEFAULT_CAPACITY, variables, nbleaves);
	}
	
	/**
	 * Create a new MDDFactory.
	 * 
	 * @param capacity		number of nodes that can be stored in the initially reserved space.
	 * @param variables		the list of variables that can be used.
	 * @param nbleaves		the number of values that can be reached.
	 */
	public MDDFactory(int capacity, MultiValuedVariable[] variables, int nbleaves) {
		this.variables = variables;
		this.nbleaves = nbleaves;
		blocsize = 2;
		for (MultiValuedVariable var: variables) {
			if (var.nbval > blocsize ) {
				blocsize  = var.nbval;
			}
		}
		blocsize += INC_VALUES;  // add INC_VALUES cells in the bloc for metadata (type, usage count)
		
		hashcodes = new int[capacity*2];
		hashitems = new int[DEFAULT_HASHITEMS];
		reset_hash();

		lastbloc = nbleaves;
		blocs = new int[nbleaves + capacity*blocsize];
		for (int i=0 ; i<nbleaves ; i++) {
			blocs[i] = i;
		}
	}
	
	/**
	 * Change the definition of variables for this MDD factory.
	 * It can be used to add new variables or change variable names,
	 * without loosing the content of the factory.
	 * If variables are removed or the max values of existing variables
	 * is changed, the factory will be invalidated. 
	 * 
	 * @param newVariables
	 */
	public void setVariables(MultiValuedVariable[] newVariables) {
		MultiValuedVariable[] oldVariables = variables;
		this.variables = newVariables;
		if (variables.length < oldVariables.length) {
			invalidate();
			return;
		}
		for (int i=0 ; i<oldVariables.length ; i++) {
			if (oldVariables[i].nbval != variables[i].nbval) {
				invalidate();
				return;
			}
		}
	}
	
	/**
	 * @return the list of variables that can be used in this factory.
	 */
	public MultiValuedVariable[] getVariables() {
		return variables;
	}
	
	/**
	 * 
	 * @param o
	 * @return the ID of this variable in the factory, or -1 of not found
	 */
	public int getVariableID(Object o) {
		// TODO: make getVariableID faster if needed
		//MDDVariable var = m_key2variable.get(o);
		for (int i=0 ; i<variables.length ; i++) {
			if (variables[i].key == o) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Debug helper: print a MDD on standard output.
	 * @param node
	 */
	public void print(int node) {
		print(node, "");
	}
	
	private void print(int node, String prefix) {
		if (node<nbleaves) {
			System.out.println(prefix+node);
			return;
		}
		MultiValuedVariable var = variables[blocs[node]];
		System.out.println(prefix+var.name);
		prefix += "   ";
		for (int i=0 ; i<var.nbval ; i++) {
			print(blocs[node+INC_VALUES+i], prefix);
		}
	}
	
	/**
	 * Invalidate the factory: destroy all content and warn factory listeners
	 * that they should not keep pointers to existing nodes.
	 * <p>
	 * FIXME: invalidate is not yet implemented.
	 */
	private void invalidate() {
		System.err.println("invalidate MDDFactory not yet implemented");
	}
	
	/**
	 * get a boolean node. It will be created if needed
	 * The resulting ID should be freed when it stopped being used. 
	 * 
	 * @param var ID of the variable
	 * @param lchild left child (false)
	 * @param rchild right child (true)
	 * @return the ID of the node
	 */
	public int get_bnode(int var, int lchild, int rchild) {
		if (lchild == rchild) {
			return use(lchild);
		}
		int hash = compute_bhash(var, lchild, rchild);
		boolean hashexists = hashcodes[hash] != -1;
		if (hashexists) {
			int pos = hashcodes[hash];
			if (is_equal(pos, var, lchild, rchild)) {
				return use(pos);
			}
			int item = hashcodes[hash+1];
			while (item != -1) {
				pos = hashitems[item];
				if (is_equal(pos, var, lchild, rchild)) {
					return use(pos);
				}
				item = hashitems[item+1];
			}
		}
		
		// node not found, create it
		int pos = create_bnode(var, lchild, rchild);
		if ( (100*nbnodes)/hashcodes.length > FILL_LIMIT) {
			extend_hash();
		} else {
			place_hash(pos, hash);
		}
		return use(pos);
	}

	/**
	 * Get a multi-valued node. It will be created if needed.
	 * The resulting ID should be freed when it stopped being used.
	 *  
	 * @param var
	 * @param children
	 * @return the ID of the node
	 */
	public int get_mnode(int var, int[] children) {
		// check that the children are not all equal
		int child = children[0];
		for (int i=0 ; i<children.length ; i++) {
			if (children[i] != child) {
				child = -1;
				break;
			}
		}
		if (child > 0) {
			return use(child);
		}
		
		int hash = compute_mhash(var, children);
		boolean hashexists = hashcodes[hash] != -1;
		if (hashexists) {
			int pos = hashcodes[hash];
			if (is_equal(pos, var, children)) {
				return use(pos);
			}
			int item = hashcodes[hash+1];
			while (item != -1) {
				pos = hashitems[item];
				if (is_equal(pos, var, children)) {
					return use(pos);
				}
				item = hashitems[item+1];
			}
		}
		
		// node not found, create it
		int pos = create_mnode(var, children);
		if ( (100*nbnodes)/hashcodes.length > FILL_LIMIT) {
			extend_hash();
		} else {
			place_hash(pos, hash);
		}
		return use(pos);
	}

//	/**
//	 * Get the value of a leaf.
//	 * 
//	 * @param id
//	 * @return the value of the leaf or -1 if the ID does not point to a leaf.
//	 */
//	public int getLeafValue(int id) {
//		if (id < nbleaves) {
//			return blocs[id];
//		}
//		return -1;
//	}
	
	/**
	 * Free a node. If it is not used at all anymore, it will be removed from the data structure.
	 * <p>
	 * This should be used for each node id that was obtained through one of the <code>get_?node()</code> method,
	 * or for the one explicitly marked as used.
	 * <p>
	 * Note that the space will be made available for new nodes, but it will NOT be returned to the garbage collector.
	 * 
	 * @param pos
	 * 
	 * @see <code>use(int)</code>
	 * @see <code>get_bnode(int, int, int)</code>
	 * @see <code>get_mnode(int, int[])</code>
	 */
	public void free(int pos) {
		if (!CANFREE) {
			return;
		}
		if (pos < nbleaves) {
			return;
		}
		
		if (blocs[pos+INC_COUNT] > 1) {
			blocs[pos+INC_COUNT]--;
			return;
		}

		if (blocs[pos] < 0) {
			System.err.println("re-free bloc: "+pos);
			return;
		}

		int var = blocs[pos];
		int nbval = variables[var].nbval;
		
		// remove it from the hash
		freeHash(pos, var, nbval);

		// clear the data and set the bloc as free
		blocs[pos] = -1;
		if (lastbloc == pos+blocsize) {
			lastbloc = pos;
			// TODO: further decrease lastbloc if the previous blocs are also free?
			// find out how many are free and remove them from the chained list.
		} else {
			blocs[pos+1] = freeBloc;
			freeBloc = pos;
		}
		// free the children
		for (int i=0 ; i<nbval ; i++) {
			free(blocs[pos+INC_VALUES+i]);
			blocs[pos+INC_VALUES+i] = 0;
		}
		nbnodes--;
	}
	
	/**
	 * Internal method to remove an entry from the hashtable.
	 * This aims to be called by <code>free(int)</code> when needed.
	 * 
	 * @param pos
	 * @param var
	 * @param nbval
	 */
	private void freeHash(int pos, int var, int nbval) {
		if (!CANFREEHASH) {
			return;
		}
		
		// compute the hash
		int hash;
		if (nbval == 2) {
			hash = compute_bhash(var, blocs[pos+INC_VALUES], blocs[pos+INC_VALUES+1]);
		} else {
			int[] children = new int[variables[var].nbval];
			System.arraycopy(blocs, pos+INC_VALUES, children, 0, children.length);
			hash = compute_mhash(var, children);
		}

		int hpos = hashcodes[hash];
		int itemPos = hashcodes[hash+1];
		if (hpos == pos) { 		// the item is in the main hashtable
			if (itemPos == -1) {
				hashcodes[hash] = -1;
			} else {
				// re-chain back this hashcode
				hashcodes[hash] = hashitems[itemPos];
				hashcodes[hash+1] = hashitems[itemPos+1];
				free_hashiten(itemPos);
			}
			return;
		}
		
		if (true) {
			//return;
		}

		// the item is not in the main hashtable: look it up in the hashitem linktable
		int nextItem, prevItem = -1;
		while (itemPos != -1) {
			hpos = hashitems[itemPos];
			nextItem = hashitems[itemPos+1];
			if (hpos == pos) {
				if (prevItem == -1) {
					// first item, link in the main hashcodes array
					hashcodes[hash+1] = nextItem;
				} else {
					// update chain in the hashitems array
					hashitems[prevItem+1] = nextItem;
				}
				free_hashiten(itemPos);
				return;
			}
			prevItem = itemPos;
			itemPos = nextItem;
		}
		System.err.println("item not found !!!!");
	}
	
	/**
	 * Internal method to destroy a hash item.
	 * This aims to be called by <code>freeHash</code> when needed.
	 * 
	 * @param item
	 */
	private void free_hashiten(int item) {
		if (item < 0) {
			System.err.println("item not in hash !!!!");
			return;
		}
		// free the hash item
		if (item == lastitem-2) {
			lastitem = item;
			// TODO: further decrease lastitem if the previous items are also free:
			// track down to find how many are free and remove them from the chained list.
		} else {
			hashitems[item]   = -1;
			hashitems[item+1] = freeItem;
			freeItem = item;
		}
	}

	/**
	 * Internal method to insert a new hash.
	 * It works like <code>get_free_bloc</code>
	 */
	private void place_hash(int blocPos, int hash) {
		if (hashcodes[hash] == -1) {
			hashcodes[hash] = blocPos;
			hashcodes[hash+1] = -1;
			return;
		}
		int pos;
		if (freeItem >= 0) {
			pos = freeItem;
			freeItem = hashitems[pos+1];
		} else {
			pos = lastitem;
			lastitem += 2;
			if (lastitem > hashitems.length) {
				hashitems = extend_array(hashitems);
			}
		}
		
		hashitems[pos] = blocPos;
		hashitems[pos+1] = hashcodes[hash+1];
		if (hashcodes[hash+1] == pos) {
			System.err.println("BIG BUG with hash link list!");
		}
		hashcodes[hash+1] = pos;
	}

	/**
	 * Mark a node as used. This is done automatically when creating or reusing a node.
	 * In most cases, manually calling this should not be necessary.
	 * If you do call it, remember to call <code>free(int)</code> when you release it.
	 * 
	 * @param node
	 * 
	 * @return the ID of the node (yes, the same as the parameter, just as convenience)
	 */
	public int use(int node) {
		if (!isleaf(node)) {
			blocs[node+INC_COUNT]++;
		}
		return node;
	}
	
	/**
	 * Flip the values of leaves reachable in this MDD.
	 * 
	 * @param node
	 * @param newValues
	 * 
	 * @return the ID of a node rooting a MDD with the same structure but different leaves
	 */
	public int leafFlip(int node, int[] newValues) {
		if (isleaf(node)) {
			return newValues[node];
		}
		int level = blocs[node];
		int nbval = variables[level].nbval;
		if (nbval == 2) {
			int l = leafFlip(blocs[node+INC_VALUES], newValues);
			int r = leafFlip(blocs[node+INC_VALUES+1], newValues);
			return get_bnode_free(level, l, r);
		}
		
		int[] children = new int[nbval];
		for (int i=0 ; i<children.length ; i++) {
			children[i] = leafFlip(node+INC_VALUES+i, newValues);
		}
		return get_mnode_free(level, children);
	}

	/**
	 * Get a multi-valued node and release the provided children.
	 * 
	 * @param level
	 * @param children
	 * @return the id of the node.
	 * 
	 * @see <code>get_mnode(int, int[])</code>
	 * @see <code>free(int)</code>
	 */
	public int get_mnode_free(int level, int[] children) {
		int ret = get_mnode(level, children);
		for (int child: children) {
			free(child);
		}
		return ret;
	}

	/**
	 * get a Boolean node and release the provided children.
	 * 
	 * @param level		level of the corresponding MDDVariable
	 * @param l			ID of the desired left child     (false)
	 * @param r			ID of the desired right children (true)
	 * 
	 * @return the ID of the node
	 * 
 	 * @see <code>get_bnode(int, int, int)</code>
	 * @see <code>free(int)</code>
	 */
	public int get_bnode_free(int level, int l, int r) {
		int ret = get_bnode(level, l, r);
		free(l);
		free(r);
		return ret;
	}
	
	/**
	 * Logical not, performed by flipping leaves 0 and 1.
	 * @param node
	 * @return the ID of the flipped MDD root
	 */
	public int not(int node) {
		return leafFlip(node, NOTFLIP);
	}


	/**
	 * Determine the relation between two nodes.
	 * Mainly used by operators to select the appropriate code path.
	 * 
	 * @param first
	 * @param other
	 * 
	 * @return  an NodeRelation value denoting which node is a leaf or has the highest ranked variable
	 */
	public NodeRelation getRelation(int first, int other) {
		if (first == other) {
			if (first < nbleaves) {
					return NodeRelation.LL;
			}
			return NodeRelation.NN;
		}
		if (first < nbleaves) {
			if (other < nbleaves) {
				return NodeRelation.LL;
			} else {
				return NodeRelation.LN;
			}
		} else if (other < nbleaves) {
			return NodeRelation.NL;
		} else {
			int l1 = blocs[first];
			int l2 = blocs[other];
			if (l1 == l2) {
				return NodeRelation.NN;
			} else if (l1 < l2) {
				return NodeRelation.NNn;
			} else {
				return NodeRelation.NNf;
			}
		}
	}
	
	/**
	 * helper to compute hashcodes. Shamelessly stolen from JavaBDD.
	 * @param a
	 * @param b
	 * @return
	 */
    static final int PAIR(int a, int b) {
        return ((a + b) * (a + b + 1) / 2 + a);
    }
    /**
	 * Compute hashcodes for a Boolean node. Shamelessly stolen from JavaBDD.
     * @param var
     * @param lchild
     * @param rchild
     * @return
     */
	private int compute_bhash(int var, int lchild, int rchild) {
		int hash = PAIR(rchild, PAIR(lchild, var));
		return (Math.abs(hash) % (hashcodes.length / 2))*2;
	}

	/**
	 * helper to compute hashcodes. JavaBDD hash adapted to multi-valued nodes.
	 * @param var
	 * @param children
	 * @return
	 */
	private int compute_mhash(int var, int[] children) {
		int hash = var;
		for (int i=0 ; i<children.length ; i++) {
			hash = PAIR(children[i], hash);
		}
		return (Math.abs(hash) % (hashcodes.length / 2))*2;
	}
	
	private boolean is_equal(int position, int var, int lchild, int rchild) {
		return blocs[position] == var && blocs[position+INC_VALUES] == lchild && blocs[position+INC_VALUES+1] == rchild;
	}
	private boolean is_equal(int position, int var, int[] children) {
		if (blocs[position] != var) {
			return false;
		}
		for (int i=0 ; i<children.length ; i++) {
			if (blocs[position+INC_VALUES+i] != children[i]) {
				return false;
			}
		}
		return true;
	}

	private int create_bnode(int var, int lchild, int rchild) {
		int pos = get_free_bloc();
		blocs[pos] = var;
		blocs[pos+INC_COUNT] = 0; 	   // reset usage count
		blocs[pos+INC_VALUES] = lchild;
		blocs[pos+INC_VALUES+1] = rchild;
		
		// increase usage of the children
		use(lchild);
		use(rchild);
		nbnodes++;
		return pos;
	}

	private int create_mnode(int var, int[] children) {
		int pos = get_free_bloc();
		blocs[pos] = var;
		blocs[pos+INC_COUNT] = 0; 	   // reset usage count
		System.arraycopy(children, 0, blocs, pos+INC_VALUES, children.length);
		
		for (int child: children) {
			use(child);
		}
		
		nbnodes += 1;
		return pos;
	}

	/**
	 * Get the next free data bloc.
	 * Look-up among free blocs or allocate a new one.
	 * 
	 * @return
	 */
	private int get_free_bloc() {
		int pos = freeBloc;
		if (pos >= 0) {
			freeBloc = blocs[pos+1];
			return pos;
		}
		pos = lastbloc;
		lastbloc += blocsize;
		if (lastbloc > blocs.length) {
			blocs = extend_array(blocs);
		}
		return pos;
	}

//	/**
//	 * Get a group of contiguous data blocs.
//	 * For now it only allocates contiguous blocs at the end.
//	 * 
//	 * @param nb the number of blocs needed
//	 * @return the ID of the starting bloc
//	 */
//	private int get_free_blocs(int nb) {
//		int pos = lastbloc;
//		// search in empty slots ?
//		lastbloc += nb*blocsize;
//		if (lastbloc > blocs.length) {
//			blocs = extend_array(blocs);
//		}
//		
//		return pos;
//	}

	/**
	 * extend an array: allocate a bigger array and copy existing data.
	 */
	private int[] extend_array(int[] data) {
		int[] new_array = new int[data.length*2];
		System.arraycopy(data, 0, new_array, 0, data.length);
		return new_array;
	}
	/**
	 * extend the hashing array: allocate a bigger array and recompute all hash
	 * into the new array.
	 */
	private void extend_hash() {
		hashcodes = new int[hashcodes.length*2];
		reset_hash();
		
		for (int i=nbleaves ; i<lastbloc ; i+= blocsize) {
			int idvar = blocs[i];
			if (idvar < 0) {
				// empty bloc, skip it
				continue;
			}
			int nbval = variables[idvar].nbval;
			int hash;
			if (nbval == 2) {
				hash = compute_bhash(idvar, blocs[i+INC_VALUES], blocs[i+INC_VALUES+1]);
			} else {
				int[] children = new int[nbval];
				System.arraycopy(blocs, i+INC_VALUES, children, 0, nbval);
				hash = compute_mhash(idvar, children);
			}
			
			// link it
			place_hash(i, hash);
			
			// FIXME: update if spanning over several blocs is implemented
			// i += variables[idvar].spanbloc;
		}
	}

	private void reset_hash() {
		// clear all positions
		for (int i=0 ; i<hashcodes.length ; i++) {
			hashcodes[i] = -1;
		}
		// clear hashitems
		for (int i=0 ; i<hashitems.length ; i++) {
			hashitems[i] = -1;
		}
		lastitem = 0;
		freeItem = -1;
	}

	/**
	 * @return the number of non-leaf nodes stored in the factory.
	 */
	public int getNodeCount() {
		return nbnodes;
	}

	/**
	 * print raw data structure (hashcodes, hashitems and data blocs).
	 * A must if you enjoy reading boring series of numbers. 
	 */
	public void debug() {
		System.out.println("------------------------------------------------------------");
		System.out.println("Raw factory info: ");
		System.out.println("    "+nbleaves  + " leaves -- " + blocsize + " cell per bloc"); 
		System.out.println("    "+nbnodes + " nodes" );
		System.out.print("Hashes: ");
		prettyPrintArray(hashcodes,0,2,-1);
		System.out.print("HList:  ");
		prettyPrintArray(hashitems, 0, 2, lastitem);
		System.out.print("Data:   ");
		prettyPrintArray(blocs, nbleaves, blocsize, lastbloc);
		System.out.println("------------------------------------------------------------");
	}

	/**
	 * Debug helper: Pretty printer for the Array used as storage.
	 *  
	 * @param a		the array
	 * @param skip	number of elements to skip at the beginning
	 * @param bs	block size
	 * @param last	last element to print
	 */
	private void prettyPrintArray(int[] a, int skip, int bs, int last) {
		if (last == -1) {
			last = a.length;
		}
		for (int i=0 ; i<last ; i++) {
			int b = i-skip;
			if (b>=0 && b%bs == 0) {
				System.out.print("| ");
			}
			System.out.print(a[i]+" ");
		}
		System.out.println();
	}
	
	/**
	 * @param id
	 * @return true if id denotes a leaf in this factory
	 */
	public boolean isleaf(int id) {
		return id < nbleaves;
	}
	
	/**
	 * Debug helper: count the paths leading to non-zero leaves.
	 * Each path may correspond to several variable assignments,
	 * the result strongly depends on the variable order.
	 * 
	 * @param id
	 * 
	 * @return the number of paths found
	 */
	public int count_positive_path(int id) {
		if (id < nbleaves) {
			if (blocs[id] > 0) {
				return 1;
			}
			return 0;
		}
		
		if (variables[blocs[id]].nbval == 2) {
			return count_positive_path(blocs[id+INC_VALUES]) + count_positive_path(blocs[id+INC_VALUES+1]);
		}
		
		int c = count_positive_path(blocs[id+INC_VALUES]);
		for (int i=1 ; i<variables[blocs[id]].nbval ; i++) {
			c += count_positive_path(blocs[id+INC_VALUES+i]);
		}
		return c;
	}
	
	/**
	 * get a node for the specified variable with two different children:
	 * a "true" child in the specified range and a "false" one outside.
	 * 
	 * @param var the variable
	 * @param vfalse the "false" child
	 * @param vtrue the "true" child
	 * @param start the start of the "true" range
	 * @param end the end of the "true" range
	 * 
	 * @return a MDD ID.
	 */
	public int getSimpleNode(int var, int vfalse, int vtrue, int start, int end) {
		int nbval = variables[var].nbval;
		if (start>end || start<0 || end>=nbval) {
			return -1;
		}
		if (nbval == 2) {
			if (start != end) {
				return vtrue;
			}
			if (start == 0) {
				return get_bnode(var, vtrue, vfalse);
			}
			return get_bnode(var, vfalse, vtrue);
		}
		
		
		int[] children = new int[nbval];
		for (int i=0 ; i<start ; i++) {
			children[i] = vfalse;
		}
		for (int i=start ; i<=end ; i++) {
			children[i] = vtrue;
		}
		for (int i=end+1 ; i<nbval ; i++) {
			children[i] = vfalse;
		}
		return get_mnode(var, children);
	}

	public int getLevel(int id) {
		if (id < nbleaves) {
			return -1;
		}
		return blocs[id];
	}
	
	/**
	 * @param id      id of a node
	 * @param value   assignment value
	 * 
	 * @return the id of the child node reached when assigning this value to the variable of the provided node.
	 */
	public int getChild(int id, int value) {
		if (id < nbleaves) {
			return -1;
		}
		return blocs[id+INC_VALUES+value];
	}

	/**
	 * How many variables are currently used?
	 * <p>
	 * Note that during the life of a factory, new variables may be added.
	 * For consistency reasons, the number should never decrease.
	 * 
	 * @return the number of MDDVariable used in this factory.
	 */
	public int getNbVariables() {
		return variables.length;
	}

	/**
	 * Find the next leaf in this MDD and store the path in the provided array.
	 * This function is a helper for the LeafPath Iterator.
	 * 
	 * @param indices	the path in terms of MDD nodes (for backtracking)
	 * @param values	the next value to be used for these nodes (for backtracking)
	 * @param tracking  the index of the current position in the previous arrays and the value of the current leaf
	 */
	public void findNextLeaf(int[] indices, int[] values, int[] tracking) {
		int cur = tracking[0];
		if (cur < 0) {
			System.err.println("findNext called after exploration is finished");
			return;
		}
		
		int node = indices[cur];
		if (isleaf(node)) {
			System.err.println("findNext went too far");
			return;
		}
		int curValue = values[cur]+1;
		MultiValuedVariable var = variables[getLevel(node)];
		if (curValue < var.nbval) {
			values[cur]++;
			int next = getChild(node, curValue);
			if (isleaf(next)) {
				tracking[1] = next;
				return;
			}
			tracking[0]++;
			indices[cur+1] = next;
			values[cur+1] = -1;
		} else {
			tracking[0]--;
			if (cur == 0) {
				return;
			}
		}
		findNextLeaf(indices, values, tracking);
	}


	/**
	 * Infer the effect of a variable in a given MDD.
	 * 
	 * @param node   the MDD
	 * @param pivot  the variable of interest
	 * @return 0 for no effect, 1 for positive effect, -1 for negative effect or 2 for dual effect
	 */
	public int getSign(int node, int pivot) {
		return getSign(node, pivot, 0);
	}
	private int getSign(int node, int pivot, int curSign) {
		if (isleaf(node)) {
			return curSign;
		}
		
		int level = getLevel(node);
		int nbval = variables[level].nbval;
		if (level < pivot) {
			// recursive call
			for (int i=0 ; i<nbval ; i++) {
				curSign = getSign(getChild(node, i), pivot, curSign);
			}
		} else if (level == pivot) {
			for (int i=1 ; i<nbval ; i++) {
				curSign = getSign_sub(getChild(node, i-1), getChild(node, i), curSign);
			}
		}
		return curSign;
	}
	
	private int getSign_sub(int n1, int n2, int curSign) {
		if (n1 == n2) {
			return curSign;
		}
		int level, nbval;
		switch (getRelation(n1, n2)) {
		case LL:
			// make the choice!
			if (n1 > n2) {
				switch (curSign) {
				case 0:
					curSign = -1;
					break;
				case 1:
					curSign = 2;
					break;
				}
			} else if (n1 < n2) {
				switch (curSign) {
				case 0:
					curSign = 1;
					break;
				case -1:
					curSign = 2;
					break;
				}
			}
			break;
		case LN:
		case NNf:
			level = getLevel(n2);
			nbval = variables[level].nbval;
			for (int i=0 ; i<nbval ; i++) {
				curSign = getSign_sub(n1, getChild(n2, i), curSign);
			}
			break;
		case NL:
		case NNn:
			level = getLevel(n1);
			nbval = variables[level].nbval;
			for (int i=0 ; i<nbval ; i++) {
				curSign = getSign_sub(getChild(n1, i), n2, curSign);
			}
			break;
		case NN:
			level = getLevel(n1);
			nbval = variables[level].nbval;
			for (int i=0 ; i<nbval ; i++) {
				curSign = getSign_sub(getChild(n1, i), getChild(n2, i), curSign);
			}
			break;
		}
		return curSign;
	}

	public int getNbValues(int level) {
		return variables[level].nbval;
	}
}
