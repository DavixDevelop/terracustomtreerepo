package com.davixdevelop.terracustomtreegen.baker;

import com.davixdevelop.terracustomtreegen.SegmentLinearFunc;

import java.util.HashSet;
import java.util.Set;

public class RawSegments {
    public  RawSegments(){
        ID = "";
        lines = new HashSet<>();
    }

    public String ID;
    public Set<SegmentLinearFunc> lines;
}
