/**
 * uctrl — 固定填充指令;mcode 为共享指令字 `ucontrol`(兜底)。
 *
 * 语法:`uctrl(<类型>,<参数…>)`
 * 输出:`ucontrol <pad(6, s)>`(参数按顶层逗号切分,填充到 6 项,缺省 `0`)
 * 示例:`uctrl(getBlock)` → `ucontrol getBlock 0 0 0 0 0`
 * 反编译:`ucontrol ` 非 target/targetp → `uctrl(reduce(0, 类型 参数…))`
 */

namespace Builtins {
  export namespace Ctrl {
    import H = Builtins.Helpers;

    export const uctrl: InstrDef = {
      key: 'uctrl',
      params: ['类型', '参数…'],
      mcode: 'ucontrol',
      compile: function (s, ctx) { return 'ucontrol ' + H.padZero(6, ctx.parts(s)); },
      restore: function (s) {
        const parts = H.splitInto(s, 2);
        return 'uctrl(' + H.reduce('0', parts[0] + ' ' + parts[1]) + ')';
      }
    };
  }
}
