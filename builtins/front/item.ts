/**
 * item — 内容查询(规范见 docs/instructions/front.md)
 * mcode 为共享指令字 "lookup",按 "item" 分派。
 */
namespace Builtins {
  export namespace Front {
    export const item: InstrDef = {
      key: 'item',
      mcode: 'lookup',
      mcodeSelect: ['item'],
      compile: function (s, ctx) { return 'lookup item ' + ctx.mid() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[1] + '=item(' + p[2] + ')';
      }
    };
  }
}
