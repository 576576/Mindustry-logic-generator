/**
 * block — 内容查询(规范见 docs/instructions/front.md)
 * mcode 为共享指令字 "lookup",按 "block" 分派。
 */
namespace Builtins {
  export namespace Front {
    export const block: InstrDef = {
      key: 'block',
      mcode: 'lookup',
      mcodeSelect: ['block'],
      compile: function (s, ctx) { return 'lookup block ' + ctx.mid() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[1] + '=block(' + p[2] + ')';
      }
    };
  }
}
