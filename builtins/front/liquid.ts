/**
 * liquid — 内容查询(规范见 docs/instructions/front.md)
 * mcode 为共享指令字 "lookup",按 "liquid" 分派。
 */
namespace Builtins {
  export namespace Front {
    export const liquid: InstrDef = {
      key: 'liquid',
      mcode: 'lookup',
      mcodeSelect: ['liquid'],
      compile: function (s, ctx) { return 'lookup liquid ' + ctx.mid() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[1] + '=liquid(' + p[2] + ')';
      }
    };
  }
}
