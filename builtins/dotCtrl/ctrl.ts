/**
 * .ctrl — 控制指令兜底;mcode 为共享指令字 `control`(无 mcodeSelect = 兜底)。
 *
 * 语法:`<block>.ctrl(<类型>,<参数…>)`
 * 输出:`control <w0|enabled> <block> <pad(4, w1)>`(w0 缺省 enabled,w1 缺省空串)
 * 反编译:`control ` 未知类型 → `<block>.ctrl(reduce(0, 类型 参数…))`
 */

namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;

    export const ctrl: InstrDef = {
      key: 'ctrl',
      mcode: 'control',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        const type = H.getOr(w, 0, D.CONTROL_DEFAULT);
        return 'control' + type + ctx.block() + ' ' + H.padZero(4, [H.getOr(w, 1, '')]);
      },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        return parts[1] + '.ctrl(' + H.reduce('0', parts[0] + ' ' + (parts[2] == null ? '' : parts[2])) + ')';
      }
    };
  }
}
