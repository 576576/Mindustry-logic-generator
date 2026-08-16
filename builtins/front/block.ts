/**
 * block — 内容查询;mcode 为共享指令字 `lookup`,按 block 分派。
 *
 * 语法:`block(@copper-wall)`
 * 输出:`lookup block mid.<ref> <s>`
 * 反编译:`lookup block ` → `<结果>=block(<索引>)`
 */

namespace Builtins {
  export namespace Front {
    export const block: InstrDef = {
      desc: '查询方块内容',
      key: 'block',
      params: ['类型'],
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
