/**
 * link — 方块链接引用。
 *
 * 语法:`link(<索引>)`
 * 输出:`getlink mid.<ref> <s>`
 * 反编译:`getlink ` → `<结果>=link(<索引>)`
 */

namespace Builtins {
  export namespace Front {
    export const link: InstrDef = {
      key: 'link',
      mcode: 'getlink',
      compile: function (s, ctx) { return 'getlink ' + ctx.mid() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[0] + '=link(' + p[1] + ')';
      }
    };
  }
}
