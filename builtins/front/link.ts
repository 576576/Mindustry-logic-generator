/**
 * link — 方块链接引用(规范见 docs/instructions/front.md)
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
