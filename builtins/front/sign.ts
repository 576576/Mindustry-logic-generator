/**
 * sign — 一元运算(规范见 docs/instructions/front.md)
 * 无 restore:op 行由注册表通用 opRestore 还原。
 */
namespace Builtins {
  export namespace Front {
    export const sign: InstrDef = {
      key: 'sign',
      compile: function (s, ctx) { return 'op sign ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
