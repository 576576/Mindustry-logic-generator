/**
 * printf — 多行展开指令;无 restore(多行 print+format 由 decompile 逐行还原)。
 *
 * 语法:`printf(<格式串>,<参数…>)`
 * 输出:参数不足 2 个时退化为 `print <s>`;否则产生多行:
 *   `print <p0>` + 每个后续参数一行 `format <pN>`。
 */

namespace Builtins {
  export namespace Ctrl {
    export const printf: InstrDef = {
      key: 'printf',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        if (w.length < 2) return 'print ' + s;
        let out = 'print ' + w[0];
        for (let i = 1; i < w.length; i++) out += '\nformat ' + w[i];
        return out;
      }
    };
  }
}
