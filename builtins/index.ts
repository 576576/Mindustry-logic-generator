/**
 * 注册表组装:由各指令的 InstrDef 双向映射派生 compile / decompile / chain 注册表。
 * 完整扫描键由大类 CategorySpec.keyOf 模板生成(附加匹配符号由大类定义)。
 * 键结构保持与 Java BuiltinEngine 兼容(compile 五类映射 + decompile + chain + 数据)。
 */
namespace Builtins {
  const ALL_DEFS: InstrDef[] = ([] as InstrDef[]).concat(
    Ctrl.category.defs, DotCtrl.category.defs, Dot.category.defs, Front.category.defs
  );

  function buildRegistry() {
    const decompile = Registry.decompileMap(ALL_DEFS);
    // 语言机制级通用还原(非内置指令):赋值与运算符行
    decompile['set '] = Registry.setRestore;
    decompile['op '] = Registry.opRestore();

    // chain 表:各大类的链式指令声明(完整扫描键 → 合法链键)
    const chain: { [key: string]: string[] } = {};
    for (const spec of [Ctrl.category, DotCtrl.category, Dot.category, Front.category]) {
      const t = Registry.chainTable(spec);
      for (const k of Object.keys(t)) chain[k] = t[k];
    }

    return {
      ctrl: Registry.compileMap(Ctrl.category),
      dotCtrl: Registry.compileMap(DotCtrl.category),
      dot: Registry.compileMap(Dot.category),
      frontHigh: Registry.compileMap(Front.high),
      frontLow: Registry.compileMap(Front.low),
      decompile: decompile,
      chain: chain,
      domain: Domain,
      codes: Codes,
      operators: Operators
    };
  }

  export const registry = buildRegistry();
}
