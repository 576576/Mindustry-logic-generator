/**
 * min — 二元运算。
 *
 * 语法:`min(a,b)`
 * 输出:`op min mid.<ref> <w0> <w1>`(w0/w1 为前两个参数)
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=min(a,b)`)。
 */

namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;

    export const min: InstrDef = {
      key: 'min',
      params: ['a', 'b'],
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'op min ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
      }
    };
  }
}
