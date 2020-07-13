package cn.treeh;

import cn.treeh.generator.LinkClusterandReads;
import cn.treeh.generator.SortBed;

public class Main {

    public static void main(String[] args) {
        MConfig configure = new MConfig();
        configure.parse(args);
        LinkClusterandReads reads = new LinkClusterandReads(configure.cluster2reads, configure.sp_reg);
        SortBed.setClusterReads(reads.getCluster2reads());
        SortBed.index(configure.bedFile, configure.outPut, "useless");
    }
}
