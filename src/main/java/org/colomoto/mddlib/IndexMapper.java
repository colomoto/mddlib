package org.colomoto.mddlib;

import java.util.Map;

public interface IndexMapper {

    static IndexMapper getSimpleMapper(Map<Integer,Integer> map) {
        return new SimpleMapper(map);
    }

    int get(int idx);

}


class SimpleMapper implements IndexMapper {

    private final Map<Integer,Integer> map;

    public SimpleMapper(Map<Integer,Integer> map) {
        this.map = map;
    }

    @Override
    public int get(int idx) {
        Integer r = this.map.get(idx);
        if (r == null) {
            return idx;
        }
        return r;
    }
}
