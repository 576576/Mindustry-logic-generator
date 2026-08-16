/**
 * .sensor — 传感器读取(规范见 docs/instructions/dot.md)
 */
namespace Builtins {
  export namespace Dot {
    export const sensor: InstrDef = {
      key: 'sensor',
      mcode: 'sensor',
      compile: function (s, ctx) { return 'sensor ' + ctx.mid() + ' ' + ctx.block() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[0] + '=' + p[1] + '.sensor(' + p[2] + ')';
      }
    };
  }
}
