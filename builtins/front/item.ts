/**
 * item — 内容查询;mcode 为共享指令字 `lookup`,按 item 分派。
 *
 * 语法:`item(@copper-wall)`
 * 输出:`lookup item mid.<ref> <s>`
 * 反编译:`lookup item ` → `<结果>=item(<索引>)`
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
