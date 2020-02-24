package org.colomoto.mddlib;

import org.colomoto.mddlib.operators.MDDBaseOperators;


/***
 * Map Decision diagrams from one manager to another.
 *
 */
public class MDDMapper {

    private final MDDManager sourceDDM, targetDDM;
    private final PathSearcher searcher;
    private final IndexMapper indexMapper;

    public MDDMapper(MDDManager sourceDDM, MDDManager targetDDM, IndexMapper indexMapper) {
        this.sourceDDM = sourceDDM;
        this.targetDDM = targetDDM;
        this.searcher = new PathSearcher(sourceDDM, 1, Integer.MAX_VALUE);
        this.indexMapper = indexMapper;
    }

    public int mapMDD(int node) {

        int result = 0;
        int[] path = searcher.setNode(node);
        for (int value: searcher) {

            int curBranch = 1;
            for (int i=0 ; i<path.length ; i++) {
                byte v = (byte)path[i];
                if (v >= 0) {
                    int curVar = targetDDM.getNodeVariable(indexMapper.get(i)).getNodeForValue(v, value);
                    int nextBranch = MDDBaseOperators.AND.combine(targetDDM, curBranch, curVar);
                    targetDDM.free(curBranch);
                    targetDDM.free(curVar);
                    curBranch = nextBranch;
                }
            }

            int next = MDDBaseOperators.OVER.combine(targetDDM, result, curBranch);
            targetDDM.free(curBranch);
            targetDDM.free(result);
            result = next;
        }

        return result;
    }
}
