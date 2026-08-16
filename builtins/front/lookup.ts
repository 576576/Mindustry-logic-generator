/**
 * lookup — 通用内容查询;mcode 为共享指令字 `lookup`(兜底:非标准内容类型)。
 *
 * 语法:`lookup(<类型>,<索引>)`
 * 输出:`lookup <w0|block> mid.<ref> <wLast|0>`(类型缺省 block,索引取末参缺省 0)
 * 反编译:`lookup ` 非标准类型 → `<结果>=lookup(<类型>,<索引>)`
 */

namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;

    export const lookup: InstrDef = {
      desc: '按索引查询内容',
      key: 'lookup',
      params: ['类型', '索引'],
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
