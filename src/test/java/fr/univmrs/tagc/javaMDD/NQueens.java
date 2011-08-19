package fr.univmrs.tagc.javaMDD;

import fr.univmrs.tagc.javaMDD.operators.MDDBaseOperators;


public class NQueens {

    public static void main(String[] args) {
    	final int N_MIN=2;
    	final int N_MAX=6;
    	
    	// some known numbers of solution (from wikipedia)
    	final int[] N_QUEENS_SOLUTIONS_COUNT = {0,1,0,0,2,10,4,40,92,352,724,2680,14200,73712,365596};
    	
    	long start;
    	int count;
		String s_result;
    	for (int N=N_MIN ; N<=N_MAX ; N++) {
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
	public static int testBNQueens(int N) {
		int nbvar = N*N;
		MultiValuedVariable[] variables = new MultiValuedVariable[nbvar];
		int[][] basics = new int[nbvar][2];
		for (int i=0 ; i<N ; i++) {
			for (int j=0 ; j<N ; j++) {
				variables[i*N+j] = new MultiValuedVariable(i+","+j);
			}
		}
		MDDFactory f = new MDDFactory(variables, 2);
		for (int i=0 ; i<nbvar ; i++) {
			basics[i][0] = f.get_bnode(i, 1, 0);
			basics[i][1] = f.get_bnode(i, 0, 1);
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
			all_cst[cstidx++] = MDDBaseOperators.OR.combine(f, elts);
		}
		
		// each place on the board is in conflict with places on the same row, column or diagonal
		int pos;
		for (int i=0 ; i<N ; i++) {
			for (int j=0 ; j<N ; j++) {
				pos = i*N+j;
				for (int k=0 ; k<N ; k++) {
					if (k>i) {
						all_cst[cstidx++] = get_nand(f, basics, pos, k*N+j);
					}
					if (k>j) {
						all_cst[cstidx++] = get_nand(f, basics, pos, i*N+k);

						int dj = k-j;
						int r = i+dj;
						if (r<N) {
							all_cst[cstidx++] = get_nand(f, basics, pos, r*N+k);
						}
						r = i-dj;
						if (r>=0) {
							all_cst[cstidx++] = get_nand(f, basics, pos, r*N+k);
						}
					}
				}
			}
		}
		int[] defined_cst = new int[cstidx];
		System.arraycopy(all_cst, 0, defined_cst, 0, cstidx);
		int result = MDDBaseOperators.AND.combine(f, defined_cst);

		System.out.println("usage: "+f.getNodeCount());
		
		for (int i: defined_cst) {
			f.free(i);
		}
		System.out.println("usage: "+f.getNodeCount());
		
		return f.count_positive_path(result);
	}
	
	
	
	private static int get_nand(MDDFactory f, int[][] basics, int p1, int p2) {
		int result = MDDBaseOperators.OR.combine(f, basics[p1][0], basics[p2][0]);
		return result;
	}
	
	/**
	 * Multi-valued version of the n-queens problem
	 */
	public static int testMNQueens(int N) {
		int nbvar = N;
		MultiValuedVariable[] variables = new MultiValuedVariable[nbvar];
		for (int i=0 ; i<N ; i++) {
			variables[i] = new MultiValuedVariable(""+i, N);
		}
		MDDFactory f = new MDDFactory(variables, 2);
		int[][] basics = new int[nbvar*nbvar][2];
		for (int i=0 ; i<nbvar ; i++) {
			for (int j=0 ; j<nbvar ; j++) {
				int[] children = new int[nbvar];
				children[j] = 1;
				basics[i*N+j][1] = f.get_mnode(i, children);

				children = new int[nbvar];
				for (int v=0 ; v<nbvar ; v++) {
					if (v != j) {
						children[v] = 1;
					}
				}
				basics[i*N+j][0] = f.get_mnode(i, children);
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
						all_cst[cstidx++] = get_nand(f, basics, pos, k*N+j);
					}
					if (k>j) {
						int dj = k-j;
						int r = i+dj;
						if (r<N) {
							all_cst[cstidx++] = get_nand(f, basics, pos, r*N+k);
						}
						r = i-dj;
						if (r>=0) {
							all_cst[cstidx++] = get_nand(f, basics, pos, r*N+k);
						}
					}
				}
			}
		}
		int[] defined_cst = new int[cstidx];
		System.arraycopy(all_cst, 0, defined_cst, 0, cstidx);
		int result = MDDBaseOperators.AND.combine(f, defined_cst);
		System.out.println("usage: "+f.getNodeCount());
		return f.count_positive_path(result);
	}
}
