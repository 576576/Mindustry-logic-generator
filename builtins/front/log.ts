/**
 * log — 任意底对数(两参交换)。
 *
 * 语法:`log(<底数>,<真数>)`
 * 输出:`op logn mid.<ref> <w1> <w0>`(两参交换)
 * 示例:`log(2,8)` → `op logn mid.1 8 2`
 * 反编译:op 行通用还原(log 交换两参并去尾部 0)。
 */

namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;

    export const log: InstrDef = {
      desc: '对数(底数,真数)',
      key: 'log',
      params: ['底数', '真数'],
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'op logn ' + ctx.mid() + ' ' + H.getOr(w, 1, 'null') + ' ' + H.getOr(w, 0, 'null');
      }
    };
  }
}
