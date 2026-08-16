// 指令数据:从 builtins/gen/builtins.js(vm 执行)提取分类指令表、链键与参数提示
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
}

export interface InstructionData {
  items: InstrInfo[];
  operators: string[];
  /** 可链式母指令 fullKey → 链键表 */
  chainByParent: Map<string, ChainKeyInfo[]>;
}

let cached: InstructionData | null = null;

export function loadData(builtinsJsPath: string): InstructionData {
  if (cached) return cached;
  const code = fs.readFileSync(builtinsJsPath, "utf8");
  const sandbox: Record<string, unknown> = {};
  vm.createContext(sandbox);
  vm.runInContext(code, sandbox);
  const B = sandbox.Builtins as any;
  const items: InstrInfo[] = [];
  // Front 大类拆为 high(一元/二元运算)与 low(三角函数)两个分类对象
  const categories: Array<[any, string]> = [
    [B?.Ctrl?.category, "控制指令"],
    [B?.Front?.high, "一元/二元运算"],
    [B?.Front?.low, "三角函数"],
    [B?.DotCtrl?.category, "链式控制"],
    [B?.Dot?.category, "链式读取"],
  ];
  const chainByParent = new Map<string, ChainKeyInfo[]>();
  for (const [cat, label] of categories) {
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
        items.push({
          key: d.key,
          fullKey,
          category: label,
          desc: d.desc ?? "",
          params: d.params ?? [],
          chain,
        });
      }
    }
  }
  const operators: string[] = (B?.Operators?.list ?? []).map((o: any) => o.value);
  cached = { items, operators, chainByParent };
  return cached;
}
