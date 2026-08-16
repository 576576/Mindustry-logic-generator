/**
 * .read — 内存读取(表达式,结果变量在等号左侧)。
 *
 * 语法:`<结果> = <block>.read(<单元号>)`
 * 输出:`read mid.<ref> <block> <s>`;调用方随后把 mid.<ref> 代入表达式并递增 ref
 * 反编译:`read ` → `<结果>=<block>.read(<单元号>)`
 */

namespace Builtins {
  export namespace Dot {
    export const read: InstrDef = {
      key: 'read',
      params: ['单元号'],
      mcode: 'read',
      compile: function (s, ctx) { return 'read ' + ctx.mid() + ' ' + ctx.block() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[0] + '=' + p[1] + '.read(' + p[2] + ')';
      }
    };
  }
}
