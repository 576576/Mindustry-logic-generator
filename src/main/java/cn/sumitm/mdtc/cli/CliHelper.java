package cn.sumitm.mdtc.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "mdtc-compiler", mixinStandardHelpOptions = true, version = "MdtC Compiler v1.31",
        description = "A command line tool for MdtC compiler.")
public class CliHelper {

    @Option(names = {"-v", "--version"}, usageHelp = true, description = "显示版本信息")
    public boolean versionInfo;

    @Option(names = {"-f", "--format"}, description = "格式化代码")
    public boolean isToFormat;

    @Option(names = {"-fo", "--format-only"}, description = "仅格式化代码")
    public boolean isFormatOnly;

    @Option(names = {"-i", "--file"}, paramLabel = "<file>", description = "指定文件路径")
    public String filePath = "";

    @Option(names = {"-o", "--output"}, paramLabel = "<output>", description = "指定输出路径")
    public String outPath = "";

    @Option(names = {"-oo", "--open-out"}, description = "编译后打开输出")
    public boolean isOpenOutput;
}
