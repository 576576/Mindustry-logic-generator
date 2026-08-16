// 指令数据:从 builtins/gen/builtins.js(vm 执行)提取分类指令表、链键、参数与 mdtcode 指令字
import * as vm from "node:vm";
import * as fs from "node:fs";
import * as path from "node:path";

export interface ChainKeyInfo {
  key: string;
  def: string;
  params: string[];
}

export interface InstrInfo {
  key: string;
  fullKey: string;
  category: string;
  desc: string;
  params: string[];
  chain: ChainKeyInfo[];
  /** mdtcode 行首指令字(如 "op" / "control" / "sensor") */
  mcode: string;
}

export interface InstructionData {
  items: InstrInfo[];
  operators: string[];
  /** 可链式母指令 fullKey → 链键表 */
  chainByParent: Map<string, ChainKeyInfo[]>;
  /** mdtcode 合法行首指令字集合 */
  mcodes: Set<string>;
  /** op 指令的运算符名集合(如 equal / abs / sin) */
  opNames: Set<string>;
}

/** 编译器会输出但 builtins 未声明的 mdtcode 指令字 */
const EXTRA_MCODES = ["set", "getblock", "setprop", "unitlocate"];

/** front 指令键 → 实际 op 运算符名(lg→log10、log/lb→logn、ln→log) */
const FRONT_OP_NAME: Record<string, string> = {
  lg: "log10",
  log: "logn",
  lb: "logn",
  ln: "log",
};

let cached: InstructionData | null = null;

export function loadData(builtinsJsPath: string): InstructionData {
  if (cached) return cached;
  const code = fs.readFileSync(builtinsJsPath, "utf8");
  const sandbox: Record<string, unknown> = {};
  vm.createContext(sandbox);
  vm.runInContext(code, sandbox);
  const B = sandbox.Builtins as any;
  const items: InstrInfo[] = [];
  // Front 大类拆为 high(一元/二元运算)与 low(三角函数)两个分类对象;
  // ns 用于推导 mcode:Front 输出行首为 "op"
  const categories: Array<[string, any, string]> = [
    ["Ctrl", B?.Ctrl?.category, "控制指令"],
    ["Front", B?.Front?.high, "一元/二元运算"],
    ["Front", B?.Front?.low, "三角函数"],
    ["DotCtrl", B?.DotCtrl?.category, "链式控制"],
    ["Dot", B?.Dot?.category, "链式读取"],
  ];
  const chainByParent = new Map<string, ChainKeyInfo[]>();
  const mcodes = new Set<string>(EXTRA_MCODES);
  const opNames = new Set<string>();
  for (const [ns, cat, label] of categories) {
    if (cat?.defs) {
      for (const d of cat.defs) {
        if (!d?.key) continue;
        const fullKey = cat.keyOf(d.key);
        const chain: ChainKeyInfo[] = (d.chain ?? []).map((c: any) => ({
          key: c.key,
          def: c.def ?? "",
          params: c.params ?? [],
        }));
        if (chain.length > 0) chainByParent.set(fullKey, chain);
        let mcode = d.mcode;
        if (!mcode) {
          if (ns === "Front" && d.key !== "lookup") {
            mcode = "op"; // 一元/二元运算输出 op 行(lookup 例外:行首即 lookup)
            opNames.add(FRONT_OP_NAME[d.key] ?? d.key); // front 键 → 实际 op 名
          } else {
            mcode = d.key.replace(/^\./, "").replace(/\($|\)$/g, "");
          }
        }
        if (mcode) mcodes.add(mcode);
        items.push({
          key: d.key,
          fullKey,
          category: label,
          desc: d.desc ?? "",
          params: d.params ?? [],
          chain,
          mcode,
        });
      }
    }
  }
  // op 运算符:Operators.list 的 name + front 一元/二元键
  for (const o of B?.Operators?.list ?? []) {
    if (o?.name) opNames.add(o.name);
  }
  const operators: string[] = (B?.Operators?.list ?? []).map((o: any) => o.value);
  cached = { items, operators, chainByParent, mcodes, opNames };
  return cached;
}
