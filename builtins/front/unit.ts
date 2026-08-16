/**
 * unit — 内容查询(规范见 docs/instructions/front.md)
 * mcode 为共享指令字 "lookup",按 "unit" 分派。
 */
namespace Builtins {
  export namespace Front {
    export const unit: InstrDef = {
      key: 'unit',
      mcode: 'lookup',
      mcodeSelect: ['unit'],
      compile: function (s, ctx) { return 'lookup unit ' + ctx.mid() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[1] + '=unit(' + p[2] + ')';
      }
    };
  }
}
