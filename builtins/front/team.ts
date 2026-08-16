/**
 * team — 内容查询(规范见 docs/instructions/front.md)
 * mcode 为共享指令字 "lookup",按 "team" 分派。
 */
namespace Builtins {
  export namespace Front {
    export const team: InstrDef = {
      key: 'team',
      mcode: 'lookup',
      mcodeSelect: ['team'],
      compile: function (s, ctx) { return 'lookup team ' + ctx.mid() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[1] + '=team(' + p[2] + ')';
      }
    };
  }
}
