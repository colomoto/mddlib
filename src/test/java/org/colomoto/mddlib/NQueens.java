package org.colomoto.mddlib;

import org.colomoto.mddlib.operators.MDDBaseOperators;

import java.util.ArrayList;
import java.util.List;


/**
 * Simple solver for the n-queens problem, using BDDs or MDDs
 * It is inspired by the n-queens example in JavaBDD.
 * 
 * @author Aurelien Naldi
 */
public class NQueens {

    public static void main(String[] args) {
    	final int N_MIN=2;
    	final int N_MAX=6;
    	
    	// some known numbers of solution (from wikipedia)
    	final int[] N_QUEENS_SOLUTIONS_COUNT = {0,1,0,0,2,10,4,40,92,352,724,2680,14200,73712,365596};
    	
    	long start;
    	int count;
		String s_result;
    	for (byte N=N_MIN ; N<=N_MAX ; N++) {
    		if (N<11) {
	    		start = System.currentTimeMillis();
	    		count = testBNQueens(N);
	    		start = System.currentTimeMillis()-start;
	    		if (count == N_QUEENS_SOLUTIONS_COUNT[N]) {
	    			s_result = count+" solutions";
	    		} else {
	    			s_result = N_QUEENS_SOLUTIONS_COUNT[N]+" solutions [found "+count+"]";
	    		}
	    		System.out.println("(B) N="+N+": "+s_result+", temps: "+start);
    		}
    		
    		if (N>2) {
	    		start = System.currentTimeMillis();
	    		count = testMNQueens(N);
	    		start = System.currentTimeMillis()-start;
	    		if (count == N_QUEENS_SOLUTIONS_COUNT[N]) {
	    			s_result = count+" solutions";
	    		} else {
	    			s_result = N_QUEENS_SOLUTIONS_COUNT[N]+" solutions [found "+count+"]";
	    		}
	    		System.out.println("(M) N="+N+": "+s_result+", temps: "+start);
    		}
    	}
	}

	/**
	 * Boolean version of the n-queens problem
	 */
	public static int testBNQueens(byte N) {
		int nbvar = N*N;
		List<String> keys = new ArrayList<String>();
		int[][] basics = new int[nbvar][2];
		for (int i=0 ; i<N ; i++) {
			for (int j=0 ; j<N ; j++) {
				keys.add(i+","+j);
			}
		}
		MDDManager ddmanager = MDDManagerFactory.getManager(keys, 2);
		MDDVariable[] variables = ddmanager.getAllVariables();
		for (int i=0 ; i<nbvar ; i++) {
			basics[i][0] = variables[i].getNode(1, 0);
			basics[i][1] = variables[i].getNode(0, 1);
		}
		
		// first set of constraints: one queen on each row
		int[] elts = new int[N];
		int[] all_cst = new int[N + nbvar*N*4];
		int cstidx = 0;
		for (int i=0 ; i<N ; i++) {
			int row = i*N;
			for (int j=0 ; j<N ; j++) {
				elts[j] = basics[row+j][1];
			}
			all_cst[cstidx++] = MDDBaseOperators.OR.combine(ddmanager, elts);
		}
		
		// each place on the board is in conflict with places on the same row, column or diagonal
		int pos;
		for (int i=0 ; i<N ; i++) {
			for (int j=0 ; j<N ; j++) {
				pos = i*N+j;
				for (int k=0 ; k<N ; k++) {
					if (k>i) {
						all_cst[cstidx++] = get_nand(ddmanager, basics, pos, k*N+j);
					}
					if (k>j) {
						all_cst[cstidx++] = get_nand(ddmanager, basics, pos, i*N+k);

						int dj = k-j;
						int r = i+dj;
						if (r<N) {
							all_cst[cstidx++] = get_nand(ddmanager, basics, pos, r*N+k);
						}
						r = i-dj;
						if (r>=0) {
							all_cst[cstidx++] = get_nand(ddmanager, basics, pos, r*N+k);
						}
					}
				}
			}
		}
		int[] defined_cst = new int[cstidx];
		System.arraycopy(all_cst, 0, defined_cst, 0, cstidx);
		int result = MDDBaseOperators.AND.combine(ddmanager, defined_cst);

		System.out.println("usage: "+ddmanager.getNodeCount());
		
		for (int i: defined_cst) {
			ddmanager.free(i);
		}
		System.out.println("usage: "+ddmanager.getNodeCount());
		
		PathSearcher searcher = new PathSearcher(ddmanager, 1);
		searcher.setNode(result);
		return searcher.countPaths();
	}
	
	
	
	private static int get_nand(MDDManager ddmanager, int[][] basics, int p1, int p2) {
		int result = MDDBaseOperators.OR.combine(ddmanager, basics[p1][0], basics[p2][0]);
		return result;
	}
	
	/**
	 * Multi-valued version of the n-queens problem
	 */
	public static int testMNQueens(byte N) {
		byte nbvar = N;
		MDDVariableFactory vbuilder = new MDDVariableFactory();
		for (int i=0 ; i<N ; i++) {
			vbuilder.add(""+i, N);
		}
		MDDManager ddmanager = MDDManagerFactory.getManager( vbuilder, 2);
		MDDVariable[] variables = ddmanager.getAllVariables();
		int[][] basics = new int[nbvar*nbvar][2];
		for (int i=0 ; i<nbvar ; i++) {
			for (int j=0 ; j<nbvar ; j++) {
				int[] children = new int[nbvar];
				children[j] = 1;
				basics[i*N+j][1] = variables[i].getNode(children);

				children = new int[nbvar];
				for (int v=0 ; v<nbvar ; v++) {
					if (v != j) {
						children[v] = 1;
					}
				}
				basics[i*N+j][0] = variables[i].getNode(children);
			}
		}

		// store constraints
		int[] all_cst = new int[nbvar*nbvar*N*4];
		int cstidx = 0;
		
		// each place on the board is in conflict with places on the same row, column or diagonal
		for (int i=0 ; i<N ; i++) {
			for (int j=0 ; j<N ; j++) {
				int pos = i*N+j;
				for (int k=Math.min(i,j)+1 ; k<N ; k++) {
					if (k>i) {
						all_cst[cstidx++] = get_nand(ddmanager, basics, pos, k*N+j);
					}
					if (k>j) {
						int dj = k-j;
						int r = i+dj;
						if (r<N) {
							all_cst[cstidx++] = get_nand(ddmanager, basics, pos, r*N+k);
						}
						r = i-dj;
						if (r>=0) {
							all_cst[cstidx++] = get_nand(ddmanager, basics, pos, r*N+k);
						}
					}
				}
			}
		}
		int[] defined_cst = new int[cstidx];
		System.arraycopy(all_cst, 0, defined_cst, 0, cstidx);
		int result = MDDBaseOperators.AND.combine(ddmanager, defined_cst);
		System.out.println("usage: "+ddmanager.getNodeCount());
		
		PathSearcher searcher = new PathSearcher(ddmanager, 1);
		searcher.setNode(result);
		return searcher.countPaths();
	}
}
