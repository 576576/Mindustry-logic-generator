/**
 * 桥接接口:由 Java 主程序(BuiltinEngine / EmitCtx)在运行期注入。
 * 指令处理器只能通过这些方法访问编译状态与词法工具,不能触碰 Java API。
 * 规范见 docs/instructions/README.md。
 */

declare interface SubResult {
  /** 子编译产生的 mdtcode 行(JS Array,支持 length/index/pop/slice) */
  bash: string[];
  /** 子编译后剩余表达式(通常为空串) */
  expr: string;
  /** 子编译结束时的 ref 计数 */
  stat: number;
}

declare interface EmitCtx {
  /** 当前中间变量计数器 ref 的值 */
  ref(): number;
  /** 覆写 ref(用于子编译后同步计数) */
  setRef(n: number): void;
  /** 当前中间变量名,即 "mid." + ref */
  mid(): string;
  /** 追加一行 mdtcode 到输出列表 */
  bash(line: string): void;
  /** 追加多行(JS Array)到输出列表 */
  bashAll(lines: string[]): void;
  /** 对表达式做子编译(调用主编译管道 convertCodeLine) */
  compileSub(expr: string): SubResult;
  /** 点链指令左侧的 block 引用 */
  block(): string;
  /** 按顶层逗号切分参数(等同 Utils.bracketPartSplit,已 trim) */
  parts(s: string): string[];
  /** 词法切分(等同 Utils.stringSplit:识别运算符/标签/'::') */
  split(s: string): string[];
  /** 解析链式参数(等同 Utils.getChainParams),返回键→值对象 */
  chain(s: string): { [key: string]: string };
  /** Building 分类 contains(含模组运行期合并的游戏内容名) */
  buildingContains(name: string): boolean;
  /** Lookup 分类 contains(含模组运行期合并的游戏内容名) */
  lookupContains(name: string): boolean;
  /** 输出一条编译警告(打印到 stderr,不中断编译) */
  warn(msg: string): void;
}

/** 指令处理器签名:输入参数串与桥接对象,输出 mdtcode 行(可含 \n 多行) */
declare type BuiltinHandler = (s: string, ctx: EmitCtx) => string;

/** 链式键声明:键名 + 缺省值(compile 与 restore 的单一默认值来源) */
declare interface ChainKeyDef {
  key: string;
  def: string;
  /** 参数提示:签名帮助与补全用(缺省 = 无参数) */
  params?: string[];
}

/**
 * 指令双向映射定义:一份定义同时派生 compile(发射)与 decompile(还原)两侧。
 * 每个指令一个文件,位于 builtins/{大类名}/ 目录。
 */
declare interface InstrDef {
  /** compile 扫描键(如 "print(" / ".sensor(" / "not(") */
  key: string;
  /** mdtcode 指令字(无空格);缺省由 key 派生:去首 "."、去尾 "(" */
  mcode?: string;
  /** 共享指令字(mcode)的行首 token 分派;
   *  命中时本定义的 restore 被调用,输入为分派 token 之后的参数串。
   *  缺省 = 该 mcode 的兜底定义(无任何 token 命中时使用)。 */
  mcodeSelect?: string[];
  /** 链式调用的合法链键声明;编译时对未知链键输出警告(不影响输出)。 */
  chain?: ChainKeyDef[];
  /** 参数提示:括号内参数名列表(签名帮助用;缺省 = 无参数) */
  params?: string[];
  /** 发射:mdtc 参数 → mdtcode 行(可含 \n 多行) */
  compile: BuiltinHandler;
  /** 还原:mdtcode 参数 → mdtc 文本。
   *  缺省 = 无反编译条目(如 printf/jump2:多行折叠仍由 decompile 管道处理)。 */
  restore?: BuiltinHandler;
}
