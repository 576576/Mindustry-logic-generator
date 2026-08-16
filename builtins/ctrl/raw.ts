/**
 * raw — 原生透传指令;无 restore(未知指令行由 decompile 还原为 `raw("…")`)。
 *
 * 语法:`raw("<原生 mdtcode 指令>")`
 * 输出:`<s>`(原样透传,不做任何编译)
 * 注意:raw( 必须排在 draw( 之后(注册顺序即匹配顺序)。
 */

namespace Builtins {
  export namespace Ctrl {
    export const raw: InstrDef = {
      key: 'raw',
      params: ['原生 mdtcode 指令'],
      compile: function (s) { return s; }
    };
  }
}
