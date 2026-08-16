/**
 * lookup — 通用内容查询(规范见 docs/instructions/front.md)
 * mcode 为共享指令字 "lookup"(兜底:非标准内容类型)。
 */
namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;

    export const lookup: InstrDef = {
      key: 'lookup',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'lookup ' + H.getOr(w, 0, D.LOOKUP_TYPES[0]) + ' ' + ctx.mid() + ' ' + H.getLastOr(w, D.VAL_0);
      },
      restore: function (s) {
        const p = s.split(' ');
        return p[1] + '=lookup(' + p[0] + ',' + p[2] + ')';
      }
    };
  }
}
