/**
 * .read — 内存读取(规范见 docs/instructions/dot.md)
 */
namespace Builtins {
  export namespace Dot {
    export const read: InstrDef = {
      key: 'read',
      mcode: 'read',
      compile: function (s, ctx) { return 'read ' + ctx.mid() + ' ' + ctx.block() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[0] + '=' + p[1] + '.read(' + p[2] + ')';
      }
    };
  }
}
