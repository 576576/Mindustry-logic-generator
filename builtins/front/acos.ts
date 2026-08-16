/**
 * acos — 一元运算(规范见 docs/instructions/front.md)
 * 无 restore:op 行由注册表通用 opRestore 还原。
 */
namespace Builtins {
  export namespace Front {
    export const acos: InstrDef = {
      key: 'acos',
      compile: function (s, ctx) { return 'op acos ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
