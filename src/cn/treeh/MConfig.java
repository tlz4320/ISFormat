package cn.treeh;

import cn.treeh.ToNX.Annotation.Arg;
import cn.treeh.ToNX.util.ArgUtil;

public class MConfig extends ArgUtil {
    @Arg(arg = "B", needed = true, hasArg = true, description = "isoform bed file")
    public String bedFile;
    @Arg(arg = "C", needed = true, hasArg = true, description = "isoform name and reads name pair")
    public String cluster2reads;
    @Arg(arg = "O", needed = true, hasArg = true, description = "output file")
    public String outPut;
    @Arg(arg = "S", needed = false, hasArg = true, val = ",\t",description = "separate symbol between isoform name and read name")
    public String sp_reg;

}
