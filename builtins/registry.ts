/**
 * 注册表组装:由各指令的 InstrDef 双向映射派生 compile / decompile 注册表。
 * - key 为裸指令名;完整扫描键由大类 CategorySpec.keyOf 模板生成
 *   (ctrl/front 附加 "(",dotCtrl/dot 附加 "." + "(")
 * - compile 映射:完整键 → 处理器(带 chain 链键检查包装)
 * - decompile 映射:mcode + " " → 还原处理器(共享指令字按 mcodeSelect 分派)
 * - chain 表:完整键 → 合法链键列表(供测试与运行期检查)
 * - set / op 为语言机制级通用还原(由运算符表数据驱动)
 */
namespace Builtins {
  export namespace Registry {
    import H = Builtins.Helpers;
    import O = Builtins.Operators;

    /** 大类定义:裸指令名 → 完整扫描键的模板(附加匹配符号由大类定义) */
    export interface CategorySpec {
      keyOf: (name: string) => string;
      defs: InstrDef[];
    }

    /** 从 chain 声明取缺省值(compile 与 restore 的单一默认值来源) */
    export function chainDef(chain: ChainKeyDef[] | undefined, key: string): string {
      if (!chain) return '';
      for (const c of chain) {
        if (c.key === key) return c.def;
      }
      return '';
    }

    /** 链键白名单(首部 main 恒允许) */
    function allowedKeys(chain: ChainKeyDef[] | undefined): { [key: string]: boolean } {
      const m: { [key: string]: boolean } = { main: true };
      if (chain) {
        for (const c of chain) m[c.key] = true;
      }
      return m;
    }

    /** 带链检查的 compile 包装:未知链键输出警告(不影响输出) */
    export function wrapChain(def: InstrDef, fullKey: string): BuiltinHandler {
      if (!def.chain || def.chain.length === 0) return def.compile;
      return function (s, ctx) {
        const m = ctx.chain(s);
        const allow = allowedKeys(def.chain);
        for (const k of Object.keys(m)) {
          if (!allow[k]) {
            ctx.warn('链式警告: ' + fullKey + ' — 未知链键 "' + k + '"(已忽略) | chain warning: ' + fullKey + ' — unknown chain key "' + k + '" (ignored)');
          }
        }
        return def.compile(s, ctx);
      };
    }

    /** 大类 → compile 映射(完整扫描键 → 包装后处理器) */
    export function compileMap(spec: CategorySpec): { [key: string]: BuiltinHandler } {
      const m: { [key: string]: BuiltinHandler } = {};
      for (const def of spec.defs) {
        const fullKey = spec.keyOf(def.key);
        m[fullKey] = wrapChain(def, fullKey);
      }
      return m;
    }

    /** mcode 派生:显式 mcode 优先,否则裸 key 即 mdtcode 指令字 */
    export function mcodeOf(def: InstrDef): string {
      return def.mcode || def.key;
    }

    /** 共享指令字分派器:按行首 token 命中 mcodeSelect;无命中走兜底定义 */
    function dispatcher(defs: InstrDef[]): BuiltinHandler {
      const selectors: InstrDef[] = [];
      let fallback: InstrDef | null = null;
      for (const d of defs) {
        if (d.mcodeSelect && d.mcodeSelect.length > 0) selectors.push(d);
        else if (fallback == null) fallback = d;
      }
      return function (s, ctx) {
        const idx = s.indexOf(' ');
        const token = idx === -1 ? s : s.slice(0, idx);
        for (const d of selectors) {
          if (d.mcodeSelect!.indexOf(token) !== -1 && d.restore) {
            return d.restore(s, ctx);
          }
        }
        if (fallback != null && fallback.restore) return fallback.restore(s, ctx);
        return '';
      };
    }

    /** defs → decompile 映射(mcode + " " → 还原处理器);仅收集带 restore 的定义 */
    export function decompileMap(defs: InstrDef[]): { [key: string]: BuiltinHandler } {
      const groups: { [mcode: string]: InstrDef[] } = {};
      for (const def of defs) {
        if (!def.restore) continue;
        const mcode = mcodeOf(def);
        if (groups[mcode] == null) groups[mcode] = [];
        groups[mcode].push(def);
      }
      const m: { [key: string]: BuiltinHandler } = {};
      for (const mcode of Object.keys(groups)) {
        const g = groups[mcode];
        m[mcode + ' '] = g.length === 1 ? g[0].restore! : dispatcher(g);
      }
      return m;
    }

    /** 大类 → chain 表(完整扫描键 → 合法链键列表,顺序 = 声明顺序) */
    export function chainTable(spec: CategorySpec): { [key: string]: string[] } {
      const t: { [key: string]: string[] } = {};
      for (const d of spec.defs) {
        if (d.chain && d.chain.length > 0) {
          t[spec.keyOf(d.key)] = d.chain.map(c => c.key);
        }
      }
      return t;
    }

    /** 赋值还原("x 1" → "x=1")——语言机制,非内置指令 */
    export function setRestore(s: string): string {
      return s.replace(' ', '=');
    }

    /** 运算符行还原("op <op> …" → mdtc 表达式)——由运算符表/别名表数据驱动 */
    export function opRestore(): BuiltinHandler {
      return function (s) {
        const p = s.split(' ');
        const operator0 = p[0];
        const result = p[1];
        let operator = operator0;
        let paramString = '';
        const val = O.byName[operator0];
        if (val != null) {
          if (p.length < 4) {
            return result + '=' + val + '(' + (p[2] == null ? '' : p[2]) + ')';
          }
          return result + '=' + (p[2] == null ? '' : p[2]) + ' ' + val + ' ' + (p[3] == null ? '' : p[3]);
        } else if (operator0 === 'logn' && p.length > 4 && p[4] === '2') {
          operator = 'lb';
          paramString = p[2] == null ? '' : p[2];
        } else {
          operator = O.alias[operator0] == null ? operator0 : O.alias[operator0];
          if (p.length <= 3) {
            paramString = p[2] == null ? '' : p[2];
          } else if (operator === 'log') {
            paramString = H.reduce('0', (p[3] == null ? '' : p[3]) + ' ' + (p[2] == null ? '' : p[2]));
          } else {
            paramString = H.reduce('0', (p[2] == null ? '' : p[2]) + ' ' + (p[3] == null ? '' : p[3]));
          }
        }
        return result + '=' + operator + '(' + paramString + ')';
      };
    }
  }
}
