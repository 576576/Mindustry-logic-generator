/**
 * team — 内容查询;mcode 为共享指令字 `lookup`,按 team 分派。
 *
 * 语法:`team(@copper-wall)`
 * 输出:`lookup team mid.<ref> <s>`
 * 反编译:`lookup team ` → `<结果>=team(<索引>)`
 */

namespace Builtins {
  export namespace Front {
    export const team: InstrDef = {
      key: 'team',
      params: ['阵营'],
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
