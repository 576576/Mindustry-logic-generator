/**
 * unit — 内容查询;mcode 为共享指令字 `lookup`,按 unit 分派。
 *
 * 语法:`unit(@copper-wall)`
 * 输出:`lookup unit mid.<ref> <s>`
 * 反编译:`lookup unit ` → `<结果>=unit(<索引>)`
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
