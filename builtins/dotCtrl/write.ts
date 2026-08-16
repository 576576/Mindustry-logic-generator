/**
 * .write — 内存写指令(规范见 docs/instructions/dot.md)
 */
namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;

    export const write: InstrDef = {
      key: 'write',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'write ' + H.getOr(w, 0, D.VAL_NUL) + ' ' + ctx.block() + ' ' + H.getOr(w, 1, D.VAL_0);
      },
      restore: function (s) {
        const p = s.split(' ');
        const content = p[2] === '0' ? p[0] : p[0] + ',' + p[2];
        return p[1] + '.write(' + content + ')';
      }
    };
  }
}
