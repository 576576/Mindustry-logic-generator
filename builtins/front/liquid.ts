/**
 * liquid — 内容查询;mcode 为共享指令字 `lookup`,按 liquid 分派。
 *
 * 语法:`liquid(@copper-wall)`
 * 输出:`lookup liquid mid.<ref> <s>`
 * 反编译:`lookup liquid ` → `<结果>=liquid(<索引>)`
 */

namespace Builtins {
  export namespace Front {
    export const liquid: InstrDef = {
      key: 'liquid',
      params: ['液体'],
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
